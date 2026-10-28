package br.edu.ifrn.helpif_api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetorRequestDTO(

        @NotBlank(message = "Campo obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "Campo obrigatório")
        @Size(max = 20, message = "A sigla deve ter no máximo 20 caracteres")
        String sigla

) {}
