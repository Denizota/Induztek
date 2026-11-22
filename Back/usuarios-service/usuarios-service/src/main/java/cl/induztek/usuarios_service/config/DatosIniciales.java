package cl.induztek.usuarios_service.config;

import cl.induztek.usuarios_service.model.Usuario;
import cl.induztek.usuarios_service.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        String clave = Seguridad.hashear("Induztek2026");

        usuarioRepository.saveAll(
                List.of(
                        empresa(
                                "76.482.915-8",
                                "Envasadora del Pacífico S.A.",
                                "Agroindustria"
                        ),
                        empresa(
                                "96.731.540-0",
                                "Sanitaria Valle Central S.A.",
                                "Servicios sanitarios"
                        ),
                        empresa(
                                "77.105.362-9",
                                "Minera Cerro Alto SpA",
                                "Minería"
                        ),
                        empresa(
                                "76.903.218-5",
                                "Clínica Los Aromos Ltda.",
                                "Salud"
                        ),
                        empresa(
                                "99.524.170-6",
                                "Cementos Lomas Blancas S.A.",
                                "Construcción"
                        ),
                        empresa(
                                "78.214.635-1",
                                "Data Center Andes Norte SpA",
                                "Tecnología"
                        ),

                        persona(
                                clave,
                                2,
                                "16.482.905-7",
                                "Thomas Peralta S.",
                                "tperalta@induztek.ejemplo"
                        ),
                        persona(
                                clave,
                                3,
                                "14.993.210-0",
                                "Joaquín Dupre",
                                "jdupre@induztek.ejemplo"
                        ),
                        persona(
                                clave,
                                4,
                                "17.650.348-3",
                                "Víctor Isidro R.",
                                "visidro@envpacifico.ejemplo"
                        ),
                        persona(
                                clave,
                                1,
                                "17.284.519-3",
                                "Matías Godoy Herrera",
                                "mgodoy@induztek.ejemplo"
                        ),
                        persona(
                                clave,
                                1,
                                "19.412.087-7",
                                "Javiera Tapia Muñoz",
                                "jtapia@induztek.ejemplo"
                        ),
                        persona(
                                clave,
                                1,
                                "26.104.583-4",
                                "Carlos Rondón Pérez",
                                "crondon@induztek.ejemplo"
                        ),
                        persona(
                                clave,
                                1,
                                "20.137.648-3",
                                "Diego Valenzuela Ríos",
                                "dvalenzuela@induztek.ejemplo"
                        ),
                        persona(
                                clave,
                                2,
                                "15.906.334-8",
                                "Andrea Contreras Silva",
                                "acontreras@induztek.ejemplo"
                        ),
                        persona(
                                clave,
                                2,
                                "16.553.270-8",
                                "Sebastián Morales Gutiérrez",
                                "smorales@induztek.ejemplo"
                        ),
                        persona(
                                clave,
                                3,
                                "13.871.905-7",
                                "Ricardo Espinoza Lagos",
                                "respinoza@induztek.ejemplo"
                        ),
                        persona(
                                clave,
                                4,
                                "14.226.791-8",
                                "Marcela Fuentes Rojas",
                                "mfuentes@envpacifico.ejemplo"
                        ),
                        persona(
                                clave,
                                4,
                                "18.390.452-3",
                                "Tomás Leiva Araya",
                                "tleiva@losaromos.ejemplo"
                        ),
                        persona(
                                clave,
                                4,
                                "12.645.308-6",
                                "Patricio Navarro Soto",
                                "pnavarro@cerroalto.ejemplo"
                        )
                )
        );

        Usuario victor = usuarioRepository.findById(9).orElseThrow();
        victor.setIdEmpresa(1);
        victor.setCargo("Jefe de Planta");
        victor.setBio("Jefe de planta. Reviso los protocolos y recibo las actas de cada visita técnica.");
        victor.setDesde(2021);
        victor.setCalificacion(4);
        usuarioRepository.save(victor);

        Usuario marcela = usuarioRepository.findById(17).orElseThrow();
        marcela.setIdEmpresa(1);
        marcela.setCargo("Jefa de Mantenimiento");
        usuarioRepository.save(marcela);

        Usuario tomas = usuarioRepository.findById(18).orElseThrow();
        tomas.setIdEmpresa(4);
        tomas.setCargo("Supervisor Eléctrico");
        usuarioRepository.save(tomas);

        Usuario patricio = usuarioRepository.findById(19).orElseThrow();
        patricio.setIdEmpresa(3);
        patricio.setCargo("Jefe de Mantenimiento Eléctrico");
        usuarioRepository.save(patricio);

        Usuario thomas = usuarioRepository.findById(7).orElseThrow();
        thomas.setBio(
                "Técnico en pruebas eléctricas. Trabajo principalmente con transformadores y celdas de " +
                "media tensión en plantas industriales de la zona centro-sur."
        );
        thomas.setEspecialidades("Transformadores, Celdas MT, Reles, Interruptores");
        thomas.setFormacion("Técnico en electricidad industrial");
        thomas.setCertificaciones("SEC clase B");
        thomas.setDesde(2019);
        thomas.setCalificacion(5);
        usuarioRepository.save(thomas);

        Usuario joaquin = usuarioRepository.findById(8).orElseThrow();
        joaquin.setBio(
                "Ingeniero a cargo de las pruebas de alto voltaje en terreno y de la revisión de los protocolos."
        );
        joaquin.setEspecialidades("Transformadores, Reles, Mediciones parciales");
        joaquin.setFormacion("Ingeniero eléctrico, mención potencia");
        joaquin.setCertificaciones("SEC clase A");
        joaquin.setDesde(2016);
        joaquin.setCalificacion(5);
        usuarioRepository.save(joaquin);
    }

    private Usuario empresa(String rut, String nombre, String giro) {
        return Usuario.builder()
                .idRol(4)
                .rut(rut)
                .nombre(nombre)
                .giro(giro)
                .telefono("")
                .cargo("")
                .bio("")
                .especialidades("")
                .formacion("")
                .certificaciones("")
                .activo(true)
                .build();
    }

    private Usuario persona(
            String clave,
            Integer idRol,
            String rut,
            String nombre,
            String correo
    ) {
        return Usuario.builder()
                .idRol(idRol)
                .rut(rut)
                .nombre(nombre)
                .correo(correo)
                .passwordHash(clave)
                .telefono("")
                .cargo("")
                .giro("")
                .bio("")
                .especialidades("")
                .formacion("")
                .certificaciones("")
                .activo(true)
                .build();
    }
}