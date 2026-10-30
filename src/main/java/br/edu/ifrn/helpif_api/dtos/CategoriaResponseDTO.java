package br.edu.ifrn.helpif_api.dtos;

import br.edu.ifrn.helpif_api.entities.Categoria;

public record CategoriaResponseDTO(
        Long id,
        String nome,
        String descricao,
        Long setorId
) {
    public CategoriaResponseDTO(Categoria categoria) {
        this(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.getSetor().getId()
        );
    }
}
