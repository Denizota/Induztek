package cl.induztek.equipos_service.config;

import cl.induztek.equipos_service.model.Equipo;
import cl.induztek.equipos_service.model.Instalacion;
import cl.induztek.equipos_service.model.Instrumento;
import cl.induztek.equipos_service.model.TipoEquipo;
import cl.induztek.equipos_service.repository.EquipoRepository;
import cl.induztek.equipos_service.repository.InstalacionRepository;
import cl.induztek.equipos_service.repository.InstrumentoRepository;
import cl.induztek.equipos_service.repository.TipoEquipoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final InstalacionRepository instalacionRepository;
    private final TipoEquipoRepository tipoEquipoRepository;
    private final EquipoRepository equipoRepository;
    private final InstrumentoRepository instrumentoRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (instalacionRepository.count() > 0) {
            return;
        }
        
        
        instalacionRepository.saveAll(List.of(
                instalacion(1, "Planta Lampa", "Sector industrial", "Lampa",
                        "Inducción obligatoria de 30 min"),
                instalacion(2, "Planta Elevadora Sur", "Sector rural poniente", "San Bernardo",
                        "Sala eléctrica subterránea, sin señal"),
                instalacion(3, "Faena Til Til", "Acceso por Ruta 5 Norte", "Til Til",
                        "Sin señal en toda la faena"),
                instalacion(4, "Edificio Clínico", "Zona urbana", "La Florida",
                        "Cortes solo domingos 06:00-10:00"),
                instalacion(5, "Planta Molienda", "Sector precordillera", "Puente Alto",
                        "Uso obligatorio de respirador"),
                instalacion(6, "Site Huechuraba", "Parque empresarial", "Huechuraba",
                        "Acompañamiento permanente del cliente")
        ));

        tipoEquipoRepository.saveAll(List.of(
                tipo(1, "Transformador", "PRIMARIO"),
                tipo(2, "Interruptor", "PRIMARIO"),
                tipo(3, "Celda MT", "PRIMARIO"),
                tipo(4, "Cable de poder", "PRIMARIO"),
                tipo(5, "Relé de protección", "SECUNDARIO")
        ));

        equipoRepository.saveAll(List.of(
                equipo(1, "TR-01", 1, "Rhona", "RH-18733", 2012,
                        1500.0, 12.0, 0.4, null, null, ""),
                equipo(1, "TR-02", 1, "Rhona", "RH-21904", 2017,
                        1000.0, 12.0, 0.4, null, null, ""),
                equipo(2, "TR-A", 1, "Tusan", "TS-5521", 2009,
                        2500.0, 23.0, 0.4, null, null, ""),
                equipo(4, "CEL-MT-03", 3, "Ormazabal", "OR-77812", 2018,
                        null, 12.0, null, 630.0, 60.0, ""),
                equipo(3, "INT-52-1", 2, "Schneider", "SE-449021", 2015,
                        null, 23.0, null, 1250.0, 45.0, ""),
                equipo(5, "REL-50/51-A", 5, "SEL", "SEL-751-30219", 2020,
                        null, null, null, null, null, "Sobrecorriente, ajuste 5 A"),
                equipo(6, "CAB-MT-02", 4, "Nexans", "NX-2201-B", 2019,
                        null, 15.0, null, null, null, "3x240 mm², 180 m"),
                equipo(3, "TR-SS-01", 1, "ABB", "ABB-1180442", 2011,
                        5000.0, 23.0, 6.6, null, null, "")
        ));

        instrumentoRepository.saveAll(List.of(
                instrumento("Megóhmetro 5 kV", "Megger", "MIT525", "MIT-0045821",
                        "2026-03-15", "2027-03-15"),
                instrumento("Megóhmetro 10 kV", "Megger", "MIT1025", "MIT-0071233",
                        "2025-10-10", "2026-10-10"),
                instrumento("Medidor de relación (TTR)", "Megger", "TTR300", "TTR-30044",
                        "2026-06-02", "2027-06-02"),
                instrumento("Micro-ohmímetro", "Megger", "DLRO10X", "DLR-10882",
                        "2026-08-20", "2027-08-20"),
                instrumento("Equipo de inyección secundaria", "Omicron", "CMC 356", "CMC-HB411",
                        "2026-01-05", "2027-01-05")
        ));
    }

    private Instalacion instalacion(Integer cliente, String nombre, String ubicacion,
                                    String comuna, String observaciones) {
        return Instalacion.builder()
                .idUsuarioCliente(cliente)
                .nombre(nombre)
                .ubicacion(ubicacion)
                .comuna(comuna)
                .observacionesAcceso(observaciones)
                .build();
    }

    private TipoEquipo tipo(Integer id, String nombre, String clasificacion) {
        return TipoEquipo.builder()
                .id(id)
                .nombre(nombre)
                .clasificacion(clasificacion)
                .build();
    }

    private Equipo equipo(Integer instalacion, String tag, Integer tipo, String marca,
                          String serie, Integer anio, Double potenciaKva, Double tensionPrimariaKv,
                          Double tensionSecundariaKv, Double corrienteNominalA,
                          Double maxContactosUohm, String observaciones) {
        return Equipo.builder()
                .idInstalacion(instalacion)
                .tag(tag)
                .idTipoEquipo(tipo)
                .marca(marca)
                .modelo(serie)
                .numeroSerie(serie)
                .anioFabricacion(anio)
                .potenciaKva(potenciaKva)
                .tensionPrimariaKv(tensionPrimariaKv)
                .tensionSecundariaKv(tensionSecundariaKv)
                .corrienteNominalA(corrienteNominalA)
                .maxContactosUohm(maxContactosUohm)
                .observaciones(observaciones)
                .build();
    }

    private Instrumento instrumento(String nombre, String marca, String modelo,
                                    String serie, String ultimaCal, String vence) {
        return Instrumento.builder()
                .nombre(nombre)
                .marca(marca)
                .modelo(modelo)
                .numeroSerie(serie)
                .fechaUltimaCal(ultimaCal)
                .fechaVence(vence)
                .build();
    }
}
