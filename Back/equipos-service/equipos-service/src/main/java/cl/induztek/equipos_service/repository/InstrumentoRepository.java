package cl.induztek.equipos_service.repository;

import cl.induztek.equipos_service.model.Instrumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstrumentoRepository extends JpaRepository<Instrumento, Integer> {

    List<Instrumento> findAllByOrderByFechaVenceAsc();

    List<Instrumento> findByFechaVenceLessThanOrderByFechaVenceAsc(String fecha);
}
