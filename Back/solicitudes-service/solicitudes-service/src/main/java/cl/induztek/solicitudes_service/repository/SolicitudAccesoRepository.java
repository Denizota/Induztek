package cl.induztek.solicitudes_service.repository;

import cl.induztek.solicitudes_service.model.SolicitudAcceso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitudAccesoRepository extends JpaRepository<SolicitudAcceso, Integer> {

    List<SolicitudAcceso> findByEstadoOrderByIdAsc(String estado);

    List<SolicitudAcceso> findByTipoOrderByIdAsc(String tipo);

    List<SolicitudAcceso> findByEstadoAndTipoOrderByIdAsc(String estado, String tipo);

    Optional<SolicitudAcceso> findFirstByCorreoAndEstado(String correo, String estado);

    long countByEstado(String estado);
}
