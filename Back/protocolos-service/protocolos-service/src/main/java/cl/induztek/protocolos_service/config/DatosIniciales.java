package cl.induztek.protocolos_service.config;

import cl.induztek.protocolos_service.model.TipoEquipoPrueba;
import cl.induztek.protocolos_service.model.TipoEquipoPruebaId;
import cl.induztek.protocolos_service.model.TipoPrueba;
import cl.induztek.protocolos_service.repository.TipoEquipoPruebaRepository;
import cl.induztek.protocolos_service.repository.TipoPruebaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final TipoPruebaRepository tipoPruebaRepository;
    private final TipoEquipoPruebaRepository tipoEquipoPruebaRepository;

    @Override
    public void run(String... args) {
        if (tipoPruebaRepository.count() > 0) {
            return;
        }

        tipoPruebaRepository.saveAll(List.of(
                tipo(1, "AISLACION", "Resistencia de aislación"),
                tipo(2, "TTR", "Relación de transformación"),
                tipo(3, "DEVANADOS", "Resistencia de devanados"),
                tipo(4, "CONTACTOS", "Resistencia de contactos"),
                tipo(5, "OPERACION_RELE", "Prueba de operación (inyección secundaria)")
        ));

        tipoEquipoPruebaRepository.saveAll(List.of(
                par(1, 1), par(1, 2), par(1, 3), par(2, 1), par(2, 4),
                par(3, 1), par(3, 4), par(4, 1), par(5, 5)
        ));
    }

    private TipoPrueba tipo(Integer id, String codigo, String nombre) {
        return TipoPrueba.builder()
                .id(id)
                .codigo(codigo)
                .nombre(nombre)
                .build();
    }

    private TipoEquipoPrueba par(Integer idTipoEquipo, Integer idTipoPrueba) {
        return TipoEquipoPrueba.builder()
                .id(new TipoEquipoPruebaId(idTipoEquipo, idTipoPrueba))
                .build();
    }
}
