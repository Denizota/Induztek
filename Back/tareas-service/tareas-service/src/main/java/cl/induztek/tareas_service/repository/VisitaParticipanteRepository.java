package cl.induztek.tareas_service.repository;

import cl.induztek.tareas_service.model.VisitaParticipante;
import cl.induztek.tareas_service.model.VisitaParticipanteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitaParticipanteRepository extends JpaRepository<VisitaParticipante, VisitaParticipanteId> {

    List<VisitaParticipante> findByIdIdVisitaOrderByIdIdUsuarioAsc(Integer idVisita);

    boolean existsByIdIdVisitaAndIdIdUsuario(Integer idVisita, Integer idUsuario);

    void deleteByIdIdVisitaAndIdIdUsuario(Integer idVisita, Integer idUsuario);
}
