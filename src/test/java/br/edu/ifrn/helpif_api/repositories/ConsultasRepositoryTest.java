package br.edu.ifrn.helpif_api.repositories;

import br.edu.ifrn.helpif_api.entities.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ConsultasRepositoryTest {
    private final UsuarioRepository usuarios;
    private final ChamadoRepository chamados;
    private final EntityManager entityManager;

    @Autowired
    ConsultasRepositoryTest(UsuarioRepository usuarios, ChamadoRepository chamados, EntityManager entityManager) {
        this.usuarios = usuarios;
        this.chamados = chamados;
        this.entityManager = entityManager;
    }

    private Usuario criarSolicitante(String nome) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(UUID.randomUUID() + "@example.com");
        usuario.setMatricula("12345");
        usuario.setTipo(TipoUsuario.SOLICITANTE);
        return usuarios.saveAndFlush(usuario);
    }

    @Test
    void listaUsuariosComNomeNuloECombinaTipoESetor() {
        Usuario usuario = criarSolicitante("RegressaoNomeNulo");
        var pagina = usuarios.filtrar(null, TipoUsuario.SOLICITANTE, null, PageRequest.of(0, 50));
        assertTrue(pagina.stream().anyMatch(u -> u.getId().equals(usuario.getId())));
        assertEquals(1, usuarios.filtrar("regressaonomenulo", TipoUsuario.SOLICITANTE,
                null, PageRequest.of(0, 10)).getTotalElements());
        assertTrue(usuarios.filtrar(null, null, -1L, PageRequest.of(0, 10)).isEmpty());
    }

    @Test
    void listaChamadosSemTecnicoComFiltrosNulosEDatasInclusivas() {
        Usuario usuario = criarSolicitante("SolicitanteConsulta");
        Setor setor = new Setor();
        setor.setNome("Setor consulta");
        setor.setSigla(UUID.randomUUID().toString());
        entityManager.persist(setor);
        Categoria categoria = new Categoria();
        categoria.setNome("Categoria consulta");
        categoria.setDescricao("Teste de filtros");
        categoria.setSetor(setor);
        entityManager.persist(categoria);
        LocalDateTime abertura = LocalDateTime.of(2026, 10, 30, 12, 0);
        Chamado chamado = new Chamado();
        chamado.setTitulo("Consulta sem tecnico");
        chamado.setDescricao("Teste");
        chamado.setLocal("Sala 1");
        chamado.setPrioridade(Prioridade.MEDIA);
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setDataAbertura(abertura);
        chamado.setSolicitante(usuario);
        chamado.setCategoria(categoria);
        chamados.saveAndFlush(chamado);
        entityManager.clear();
        var pagina = chamados.filtrar(null, null, null, null, usuario.getId(), null,
                null, null, PageRequest.of(0, 10));
        assertEquals(1, pagina.getTotalElements());
        assertNull(pagina.getContent().getFirst().getTecnico());
        assertEquals(1, chamados.filtrar(StatusChamado.ABERTO, Prioridade.MEDIA,
                setor.getId(), categoria.getId(), usuario.getId(), null,
                abertura, abertura, PageRequest.of(0, 10)).getTotalElements());
        assertTrue(chamados.filtrar(null, null, null, null, usuario.getId(), null,
                abertura.plusSeconds(1), null, PageRequest.of(0, 10)).isEmpty());
        assertTrue(chamados.filtrar(null, null, null, null, usuario.getId(), null,
                null, abertura.minusSeconds(1), PageRequest.of(0, 10)).isEmpty());
    }
}
