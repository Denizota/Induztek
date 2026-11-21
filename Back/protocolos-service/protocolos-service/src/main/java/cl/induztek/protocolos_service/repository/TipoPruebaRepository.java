package cl.induztek.protocolos_service.repository;

import cl.induztek.protocolos_service.model.TipoPrueba;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoPruebaRepository extends JpaRepository<TipoPrueba, Integer> {

    Optional<TipoPrueba> findByCodigo(String codigo);
}
