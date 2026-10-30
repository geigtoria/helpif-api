package br.edu.ifrn.helpif_api.repositories;

import br.edu.ifrn.helpif_api.entities.Interacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InteracaoRepository extends JpaRepository<Interacao, Long> {
    List<Interacao> findByChamado_IdOrderByDataHoraAscIdAsc(Long chamadoId);
}
