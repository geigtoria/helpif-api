package br.edu.ifrn.helpif_api.repositories;

import br.edu.ifrn.helpif_api.entities.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    boolean existsByCategoria_Setor_Id(Long setorId);
    boolean existsByCategoria_Id(Long categoriaId);

}
