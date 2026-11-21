package cl.induztek.solicitudes_service.service;

import cl.induztek.solicitudes_service.config.Seguridad;
import cl.induztek.solicitudes_service.dto.SolicitudRequest;
import cl.induztek.solicitudes_service.dto.SolicitudResponse;
import cl.induztek.solicitudes_service.model.SolicitudAcceso;
import cl.induztek.solicitudes_service.repository.SolicitudAccesoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private static final String PENDIENTE = "PENDIENTE";
    private static final List<String> TIPOS = List.of("INGRESO", "BAJA", "CAMBIO");
    private static final List<String> ESTADOS = List.of("PENDIENTE", "APROBADA", "RECHAZADA", "CREADA");

    private final SolicitudAccesoRepository solicitudRepository;

    public List<SolicitudResponse> listar(String estado, String tipo) {
        List<SolicitudAcceso> solicitudes;

        if (estado != null && !estado.isBlank() && tipo != null && !tipo.isBlank()) {
            solicitudes = solicitudRepository.findByEstadoAndTipoOrderByIdAsc(estado, tipo);
        } else if (estado != null && !estado.isBlank()) {
            solicitudes = solicitudRepository.findByEstadoOrderByIdAsc(estado);
        } else if (tipo != null && !tipo.isBlank()) {
            solicitudes = solicitudRepository.findByTipoOrderByIdAsc(tipo);
        } else {
            solicitudes = solicitudRepository.findAll();
        }

        return solicitudes.stream().map(this::aRespuesta).toList();
    }

    public SolicitudResponse obtener(Integer id) {
        return aRespuesta(buscar(id));
    }

    public long pendientes() {
        return solicitudRepository.countByEstado(PENDIENTE);
    }

    public SolicitudResponse crear(SolicitudRequest datos) {
        if (vacio(datos.nombre()) || vacio(datos.apellido()) || vacio(datos.correo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre, apellido y correo son obligatorios");
        }
        if (vacio(datos.justificacion())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La solicitud necesita una justificación");
        }

        String tipo = datos.tipo() != null ? datos.tipo() : "INGRESO";
        if (!TIPOS.contains(tipo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de solicitud no existe");
        }
        if (tipo.equals("INGRESO") && (datos.contrasena() == null || datos.contrasena().length() < 6)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener mínimo 6 caracteres");
        }
        if (solicitudRepository.findFirstByCorreoAndEstado(datos.correo(), PENDIENTE).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya hay una solicitud pendiente con ese correo");
        }

        SolicitudAcceso solicitud = SolicitudAcceso.builder()
                .nombre(datos.nombre())
                .apellido(datos.apellido())
                .pais(datos.pais() != null ? datos.pais() : "")
                .ciudad(datos.ciudad() != null ? datos.ciudad() : "")
                .correo(datos.correo())
                .passwordHash(Seguridad.hashear(datos.contrasena()))
                .justificacion(datos.justificacion())
                .estado(PENDIENTE)
                .tipo(tipo)
                .idUsuario(datos.idUsuario())
                .campo(datos.campo() != null ? datos.campo() : "")
                .valor(datos.valor() != null ? datos.valor() : "")
                .build();
        return aRespuesta(solicitudRepository.save(solicitud));
    }

    public SolicitudResponse cambiarEstado(Integer id, String estado) {
        if (estado == null || !ESTADOS.contains(estado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado indicado no existe");
        }
        SolicitudAcceso solicitud = buscar(id);
        solicitud.setEstado(estado);
        return aRespuesta(solicitudRepository.save(solicitud));
    }

    public void eliminar(Integer id) {
        solicitudRepository.delete(buscar(id));
    }

    private SolicitudAcceso buscar(Integer id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
    }

    private SolicitudResponse aRespuesta(SolicitudAcceso solicitud) {
        return new SolicitudResponse(
                solicitud.getId(),
                solicitud.getNombre(),
                solicitud.getApellido(),
                solicitud.getPais(),
                solicitud.getCiudad(),
                solicitud.getCorreo(),
                solicitud.getJustificacion(),
                solicitud.getEstado(),
                solicitud.getTipo(),
                solicitud.getIdUsuario(),
                solicitud.getCampo(),
                solicitud.getValor()
        );
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
