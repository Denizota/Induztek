package cl.induztek.equipos_service.repository;

import cl.induztek.equipos_service.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipoRepository extends JpaRepository<Equipo, Integer> {

    List<Equipo> findByIdInstalacionOrderByTagAsc(Integer idInstalacion);

    Optional<Equipo> findByIdInstalacionAndTag(Integer idInstalacion, String tag);
}
