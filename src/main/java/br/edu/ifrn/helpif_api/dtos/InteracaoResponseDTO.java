package br.edu.ifrn.helpif_api.dtos;

import br.edu.ifrn.helpif_api.entities.Interacao;
import java.time.LocalDateTime;

public record InteracaoResponseDTO(Long id, String texto, LocalDateTime dataHora, Long autorId) {
    public InteracaoResponseDTO(Interacao interacao) {
        this(interacao.getId(), interacao.getTexto(),
                interacao.getDataHora(), interacao.getAutor().getId());
    }
}
