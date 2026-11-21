package cl.induztek.protocolos_service.repository;

import cl.induztek.protocolos_service.model.Protocolo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProtocoloRepository extends JpaRepository<Protocolo, Integer> {

    List<Protocolo> findAllByOrderByFechaHoraPruebaDesc();

    List<Protocolo> findByIdVisitaOrderByFechaHoraPruebaDesc(Integer idVisita);

    List<Protocolo> findByIdEquipoOrderByFechaHoraPruebaDesc(Integer idEquipo);

    Optional<Protocolo> findByIdVisitaAndIdEquipo(Integer idVisita, Integer idEquipo);

    Optional<Protocolo> findFirstByNumeroStartingWithOrderByNumeroDesc(String prefijo);
}
