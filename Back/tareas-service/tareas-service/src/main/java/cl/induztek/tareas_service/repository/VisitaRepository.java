package cl.induztek.tareas_service.repository;

import cl.induztek.tareas_service.model.Visita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VisitaRepository extends JpaRepository<Visita, Integer> {

    List<Visita> findAllByOrderByFechaDesc();

    List<Visita> findByIdInstalacionOrderByFechaDesc(Integer idInstalacion);

    List<Visita> findByFechaHoraCierreIsNullOrderByIdAsc();

    List<Visita> findByIdInstalacionAndFechaHoraCierreIsNullOrderByIdAsc(Integer idInstalacion);

    Optional<Visita> findFirstByIdInstalacionAndFechaHoraCierreIsNullOrderByIdDesc(Integer idInstalacion);

    @Query("SELECT v FROM Visita v WHERE v.idIngenieroCargo = :idIngeniero " +
            "OR v.id IN (SELECT p.id.idVisita FROM VisitaParticipante p WHERE p.id.idUsuario = :idIngeniero) " +
            "ORDER BY v.fecha DESC")
    List<Visita> buscarPorIngeniero(@Param("idIngeniero") Integer idIngeniero);
}
