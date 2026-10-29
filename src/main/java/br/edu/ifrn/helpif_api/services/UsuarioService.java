package br.edu.ifrn.helpif_api.services;

import br.edu.ifrn.helpif_api.dtos.UsuarioRequestDTO;
import br.edu.ifrn.helpif_api.dtos.UsuarioResponseDTO;
import br.edu.ifrn.helpif_api.entities.*;
import br.edu.ifrn.helpif_api.exceptions.RegraNegocioException;
import br.edu.ifrn.helpif_api.repositories.SetorRepository;
import br.edu.ifrn.helpif_api.repositories.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;
    private final SetorRepository setorRepository;

    public UsuarioService(UsuarioRepository repository, SetorRepository setorRepository) {
        this.repository = repository;
        this.setorRepository = setorRepository;
    }

    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        if (repository.existsByEmailIgnoreCase(dto.email().trim())) {
            throw new RegraNegocioException("Email já cadastrado");
        }
        Usuario usuario = new Usuario();
        preencher(usuario, dto);
        return new UsuarioResponseDTO(repository.saveAndFlush(usuario));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listar(String nome, TipoUsuario tipo,
                                           Long setorId, Pageable pageable) {
        String filtroNome = nome == null || nome.isBlank() ? null : nome.trim();
        return repository.filtrar(filtroNome, tipo, setorId, pageable)
                .map(UsuarioResponseDTO::new);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        return new UsuarioResponseDTO(encontrarUsuario(id));
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = encontrarUsuario(id);
        if (repository.existsByEmailIgnoreCaseAndIdNot(dto.email().trim(), id)) {
            throw new RegraNegocioException("Email já cadastrado");
        }
        preencher(usuario, dto);
        return new UsuarioResponseDTO(repository.saveAndFlush(usuario));
    }

    private void preencher(Usuario usuario, UsuarioRequestDTO dto) {
        Setor setor = null;
        if (dto.tipo() == TipoUsuario.TECNICO && dto.setorId() == null) {
            throw new RegraNegocioException("Técnico deve pertencer a um setor");
        }
        if (dto.setorId() != null) {
            setor = setorRepository.findById(dto.setorId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Setor não encontrado"));
        }
        usuario.setNome(dto.nome().trim());
        usuario.setEmail(dto.email().trim());
        usuario.setMatricula(dto.matricula().trim());
        usuario.setTipo(dto.tipo());
        usuario.setSetor(setor);
    }

    private Usuario encontrarUsuario(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }
}
