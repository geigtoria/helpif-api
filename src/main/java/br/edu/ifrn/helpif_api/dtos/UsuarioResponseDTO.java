package br.edu.ifrn.helpif_api.dtos;

import br.edu.ifrn.helpif_api.entities.TipoUsuario;
import br.edu.ifrn.helpif_api.entities.Usuario;

public record UsuarioResponseDTO(
        Long id, String nome, String email, String matricula,
        TipoUsuario tipo, Long setorId
) {
    public UsuarioResponseDTO(Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getMatricula(), usuario.getTipo(),
                usuario.getSetor() == null ? null : usuario.getSetor().getId());
    }
}
