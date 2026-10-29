package br.edu.ifrn.helpif_api.dtos;

import br.edu.ifrn.helpif_api.entities.TipoUsuario;
import jakarta.validation.constraints.*;

public record UsuarioRequestDTO(
        @NotBlank(message = "Campo obrigatório") @Size(max = 255) String nome,
        @NotBlank(message = "Campo obrigatório") @Email(message = "Email inválido") @Size(max = 255) String email,
        @NotBlank(message = "Campo obrigatório") @Size(max = 255) String matricula,
        @NotNull(message = "Campo obrigatório") TipoUsuario tipo,
        Long setorId
) {}
