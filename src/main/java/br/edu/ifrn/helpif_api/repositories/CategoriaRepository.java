
package br.edu.ifrn.helpif_api.repositories;

import br.edu.ifrn.helpif_api.entities.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository
        extends JpaRepository<Categoria, Long> {

    Page<Categoria> findByNomeContainingIgnoreCase(
            String nome, Pageable pageable);

    Page<Categoria> findBySetor_Id(
            Long setorId, Pageable pageable);

    Page<Categoria> findByNomeContainingIgnoreCaseAndSetor_Id(
            String nome, Long setorId, Pageable pageable);

    boolean existsBySetor_Id(Long setorId);
}
