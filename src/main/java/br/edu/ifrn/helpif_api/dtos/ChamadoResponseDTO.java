package br.edu.ifrn.helpif_api.dtos;

import br.edu.ifrn.helpif_api.entities.*;
import java.time.LocalDateTime;

public record ChamadoResponseDTO(
        Long id, String titulo, String descricao, String local, String patrimonio,
        Prioridade prioridade, StatusChamado status,
        LocalDateTime dataAbertura, LocalDateTime dataFechamento, String solucao,
        Long solicitanteId, Long categoriaId, Long tecnicoId
) {
    public ChamadoResponseDTO(Chamado chamado) {
        this(chamado.getId(), chamado.getTitulo(), chamado.getDescricao(),
                chamado.getLocal(), chamado.getPatrimonio(), chamado.getPrioridade(),
                chamado.getStatus(), chamado.getDataAbertura(), chamado.getDataFechamento(),
                chamado.getSolucao(), chamado.getSolicitante().getId(),
                chamado.getCategoria().getId(),
                chamado.getTecnico() == null ? null : chamado.getTecnico().getId());
    }
}
