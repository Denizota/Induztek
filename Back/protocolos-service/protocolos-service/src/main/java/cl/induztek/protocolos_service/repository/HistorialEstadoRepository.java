package cl.induztek.protocolos_service.repository;

import cl.induztek.protocolos_service.model.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Integer> {

    List<HistorialEstado> findByIdProtocoloOrderByIdAsc(Integer idProtocolo);
}
