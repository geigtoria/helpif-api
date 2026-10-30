package br.edu.ifrn.helpif_api.repositories;

import br.edu.ifrn.helpif_api.entities.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
    boolean existsByCategoria_Setor_Id(Long setorId);
    boolean existsByCategoria_Id(Long categoriaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Chamado c WHERE c.id = :id")
    Optional<Chamado> buscarParaAtualizar(@Param("id") Long id);

    @Query("""
            SELECT c FROM Chamado c
            WHERE (:status IS NULL OR c.status = :status)
              AND (:prioridade IS NULL OR c.prioridade = :prioridade)
              AND (:setorId IS NULL OR c.categoria.setor.id = :setorId)
              AND (:categoriaId IS NULL OR c.categoria.id = :categoriaId)
              AND (:solicitanteId IS NULL OR c.solicitante.id = :solicitanteId)
              AND (:tecnicoId IS NULL OR c.tecnico.id = :tecnicoId)
              AND c.dataAbertura >= COALESCE(:dataInicio, c.dataAbertura)
              AND c.dataAbertura <= COALESCE(:dataFim, c.dataAbertura)
            """)
    Page<Chamado> filtrar(
            @Param("status") StatusChamado status,
            @Param("prioridade") Prioridade prioridade,
            @Param("setorId") Long setorId,
            @Param("categoriaId") Long categoriaId,
            @Param("solicitanteId") Long solicitanteId,
            @Param("tecnicoId") Long tecnicoId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim, Pageable pageable);
}
