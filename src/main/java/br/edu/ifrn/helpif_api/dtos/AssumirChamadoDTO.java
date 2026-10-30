package br.edu.ifrn.helpif_api.dtos;

import jakarta.validation.constraints.NotNull;

public record AssumirChamadoDTO(@NotNull(message = "Campo obrigatório") Long tecnicoId) {}
