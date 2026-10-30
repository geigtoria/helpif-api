package br.edu.ifrn.helpif_api.dtos;

import br.edu.ifrn.helpif_api.entities.Prioridade;
import jakarta.validation.constraints.*;

public record ChamadoRequestDTO(
        @NotBlank(message = "Campo obrigatório") @Size(max = 255) String titulo,
        @NotBlank(message = "Campo obrigatório") String descricao,
        @NotBlank(message = "Campo obrigatório") @Size(max = 255) String local,
        @Size(max = 50) String patrimonio,
        Prioridade prioridade,
        @NotNull(message = "Campo obrigatório") Long solicitanteId,
        @NotNull(message = "Campo obrigatório") Long categoriaId
) {}
