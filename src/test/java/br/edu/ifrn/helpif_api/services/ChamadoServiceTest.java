package br.edu.ifrn.helpif_api.services;

import br.edu.ifrn.helpif_api.dtos.*;
import br.edu.ifrn.helpif_api.entities.*;
import br.edu.ifrn.helpif_api.exceptions.RegraNegocioException;
import br.edu.ifrn.helpif_api.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.data.domain.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChamadoServiceTest {
    private final ChamadoRepository repository = mock(ChamadoRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final CategoriaRepository categorias = mock(CategoriaRepository.class);
    private final InteracaoRepository interacoes = mock(InteracaoRepository.class);
    private final ChamadoService service = new ChamadoService(repository, usuarios, categorias, interacoes);
    private Usuario solicitante;
    private Usuario tecnico;
    private Categoria categoria;
    private Chamado chamado;

    @BeforeEach
    void preparar() {
        Setor setor = new Setor();
        setor.setId(1L);
        solicitante = usuario(1L, TipoUsuario.SOLICITANTE, null);
        tecnico = usuario(3L, TipoUsuario.TECNICO, setor);
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setSetor(setor);
        chamado = new Chamado();
        chamado.setId(10L);
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setSolicitante(solicitante);
        chamado.setCategoria(categoria);
        when(repository.buscarParaAtualizar(10L)).thenReturn(Optional.of(chamado));
        when(repository.save(any(Chamado.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarios.findById(1L)).thenReturn(Optional.of(solicitante));
        when(usuarios.findById(3L)).thenReturn(Optional.of(tecnico));
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria));
    }

    private Usuario usuario(Long id, TipoUsuario tipo, Setor setor) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setTipo(tipo);
        usuario.setSetor(setor);
        return usuario;
    }

    private ChamadoRequestDTO abertura(Long solicitanteId, Prioridade prioridade) {
        return new ChamadoRequestDTO(" Projetor ", "Sem imagem", "Sala 1", null,
                prioridade, solicitanteId, 1L);
    }

    @Test
    void abreComStatusDataEPrioridadePadrao() {
        LocalDateTime antes = LocalDateTime.now();
        var resposta = service.abrir(abertura(1L, null));
        assertEquals(StatusChamado.ABERTO, resposta.status());
        assertEquals(Prioridade.MEDIA, resposta.prioridade());
        assertEquals("Projetor", resposta.titulo());
        assertFalse(resposta.dataAbertura().isBefore(antes));
        assertNull(resposta.tecnicoId());
        assertNull(resposta.dataFechamento());
    }

    @Test
    void preservaPrioridadeInformada() {
        assertEquals(Prioridade.ALTA, service.abrir(abertura(1L, Prioridade.ALTA)).prioridade());
    }

    @Test
    void tecnicoNaoAbreChamado() {
        assertThrows(RegraNegocioException.class, () -> service.abrir(abertura(3L, null)));
        verify(repository, never()).save(any());
    }

    @Test
    void categoriaInexistenteRetorna404() {
        when(categorias.findById(1L)).thenReturn(Optional.empty());
        var erro = assertThrows(ResponseStatusException.class, () -> service.abrir(abertura(1L, null)));
        assertEquals(404, erro.getStatusCode().value());
    }

    @Test
    void tecnicoDoSetorAssumeChamadoAberto() {
        var resposta = service.assumir(10L, new AssumirChamadoDTO(3L));
        assertEquals(StatusChamado.EM_ATENDIMENTO, resposta.status());
        assertEquals(3L, resposta.tecnicoId());
    }

    @Test
    void tecnicoDeOutroSetorNaoAssume() {
        Setor outro = new Setor();
        outro.setId(2L);
        tecnico.setSetor(outro);
        assertThrows(RegraNegocioException.class, () -> service.assumir(10L, new AssumirChamadoDTO(3L)));
        assertEquals(StatusChamado.ABERTO, chamado.getStatus());
    }

    @Test
    void solicitanteNaoAssume() {
        assertThrows(RegraNegocioException.class, () -> service.assumir(10L, new AssumirChamadoDTO(1L)));
    }

    @ParameterizedTest
    @EnumSource(value = StatusChamado.class, names = {"EM_ATENDIMENTO", "RESOLVIDO", "CANCELADO"})
    void naoAssumeChamadoForaDeAberto(StatusChamado status) {
        chamado.setStatus(status);
        assertThrows(RegraNegocioException.class, () -> service.assumir(10L, new AssumirChamadoDTO(3L)));
    }

    @Test
    void tecnicoAtribuidoResolveComSolucaoEData() {
        chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
        chamado.setTecnico(tecnico);
        var resposta = service.resolver(10L, new ResolverChamadoDTO(3L, " Cabo trocado "));
        assertEquals(StatusChamado.RESOLVIDO, resposta.status());
        assertEquals("Cabo trocado", resposta.solucao());
        assertNotNull(resposta.dataFechamento());
    }

    @Test
    void outroUsuarioNaoResolve() {
        chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
        chamado.setTecnico(tecnico);
        assertThrows(RegraNegocioException.class,
                () -> service.resolver(10L, new ResolverChamadoDTO(1L, "Resolvido")));
    }

    @Test
    void solucaoVaziaNaoResolve() {
        chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
        chamado.setTecnico(tecnico);
        assertThrows(RegraNegocioException.class,
                () -> service.resolver(10L, new ResolverChamadoDTO(3L, "   ")));
    }

    @ParameterizedTest
    @EnumSource(value = StatusChamado.class, names = {"ABERTO", "RESOLVIDO", "CANCELADO"})
    void naoResolveForaDeAtendimento(StatusChamado status) {
        chamado.setStatus(status);
        chamado.setTecnico(tecnico);
        assertThrows(RegraNegocioException.class,
                () -> service.resolver(10L, new ResolverChamadoDTO(3L, "Resolvido")));
    }

    @Test
    void solicitanteCancelaChamadoAberto() {
        var resposta = service.cancelar(10L, new CancelarChamadoDTO(1L));
        assertEquals(StatusChamado.CANCELADO, resposta.status());
        assertNotNull(resposta.dataFechamento());
    }

    @Test
    void outroUsuarioNaoCancela() {
        assertThrows(RegraNegocioException.class, () -> service.cancelar(10L, new CancelarChamadoDTO(3L)));
    }

    @ParameterizedTest
    @EnumSource(value = StatusChamado.class, names = {"EM_ATENDIMENTO", "RESOLVIDO", "CANCELADO"})
    void naoCancelaForaDeAberto(StatusChamado status) {
        chamado.setStatus(status);
        assertThrows(RegraNegocioException.class, () -> service.cancelar(10L, new CancelarChamadoDTO(1L)));
    }

    @ParameterizedTest
    @EnumSource(value = StatusChamado.class, names = {"RESOLVIDO", "CANCELADO"})
    void encerradoNaoAceitaInteracao(StatusChamado status) {
        chamado.setStatus(status);
        assertThrows(RegraNegocioException.class,
                () -> service.registrarInteracao(10L, new InteracaoRequestDTO("Teste", 1L)));
        verify(interacoes, never()).save(any());
    }

    @Test
    void terceiroNaoRegistraInteracao() {
        assertThrows(RegraNegocioException.class,
                () -> service.registrarInteracao(10L, new InteracaoRequestDTO("Teste", 3L)));
        verify(interacoes, never()).save(any());
    }

    @Test
    void solicitanteRegistraInteracaoEmAberto() {
        when(interacoes.save(any(Interacao.class))).thenAnswer(i -> i.getArgument(0));
        var resposta = service.registrarInteracao(10L, new InteracaoRequestDTO(" Aguardando ", 1L));
        assertEquals("Aguardando", resposta.texto());
        assertEquals(1L, resposta.autorId());
        assertNotNull(resposta.dataHora());
    }

    @Test
    void tecnicoAtribuidoRegistraInteracao() {
        chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
        chamado.setTecnico(tecnico);
        when(interacoes.save(any(Interacao.class))).thenAnswer(i -> i.getArgument(0));
        assertEquals(3L, service.registrarInteracao(10L,
                new InteracaoRequestDTO("Em análise", 3L)).autorId());
    }

    @Test
    void chamadoInexistenteRetorna404() {
        when(repository.buscarParaAtualizar(99L)).thenReturn(Optional.empty());
        var erro = assertThrows(ResponseStatusException.class,
                () -> service.assumir(99L, new AssumirChamadoDTO(3L)));
        assertEquals(404, erro.getStatusCode().value());
    }

    @Test
    void periodoInvertidoRetorna400() {
        var erro = assertThrows(ResponseStatusException.class,
                () -> service.listar(null, null, null, null, null, null,
                        LocalDateTime.of(2026, 10, 30, 12, 0),
                        LocalDateTime.of(2026, 10, 29, 12, 0), PageRequest.of(0, 10)));
        assertEquals(400, erro.getStatusCode().value());
        verify(repository, never()).filtrar(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void combinaTodosOsFiltros() {
        var inicio = LocalDateTime.of(2026, 10, 1, 0, 0);
        var fim = LocalDateTime.of(2026, 10, 31, 23, 59);
        var pagina = PageRequest.of(0, 10);
        when(repository.filtrar(StatusChamado.ABERTO, Prioridade.ALTA, 1L, 2L,
                3L, 4L, inicio, fim, pagina)).thenReturn(Page.empty(pagina));
        assertTrue(service.listar(StatusChamado.ABERTO, Prioridade.ALTA, 1L, 2L,
                3L, 4L, inicio, fim, pagina).isEmpty());
        verify(repository).filtrar(StatusChamado.ABERTO, Prioridade.ALTA,
                1L, 2L, 3L, 4L, inicio, fim, pagina);
    }

    @Test
    void listaChamadosAbertosPeloSolicitante() {
        var pagina = PageRequest.of(0, 10);
        when(repository.filtrar(null, null, null, null, 1L, null, null, null, pagina))
                .thenReturn(new PageImpl<>(List.of(chamado)));
        assertEquals(1, service.listarPorUsuario(1L, null, pagina).getTotalElements());
    }

    @Test
    void listaChamadosAtribuidosAoTecnicoComStatus() {
        var pagina = PageRequest.of(0, 10);
        when(repository.filtrar(StatusChamado.EM_ATENDIMENTO, null, null, null,
                null, 3L, null, null, pagina)).thenReturn(Page.empty(pagina));
        assertTrue(service.listarPorUsuario(3L, StatusChamado.EM_ATENDIMENTO, pagina).isEmpty());
        verify(repository).filtrar(StatusChamado.EM_ATENDIMENTO, null, null, null,
                null, 3L, null, null, pagina);
    }

    @Test
    void detalheIncluiHistorico() {
        when(repository.findById(10L)).thenReturn(Optional.of(chamado));
        Interacao interacao = new Interacao();
        interacao.setId(20L);
        interacao.setAutor(solicitante);
        interacao.setTexto("Aguardando retorno");
        when(interacoes.findByChamado_IdOrderByDataHoraAscIdAsc(10L)).thenReturn(List.of(interacao));
        var detalhe = service.buscarPorId(10L);
        assertEquals(10L, detalhe.chamado().id());
        assertEquals("Aguardando retorno", detalhe.interacoes().getFirst().texto());
    }

    @Test
    void solicitanteInexistenteNaoAbreChamado() {
        var erro = assertThrows(ResponseStatusException.class,
                () -> service.abrir(abertura(99L, null)));
        assertEquals(404, erro.getStatusCode().value());
        verify(repository, never()).save(any());
    }

    @Test
    void tecnicoSemSetorNaoAssume() {
        tecnico.setSetor(null);
        assertThrows(RegraNegocioException.class,
                () -> service.assumir(10L, new AssumirChamadoDTO(3L)));
        assertEquals(StatusChamado.ABERTO, chamado.getStatus());
        assertNull(chamado.getTecnico());
        verify(repository, never()).save(any());
    }

    @Test
    void tecnicoInexistenteNaoAssume() {
        var erro = assertThrows(ResponseStatusException.class,
                () -> service.assumir(10L, new AssumirChamadoDTO(99L)));
        assertEquals(404, erro.getStatusCode().value());
        verify(repository, never()).save(any());
    }

    @Test
    void autorInexistenteNaoRegistraInteracao() {
        var erro = assertThrows(ResponseStatusException.class,
                () -> service.registrarInteracao(10L, new InteracaoRequestDTO("Teste", 99L)));
        assertEquals(404, erro.getStatusCode().value());
        verify(interacoes, never()).save(any());
    }

    @Test
    void detalheInexistenteRetorna404() {
        var erro = assertThrows(ResponseStatusException.class, () -> service.buscarPorId(99L));
        assertEquals(404, erro.getStatusCode().value());
        verifyNoInteractions(interacoes);
    }

    @Test
    void usuarioInexistenteNaoListaChamados() {
        var erro = assertThrows(ResponseStatusException.class,
                () -> service.listarPorUsuario(99L, null, PageRequest.of(0, 10)));
        assertEquals(404, erro.getStatusCode().value());
        verify(repository, never()).filtrar(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }
}
