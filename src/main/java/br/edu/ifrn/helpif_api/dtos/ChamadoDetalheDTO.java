package br.edu.ifrn.helpif_api.dtos;

import java.util.List;

public record ChamadoDetalheDTO(ChamadoResponseDTO chamado, List<InteracaoResponseDTO> interacoes) {}
