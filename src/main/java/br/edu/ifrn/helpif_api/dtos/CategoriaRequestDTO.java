package br.edu.ifrn.helpif_api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoriaRequestDTO(

        @NotBlank(message = "Campo obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "Campo obrigatório")
        String descricao,

        @NotNull(message = "Campo obrigatório")
        Long setorId

) {}
