package cl.induztek.equipos_service.repository;

import cl.induztek.equipos_service.model.Instalacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstalacionRepository extends JpaRepository<Instalacion, Integer> {

    List<Instalacion> findByIdUsuarioClienteOrderByNombreAsc(Integer idUsuarioCliente);
}
