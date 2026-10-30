package br.edu.ifrn.helpif_api.services;

import br.edu.ifrn.helpif_api.dtos.SetorRequestDTO;
import br.edu.ifrn.helpif_api.entities.Setor;
import br.edu.ifrn.helpif_api.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SetorServiceTest {
    private final SetorRepository repository = mock(SetorRepository.class);
    private final ChamadoRepository chamados = mock(ChamadoRepository.class);
    private final SetorService service = new SetorService(repository, chamados);

    private Setor setor() {
        Setor setor = new Setor();
        setor.setId(1L);
        setor.setNome("Tecnologia da Informação");
        setor.setSigla("COTIC");
        return setor;
    }

    @Test
    void criaSetorComSiglaDisponivel() {
        when(repository.save(any(Setor.class))).thenAnswer(i -> {
            Setor salvo = i.getArgument(0);
            salvo.setId(1L);
            return salvo;
        });
        var resposta = service.criar(new SetorRequestDTO("Tecnologia da Informação", "COTIC"));
        assertEquals(1L, resposta.id());
        assertEquals("Tecnologia da Informação", resposta.nome());
        assertEquals("COTIC", resposta.sigla());
    }

    @Test
    void siglaDuplicadaRetorna409SemSalvar() {
        when(repository.existsBySiglaIgnoreCase("cotic")).thenReturn(true);
        var erro = assertThrows(ResponseStatusException.class,
                () -> service.criar(new SetorRequestDTO("Outro setor", "cotic")));
        assertEquals(409, erro.getStatusCode().value());
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void listaSemFiltroPreservandoMetadados(String nome) {
        var pagina = PageRequest.of(1, 10, Sort.by("nome"));
        when(repository.findAll(pagina)).thenReturn(new PageImpl<>(List.of(setor()), pagina, 11));
        var resposta = service.listar(nome, pagina);
        assertEquals(1, resposta.getNumber());
        assertEquals(10, resposta.getSize());
        assertEquals(11, resposta.getTotalElements());
        assertEquals(2, resposta.getTotalPages());
        assertEquals("COTIC", resposta.getContent().getFirst().sigla());
    }

    @Test
    void filtraNomeComPaginacao() {
        var pagina = PageRequest.of(0, 10);
        when(repository.findByNomeContainingIgnoreCase("tecnologia", pagina))
                .thenReturn(new PageImpl<>(List.of(setor())));
        assertEquals(1, service.listar("tecnologia", pagina).getTotalElements());
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    void buscaSetorExistente() {
        when(repository.findById(1L)).thenReturn(Optional.of(setor()));
        assertEquals("COTIC", service.buscarPorId(1L).sigla());
    }

    @Test
    void buscaInexistenteRetorna404() {
        var erro = assertThrows(ResponseStatusException.class, () -> service.buscarPorId(99L));
        assertEquals(404, erro.getStatusCode().value());
    }

    @Test
    void atualizaMantendoPropriaSigla() {
        when(repository.findById(1L)).thenReturn(Optional.of(setor()));
        when(repository.save(any(Setor.class))).thenAnswer(i -> i.getArgument(0));
        var resposta = service.atualizar(1L, new SetorRequestDTO("Novo nome", "COTIC"));
        assertEquals("Novo nome", resposta.nome());
        assertEquals("COTIC", resposta.sigla());
        verify(repository).existsBySiglaIgnoreCaseAndIdNot("COTIC", 1L);
    }

    @Test
    void atualizacaoComSiglaDeOutroSetorRetorna409() {
        Setor original = setor();
        when(repository.findById(1L)).thenReturn(Optional.of(original));
        when(repository.existsBySiglaIgnoreCaseAndIdNot("MANUT", 1L)).thenReturn(true);
        var erro = assertThrows(ResponseStatusException.class,
                () -> service.atualizar(1L, new SetorRequestDTO("Manutenção", "MANUT")));
        assertEquals(409, erro.getStatusCode().value());
        assertEquals("COTIC", original.getSigla());
        verify(repository, never()).save(any());
    }

    @Test
    void atualizacaoInexistenteRetorna404() {
        var erro = assertThrows(ResponseStatusException.class,
                () -> service.atualizar(99L, new SetorRequestDTO("Teste", "TST")));
        assertEquals(404, erro.getStatusCode().value());
        verify(repository, never()).save(any());
    }

    @Test
    void excluiSetorSemChamados() {
        Setor setor = setor();
        when(repository.findById(1L)).thenReturn(Optional.of(setor));
        service.excluir(1L);
        verify(repository).delete(setor);
        verify(repository).flush();
    }

    @Test
    void setorComChamadosNaoPodeSerExcluido() {
        when(repository.findById(1L)).thenReturn(Optional.of(setor()));
        when(chamados.existsByCategoria_Setor_Id(1L)).thenReturn(true);
        var erro = assertThrows(ResponseStatusException.class, () -> service.excluir(1L));
        assertEquals(409, erro.getStatusCode().value());
        verify(repository, never()).delete(any());
        verify(repository, never()).flush();
    }

    @Test
    void exclusaoInexistenteRetorna404() {
        var erro = assertThrows(ResponseStatusException.class, () -> service.excluir(99L));
        assertEquals(404, erro.getStatusCode().value());
        verify(repository, never()).delete(any());
    }
}
