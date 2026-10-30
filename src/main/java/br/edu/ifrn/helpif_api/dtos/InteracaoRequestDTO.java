package br.edu.ifrn.helpif_api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InteracaoRequestDTO(
        @NotBlank(message = "Campo obrigatório") String texto,
        @NotNull(message = "Campo obrigatório") Long autorId
) {}
