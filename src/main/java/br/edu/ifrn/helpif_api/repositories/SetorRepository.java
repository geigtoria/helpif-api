package br.edu.ifrn.helpif_api.repositories;

import br.edu.ifrn.helpif_api.entities.Setor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SetorRepository extends JpaRepository<Setor, Long> {

    Page<Setor> findByNomeContainingIgnoreCase(
            String nome, Pageable pageable);

    boolean existsBySiglaIgnoreCase(String sigla);

    boolean existsBySiglaIgnoreCaseAndIdNot(
            String sigla, Long id);
}
