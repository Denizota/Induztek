package cl.induztek.tareas_service.service;

import cl.induztek.tareas_service.client.ProtocoloClient;
import cl.induztek.tareas_service.dto.CierreRequest;
import cl.induztek.tareas_service.dto.ProtocoloDto;
import cl.induztek.tareas_service.dto.RecepcionResponse;
import cl.induztek.tareas_service.dto.VisitaDetalleResponse;
import cl.induztek.tareas_service.dto.VisitaRequest;
import cl.induztek.tareas_service.dto.VisitaResponse;
import cl.induztek.tareas_service.model.RecepcionActa;
import cl.induztek.tareas_service.model.Visita;
import cl.induztek.tareas_service.model.VisitaParticipante;
import cl.induztek.tareas_service.model.VisitaParticipanteId;
import cl.induztek.tareas_service.repository.RecepcionActaRepository;
import cl.induztek.tareas_service.repository.VisitaParticipanteRepository;
import cl.induztek.tareas_service.repository.VisitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VisitaService {

    private static final String REGISTRADO = "REGISTRADO_TERRENO";
    private static final String ANULADO = "ANULADO";
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final VisitaRepository visitaRepository;
    private final VisitaParticipanteRepository participanteRepository;
    private final RecepcionActaRepository actaRepository;
    private final ProtocoloClient protocoloClient;

    public List<VisitaResponse> listar(Integer idInstalacion, Integer idIngeniero) {
        List<Visita> visitas;

        if (idIngeniero != null) {
            visitas = visitaRepository.buscarPorIngeniero(idIngeniero);
            if (idInstalacion != null) {
                visitas = visitas.stream()
                        .filter(visita -> idInstalacion.equals(visita.getIdInstalacion()))
                        .toList();
            }
        } else if (idInstalacion != null) {
            visitas = visitaRepository.findByIdInstalacionOrderByFechaDesc(idInstalacion);
        } else {
            visitas = visitaRepository.findAllByOrderByFechaDesc();
        }

        List<VisitaResponse> lista = new ArrayList<>();
        for (Visita visita : visitas) {
            lista.add(aRespuesta(visita));
        }
        return lista;
    }

    public List<VisitaResponse> listarAbiertas(Integer idInstalacion) {
        List<Visita> visitas;
        if (idInstalacion != null) {
            visitas = visitaRepository.findByIdInstalacionAndFechaHoraCierreIsNullOrderByIdAsc(idInstalacion);
        } else {
            visitas = visitaRepository.findByFechaHoraCierreIsNullOrderByIdAsc();
        }

        List<VisitaResponse> lista = new ArrayList<>();
        for (Visita visita : visitas) {
            lista.add(aRespuesta(visita));
        }
        return lista;
    }

    public VisitaResponse obtener(Integer id) {
        return aRespuesta(buscar(id));
    }

    public VisitaDetalleResponse detalle(Integer id) {
        Visita visita = buscar(id);

        List<Integer> participantes = new ArrayList<>();
        for (VisitaParticipante participante : participanteRepository.findByIdIdVisitaOrderByIdIdUsuarioAsc(id)) {
            participantes.add(participante.getId().getIdUsuario());
        }

        RecepcionResponse acta = null;
        Optional<RecepcionActa> recepcion = actaRepository.findById(id);
        if (recepcion.isPresent()) {
            acta = aRecepcionResponse(recepcion.get());
        }

        return new VisitaDetalleResponse(aRespuesta(visita), participantes, acta);
    }

    public VisitaResponse crear(VisitaRequest datos) {
        if (datos.idInstalacion() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La visita necesita una instalación");
        }

        Visita visita = new Visita();
        visita.setNumeroOt(nuevoNumeroOt());
        visita.setIdInstalacion(datos.idInstalacion());
        visita.setFecha(datos.fecha() != null && !datos.fecha().isBlank()
                ? datos.fecha() : LocalDate.now().toString());
        visita.setTipoVisita(datos.tipoVisita() != null && !datos.tipoVisita().isBlank()
                ? datos.tipoVisita() : "SOLO_PRUEBAS");
        visita.setIdIngenieroCargo(datos.idIngenieroCargo());
        visita.setDescripcionMantenimiento(datos.descripcionMantenimiento() != null
                ? datos.descripcionMantenimiento() : "");
        visitaRepository.save(visita);

        return aRespuesta(visita);
    }

    public List<Integer> agregarParticipante(Integer id, Integer idUsuario) {
        buscar(id);
        if (idUsuario == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el usuario que participa");
        }
        if (!participanteRepository.existsByIdIdVisitaAndIdIdUsuario(id, idUsuario)) {
            participanteRepository.save(VisitaParticipante.builder()
                    .id(new VisitaParticipanteId(id, idUsuario))
                    .build());
        }
        return participantes(id);
    }

    public List<Integer> quitarParticipante(Integer id, Integer idUsuario) {
        buscar(id);
        participanteRepository.deleteByIdIdVisitaAndIdIdUsuario(id, idUsuario);
        return participantes(id);
    }

    public List<Integer> participantes(Integer id) {
        List<Integer> lista = new ArrayList<>();
        for (VisitaParticipante participante : participanteRepository.findByIdIdVisitaOrderByIdIdUsuarioAsc(id)) {
            lista.add(participante.getId().getIdUsuario());
        }
        return lista;
    }

    public RecepcionResponse cerrar(Integer id, CierreRequest datos) {
        Visita visita = buscar(id);

        if (datos.receptorNombre() == null || datos.receptorNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el nombre de quien recibe el acta");
        }
        if (datos.receptorRut() == null || datos.receptorRut().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el RUT de quien recibe el acta");
        }
        if (datos.receptorCargo() == null || datos.receptorCargo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el cargo de quien recibe el acta");
        }


        List<ProtocoloDto> pendientes = new ArrayList<>();
        for (ProtocoloDto protocolo : protocoloClient.listarPorVisita(id)) {
            if (!REGISTRADO.equals(protocolo.estado()) && !ANULADO.equals(protocolo.estado())) {
                pendientes.add(protocolo);
            }
        }
        if (!pendientes.isEmpty()) {
            String detalle = "";
            for (ProtocoloDto protocolo : pendientes) {
                if (!detalle.isEmpty()) {
                    detalle = detalle + ", ";
                }
                detalle = detalle + "equipo " + protocolo.idEquipo() + " (" + nombreEstado(protocolo.estado()) + ")";
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Faltan protocolos por registrar en terreno: " + detalle + ".");
        }

        String ahora = ahora();
        RecepcionActa acta = actaRepository.findById(id).orElseGet(RecepcionActa::new);
        acta.setIdVisita(id);
        acta.setReceptorNombre(datos.receptorNombre());
        acta.setReceptorRut(datos.receptorRut());
        acta.setReceptorCargo(datos.receptorCargo());
        acta.setFechaHoraRecepcion(ahora);
        actaRepository.save(acta);

        visita.setFechaHoraCierre(ahora);
        visitaRepository.save(visita);

        return aRecepcionResponse(acta);
    }

    public RecepcionResponse acta(Integer id) {
        buscar(id);
        RecepcionActa acta = actaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La visita no tiene acta de recepción"));
        return aRecepcionResponse(acta);
    }

    private String nuevoNumeroOt() {
        long cantidad = visitaRepository.count();
        return "OT-" + LocalDate.now().getYear() + "-" + (1300 + cantidad + 1);
    }

    private String ahora() {
        return LocalDateTime.now().format(FORMATO_FECHA_HORA);
    }

    private String nombreEstado(String estado) {
        switch (estado) {
            case "BORRADOR":
                return "borrador";
            case REGISTRADO:
                return "registrado en terreno";
            case "OBSERVADO":
                return "observado";
            case "FORMALIZADO":
                return "formalizado";
            case ANULADO:
                return "anulado";
            default:
                return estado;
        }
    }

    private Visita buscar(Integer id) {
        return visitaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visita no encontrada"));
    }

    private VisitaResponse aRespuesta(Visita visita) {
        return new VisitaResponse(
                visita.getId(),
                visita.getNumeroOt(),
                visita.getIdInstalacion(),
                visita.getFecha(),
                visita.getTipoVisita(),
                visita.getIdIngenieroCargo(),
                visita.getFechaHoraCierre(),
                visita.getDescripcionMantenimiento()
        );
    }

    private RecepcionResponse aRecepcionResponse(RecepcionActa acta) {
        return new RecepcionResponse(
                acta.getIdVisita(),
                acta.getReceptorNombre(),
                acta.getReceptorRut(),
                acta.getReceptorCargo(),
                acta.getFechaHoraRecepcion()
        );
    }
}
