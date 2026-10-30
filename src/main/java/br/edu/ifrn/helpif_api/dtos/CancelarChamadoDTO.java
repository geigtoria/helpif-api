package br.edu.ifrn.helpif_api.dtos;

import jakarta.validation.constraints.NotNull;

public record CancelarChamadoDTO(@NotNull(message = "Campo obrigatório") Long solicitanteId) {}
