package br.edu.ifrn.helpif_api.services;

import br.edu.ifrn.helpif_api.dtos.*;
import br.edu.ifrn.helpif_api.entities.*;
import br.edu.ifrn.helpif_api.exceptions.RegraNegocioException;
import br.edu.ifrn.helpif_api.repositories.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;

@Service
public class ChamadoService {
    private final ChamadoRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final InteracaoRepository interacaoRepository;

    public ChamadoService(ChamadoRepository repository, UsuarioRepository usuarioRepository,
                          CategoriaRepository categoriaRepository, InteracaoRepository interacaoRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.interacaoRepository = interacaoRepository;
    }

    @Transactional
    public ChamadoResponseDTO abrir(ChamadoRequestDTO dto) {
        Usuario solicitante = encontrarUsuario(dto.solicitanteId());
        if (solicitante.getTipo() != TipoUsuario.SOLICITANTE) {
            throw new RegraNegocioException("Apenas solicitantes podem abrir chamados");
        }
        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
        Chamado chamado = new Chamado();
        chamado.setTitulo(dto.titulo().trim());
        chamado.setDescricao(dto.descricao().trim());
        chamado.setLocal(dto.local().trim());
        chamado.setPatrimonio(dto.patrimonio());
        chamado.setPrioridade(dto.prioridade() == null ? Prioridade.MEDIA : dto.prioridade());
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setDataAbertura(LocalDateTime.now());
        chamado.setSolicitante(solicitante);
        chamado.setCategoria(categoria);
        return new ChamadoResponseDTO(repository.save(chamado));
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponseDTO> listar(StatusChamado status, Prioridade prioridade,
            Long setorId, Long categoriaId, Long solicitanteId, Long tecnicoId,
            LocalDateTime dataInicio, LocalDateTime dataFim, Pageable pageable) {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Data inicial deve ser anterior ou igual à data final");
        }
        return repository.filtrar(status, prioridade, setorId, categoriaId, solicitanteId,
                tecnicoId, dataInicio, dataFim, pageable).map(ChamadoResponseDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponseDTO> listarPorUsuario(Long usuarioId, StatusChamado status, Pageable pageable) {
        Usuario usuario = encontrarUsuario(usuarioId);
        Long solicitanteId = usuario.getTipo() == TipoUsuario.SOLICITANTE ? usuarioId : null;
        Long tecnicoId = usuario.getTipo() == TipoUsuario.TECNICO ? usuarioId : null;
        return listar(status, null, null, null, solicitanteId, tecnicoId, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public ChamadoDetalheDTO buscarPorId(Long id) {
        Chamado chamado = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chamado não encontrado"));
        var interacoes = interacaoRepository.findByChamado_IdOrderByDataHoraAscIdAsc(id)
                .stream().map(InteracaoResponseDTO::new).toList();
        return new ChamadoDetalheDTO(new ChamadoResponseDTO(chamado), interacoes);
    }

    @Transactional
    public ChamadoResponseDTO assumir(Long id, AssumirChamadoDTO dto) {
        Chamado chamado = encontrarParaAtualizar(id);
        exigirStatus(chamado, StatusChamado.ABERTO);
        Usuario tecnico = encontrarUsuario(dto.tecnicoId());
        if (tecnico.getTipo() != TipoUsuario.TECNICO || tecnico.getSetor() == null
                || !tecnico.getSetor().getId().equals(chamado.getCategoria().getSetor().getId())) {
            throw new RegraNegocioException("Apenas um técnico do setor da categoria pode assumir o chamado");
        }
        chamado.setTecnico(tecnico);
        chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
        return new ChamadoResponseDTO(repository.save(chamado));
    }

    @Transactional
    public ChamadoResponseDTO resolver(Long id, ResolverChamadoDTO dto) {
        Chamado chamado = encontrarParaAtualizar(id);
        exigirStatus(chamado, StatusChamado.EM_ATENDIMENTO);
        if (chamado.getTecnico() == null || !chamado.getTecnico().getId().equals(dto.tecnicoId())) {
            throw new RegraNegocioException("Somente o técnico atribuído pode resolver o chamado");
        }
        if (dto.solucao() == null || dto.solucao().isBlank()) {
            throw new RegraNegocioException("A solução é obrigatória");
        }
        chamado.setSolucao(dto.solucao().trim());
        chamado.setStatus(StatusChamado.RESOLVIDO);
        chamado.setDataFechamento(LocalDateTime.now());
        return new ChamadoResponseDTO(repository.save(chamado));
    }

    @Transactional
    public ChamadoResponseDTO cancelar(Long id, CancelarChamadoDTO dto) {
        Chamado chamado = encontrarParaAtualizar(id);
        exigirStatus(chamado, StatusChamado.ABERTO);
        if (!chamado.getSolicitante().getId().equals(dto.solicitanteId())) {
            throw new RegraNegocioException("Somente o próprio solicitante pode cancelar o chamado");
        }
        chamado.setStatus(StatusChamado.CANCELADO);
        chamado.setDataFechamento(LocalDateTime.now());
        return new ChamadoResponseDTO(repository.save(chamado));
    }

    @Transactional
    public InteracaoResponseDTO registrarInteracao(Long id, InteracaoRequestDTO dto) {
        Chamado chamado = encontrarParaAtualizar(id);
        if (chamado.getStatus() == StatusChamado.RESOLVIDO || chamado.getStatus() == StatusChamado.CANCELADO) {
            throw new RegraNegocioException("Chamado encerrado não aceita novas interações");
        }
        Usuario autor = encontrarUsuario(dto.autorId());
        boolean solicitante = chamado.getSolicitante().getId().equals(autor.getId());
        boolean tecnico = chamado.getTecnico() != null && chamado.getTecnico().getId().equals(autor.getId());
        if (!solicitante && !tecnico) {
            throw new RegraNegocioException("Apenas o solicitante e o técnico atribuído podem registrar interações");
        }
        Interacao interacao = new Interacao();
        interacao.setTexto(dto.texto().trim());
        interacao.setDataHora(LocalDateTime.now());
        interacao.setChamado(chamado);
        interacao.setAutor(autor);
        return new InteracaoResponseDTO(interacaoRepository.save(interacao));
    }

    private Chamado encontrarParaAtualizar(Long id) {
        return repository.buscarParaAtualizar(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chamado não encontrado"));
    }

    private Usuario encontrarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    private void exigirStatus(Chamado chamado, StatusChamado status) {
        if (chamado.getStatus() != status) {
            throw new RegraNegocioException("Ação não permitida para o status atual do chamado");
        }
    }
}
