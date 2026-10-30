package br.edu.ifrn.helpif_api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResolverChamadoDTO(
        @NotNull(message = "Campo obrigatório") Long tecnicoId,
        @NotBlank(message = "Campo obrigatório") String solucao
) {}
