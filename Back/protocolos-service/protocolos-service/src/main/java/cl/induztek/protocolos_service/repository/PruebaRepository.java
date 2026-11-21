package cl.induztek.protocolos_service.repository;

import cl.induztek.protocolos_service.model.Prueba;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PruebaRepository extends JpaRepository<Prueba, Integer> {

    List<Prueba> findByIdProtocoloOrderByIdAsc(Integer idProtocolo);

    Optional<Prueba> findFirstByIdProtocoloAndCodigoTipoPruebaAndElementoOrderByIdDesc(
            Integer idProtocolo, String codigoTipoPrueba, String elemento);
}
