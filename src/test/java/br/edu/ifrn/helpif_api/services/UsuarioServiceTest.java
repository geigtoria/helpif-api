package br.edu.ifrn.helpif_api.services;

import br.edu.ifrn.helpif_api.dtos.UsuarioRequestDTO;
import br.edu.ifrn.helpif_api.entities.*;
import br.edu.ifrn.helpif_api.exceptions.RegraNegocioException;
import br.edu.ifrn.helpif_api.repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {
    private final UsuarioRepository repository = mock(UsuarioRepository.class);
    private final SetorRepository setores = mock(SetorRepository.class);
    private final UsuarioService service = new UsuarioService(repository, setores);

    private UsuarioRequestDTO dados(TipoUsuario tipo, Long setorId) {
        return new UsuarioRequestDTO(" Ana ", "ana@ifrn.edu.br", "123", tipo, setorId);
    }

    @Test
    void rejeitaEmailDuplicado() {
        when(repository.existsByEmailIgnoreCase("ana@ifrn.edu.br")).thenReturn(true);
        assertThrows(RegraNegocioException.class,
                () -> service.criar(dados(TipoUsuario.SOLICITANTE, null)));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void tecnicoPrecisaDeSetor() {
        assertThrows(RegraNegocioException.class,
                () -> service.criar(dados(TipoUsuario.TECNICO, null)));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void rejeitaSetorInexistente() {
        when(setores.findById(9L)).thenReturn(Optional.empty());
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> service.criar(dados(TipoUsuario.TECNICO, 9L)));
        assertEquals(404, erro.getStatusCode().value());
    }

    @Test
    void criaSolicitanteSemSetor() {
        when(repository.saveAndFlush(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });
        var resposta = service.criar(dados(TipoUsuario.SOLICITANTE, null));
        assertEquals("Ana", resposta.nome());
        assertEquals(TipoUsuario.SOLICITANTE, resposta.tipo());
        assertNull(resposta.setorId());
    }

    @Test
    void atualizacaoVerificaEmailDeOutroUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.existsByEmailIgnoreCaseAndIdNot("ana@ifrn.edu.br", 1L))
                .thenReturn(true);
        assertThrows(RegraNegocioException.class,
                () -> service.atualizar(1L, dados(TipoUsuario.SOLICITANTE, null)));
        verify(repository, never()).saveAndFlush(any());
    }
}
