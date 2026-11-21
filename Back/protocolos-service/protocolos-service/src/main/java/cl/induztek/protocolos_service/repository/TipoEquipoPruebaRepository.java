package cl.induztek.protocolos_service.repository;

import cl.induztek.protocolos_service.model.TipoEquipoPrueba;
import cl.induztek.protocolos_service.model.TipoEquipoPruebaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoEquipoPruebaRepository extends JpaRepository<TipoEquipoPrueba, TipoEquipoPruebaId> {

    List<TipoEquipoPrueba> findByIdIdTipoEquipo(Integer idTipoEquipo);
}
