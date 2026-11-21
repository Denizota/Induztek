package cl.induztek.protocolos_service.repository;

import cl.induztek.protocolos_service.model.Medicion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicionRepository extends JpaRepository<Medicion, Integer> {

    List<Medicion> findByIdPruebaOrderByIdAsc(Integer idPrueba);

    void deleteByIdPrueba(Integer idPrueba);
}
