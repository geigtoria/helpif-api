
package br.edu.ifrn.helpif_api.dtos;

import br.edu.ifrn.helpif_api.entities.Setor;

public record SetorResponseDTO(
        Long id,
        String nome,
        String sigla
) {
    public SetorResponseDTO(Setor setor) {
        this(
                setor.getId(),
                setor.getNome(),
                setor.getSigla()
        );
    }
}
