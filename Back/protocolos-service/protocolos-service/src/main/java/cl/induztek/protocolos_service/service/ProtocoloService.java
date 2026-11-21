package cl.induztek.protocolos_service.service;

import cl.induztek.protocolos_service.dto.AmbientesRequest;
import cl.induztek.protocolos_service.dto.EstadoProtocoloRequest;
import cl.induztek.protocolos_service.dto.HistorialResponse;
import cl.induztek.protocolos_service.dto.ProtocoloDetalleResponse;
import cl.induztek.protocolos_service.dto.ProtocoloRequest;
import cl.induztek.protocolos_service.dto.ProtocoloResponse;
import cl.induztek.protocolos_service.dto.PruebaRequest;
import cl.induztek.protocolos_service.dto.PruebaResponse;
import cl.induztek.protocolos_service.model.HistorialEstado;
import cl.induztek.protocolos_service.model.Medicion;
import cl.induztek.protocolos_service.model.Protocolo;
import cl.induztek.protocolos_service.model.Prueba;
import cl.induztek.protocolos_service.model.TipoEquipoPrueba;
import cl.induztek.protocolos_service.model.TipoPrueba;
import cl.induztek.protocolos_service.repository.HistorialEstadoRepository;
import cl.induztek.protocolos_service.repository.MedicionRepository;
import cl.induztek.protocolos_service.repository.ProtocoloRepository;
import cl.induztek.protocolos_service.repository.PruebaRepository;
import cl.induztek.protocolos_service.repository.TipoEquipoPruebaRepository;
import cl.induztek.protocolos_service.repository.TipoPruebaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProtocoloService {

    private static final String BORRADOR = "BORRADOR";
    private static final String REGISTRADO = "REGISTRADO_TERRENO";
    private static final String OBSERVADO = "OBSERVADO";
    private static final String FORMALIZADO = "FORMALIZADO";
    private static final String ANULADO = "ANULADO";
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ProtocoloRepository protocoloRepository;
    private final PruebaRepository pruebaRepository;
    private final MedicionRepository medicionRepository;
    private final HistorialEstadoRepository historialRepository;
    private final TipoPruebaRepository tipoPruebaRepository;
    private final TipoEquipoPruebaRepository tipoEquipoPruebaRepository;

    public List<TipoPrueba> listarTipos() {
        return tipoPruebaRepository.findAll();
    }

    public List<TipoPrueba> listarTiposDeEquipo(Integer idTipoEquipo) {
        List<TipoPrueba> lista = new ArrayList<>();
        for (TipoEquipoPrueba par : tipoEquipoPruebaRepository.findByIdIdTipoEquipo(idTipoEquipo)) {
            tipoPruebaRepository.findById(par.getId().getIdTipoPrueba()).ifPresent(lista::add);
        }
        return lista;
    }

    public List<ProtocoloResponse> listar(Integer idVisita, Integer idEquipo) {
        List<Protocolo> protocolos;
        if (idVisita != null) {
            protocolos = protocoloRepository.findByIdVisitaOrderByFechaHoraPruebaDesc(idVisita);
        } else if (idEquipo != null) {
            protocolos = protocoloRepository.findByIdEquipoOrderByFechaHoraPruebaDesc(idEquipo);
        } else {
            protocolos = protocoloRepository.findAllByOrderByFechaHoraPruebaDesc();
        }

        List<ProtocoloResponse> lista = new ArrayList<>();
        for (Protocolo protocolo : protocolos) {
            lista.add(aRespuesta(protocolo));
        }
        return lista;
    }

    public ProtocoloResponse obtener(Integer id) {
        return aRespuesta(buscar(id));
    }

    public ProtocoloDetalleResponse detalle(Integer id) {
        Protocolo protocolo = buscar(id);

        List<PruebaResponse> pruebas = new ArrayList<>();
        for (Prueba prueba : pruebaRepository.findByIdProtocoloOrderByIdAsc(id)) {
            pruebas.add(aPruebaResponse(prueba));
        }

        return new ProtocoloDetalleResponse(aRespuesta(protocolo), pruebas, historial(id));
    }

    public List<HistorialResponse> historial(Integer id) {
        buscar(id);
        List<HistorialResponse> lista = new ArrayList<>();
        for (HistorialEstado paso : historialRepository.findByIdProtocoloOrderByIdAsc(id)) {
            lista.add(aHistorialResponse(paso));
        }
        return lista;
    }

    public ProtocoloResponse crear(ProtocoloRequest datos) {
        if (datos.idVisita() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El protocolo necesita una visita");
        }
        if (datos.idEquipo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El protocolo necesita un equipo");
        }
        if (datos.idUsuarioRegistra() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El protocolo necesita el usuario que lo abre");
        }
        if (protocoloRepository.findByIdVisitaAndIdEquipo(datos.idVisita(), datos.idEquipo()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ese equipo ya tiene un protocolo en esta visita");
        }

        Protocolo protocolo = new Protocolo();
        protocolo.setNumero(nuevoNumero());
        protocolo.setIdVisita(datos.idVisita());
        protocolo.setIdEquipo(datos.idEquipo());
        protocolo.setIdUsuarioRegistra(datos.idUsuarioRegistra());
        protocolo.setFechaHoraPrueba(ahora());
        protocolo.setTemperaturaC(datos.temperaturaC());
        protocolo.setHumedadPct(datos.humedadPct());
        protocolo.setEstado(BORRADOR);
        protocolo.setObservaciones(datos.observaciones() != null ? datos.observaciones() : "");
        protocoloRepository.save(protocolo);

        guardarHistorial(protocolo.getId(), null, BORRADOR, datos.idUsuarioRegistra(), "");
        return aRespuesta(protocolo);
    }

    public ProtocoloResponse actualizarAmbientes(Integer id, AmbientesRequest datos) {
        Protocolo protocolo = buscarEditable(id);
        protocolo.setTemperaturaC(datos.temperaturaC());
        protocolo.setHumedadPct(datos.humedadPct());
        if (datos.observaciones() != null) {
            protocolo.setObservaciones(datos.observaciones());
        }
        protocoloRepository.save(protocolo);
        return aRespuesta(protocolo);
    }

    public PruebaResponse guardarPrueba(Integer id, PruebaRequest datos) {
        buscarEditable(id);

        if (datos.codigoTipoPrueba() == null || datos.codigoTipoPrueba().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La prueba necesita un tipo");
        }
        if (tipoPruebaRepository.findByCodigo(datos.codigoTipoPrueba()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de prueba no existe");
        }

        String elemento = datos.elemento() != null ? datos.elemento() : "";
        boolean realizada = Boolean.TRUE.equals(datos.realizada());

        if (realizada && datos.resultado() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La prueba realizada necesita un resultado");
        }
        if (datos.resultado() != null && !esResultado(datos.resultado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El resultado debe ser APROBADO, OBSERVADO o RECHAZADO");
        }
        if (!realizada && (datos.motivoNoRealizada() == null || datos.motivoNoRealizada().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Si la prueba no se realizó, escribe el motivo");
        }

        
        Optional<Prueba> existente = pruebaRepository
                .findFirstByIdProtocoloAndCodigoTipoPruebaAndElementoOrderByIdDesc(
                        id, datos.codigoTipoPrueba(), elemento);
        if (existente.isPresent()) {
            medicionRepository.deleteByIdPrueba(existente.get().getId());
            pruebaRepository.delete(existente.get());
        }

        Prueba prueba = new Prueba();
        prueba.setIdProtocolo(id);
        prueba.setCodigoTipoPrueba(datos.codigoTipoPrueba());
        prueba.setElemento(elemento);
        prueba.setMomento("UNICA");
        prueba.setRealizada(realizada);
        prueba.setMotivoNoRealizada(datos.motivoNoRealizada() != null ? datos.motivoNoRealizada() : "");
        prueba.setIdInstrumento(datos.idInstrumento());
        prueba.setResultado(datos.resultado());
        prueba.setComentario(datos.comentario() != null ? datos.comentario() : "");
        pruebaRepository.save(prueba);

        if (realizada && datos.mediciones() != null) {
            for (Map.Entry<String, Double> medicion : datos.mediciones().entrySet()) {
                medicionRepository.save(Medicion.builder()
                        .idPrueba(prueba.getId())
                        .codigoParametro(medicion.getKey())
                        .valor(String.valueOf(medicion.getValue()))
                        .build());
            }
        }

        return aPruebaResponse(prueba);
    }

    public ProtocoloResponse cambiarEstado(Integer id, EstadoProtocoloRequest datos) {
        Protocolo protocolo = buscar(id);
        String actual = protocolo.getEstado();
        String nuevo = datos.estado();

        if (nuevo == null || !esEstado(nuevo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado indicado no existe");
        }
        if (FORMALIZADO.equals(actual)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un protocolo formalizado no se modifica ni se anula.");
        }
        if (!cambioPermitido(actual, nuevo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La transición " + nombreEstado(actual) + " → " + nombreEstado(nuevo) + " no existe.");
        }

        String motivo = datos.motivo() != null ? datos.motivo() : "";
        if (motivo.isBlank() && (OBSERVADO.equals(nuevo) || ANULADO.equals(nuevo))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El " + nombreEstado(nuevo).toLowerCase() + " requiere escribir un motivo.");
        }
        if (datos.idUsuario() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el usuario que hace el cambio");
        }

        protocolo.setEstado(nuevo);
        protocoloRepository.save(protocolo);
        guardarHistorial(id, actual, nuevo, datos.idUsuario(), motivo);
        return aRespuesta(protocolo);
    }

    private boolean cambioPermitido(String desde, String hacia) {
        if (REGISTRADO.equals(hacia) && (BORRADOR.equals(desde) || OBSERVADO.equals(desde))) {
            return true;
        }
        if (FORMALIZADO.equals(hacia) && REGISTRADO.equals(desde)) {
            return true;
        }
        if (OBSERVADO.equals(hacia) && REGISTRADO.equals(desde)) {
            return true;
        }
        return ANULADO.equals(hacia);
    }

    private Protocolo buscar(Integer id) {
        return protocoloRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Protocolo no encontrado"));
    }

    private Protocolo buscarEditable(Integer id) {
        Protocolo protocolo = buscar(id);
        String estado = protocolo.getEstado();

        if (BORRADOR.equals(estado) || OBSERVADO.equals(estado)) {
            return protocolo;
        }
        if (REGISTRADO.equals(estado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El equipo ya tiene el protocolo " + protocolo.getNumero() + " registrado en esta visita. " +
                            "Para corregirlo, el ingeniero debe devolverlo con observación.");
        }
        if (FORMALIZADO.equals(estado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El protocolo " + protocolo.getNumero() + " está formalizado y no se modifica.");
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "El protocolo " + protocolo.getNumero() + " está anulado en esta visita.");
    }

    private String nuevoNumero() {
        String prefijo = "PT-" + LocalDate.now().getYear() + "-";
        int correlativo = 400;

        Optional<Protocolo> ultimo =
                protocoloRepository.findFirstByNumeroStartingWithOrderByNumeroDesc(prefijo);
        if (ultimo.isPresent()) {
            String numero = ultimo.get().getNumero();
            String cola = numero.substring(numero.lastIndexOf("-") + 1);
            try {
                correlativo = Integer.parseInt(cola);
            } catch (NumberFormatException ex) {
                correlativo = 400;
            }
        }
        return prefijo + (correlativo + 1);
    }

    private void guardarHistorial(Integer idProtocolo, String anterior, String nuevo,
                                  Integer idUsuario, String motivo) {
        historialRepository.save(HistorialEstado.builder()
                .idProtocolo(idProtocolo)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .idUsuario(idUsuario)
                .fechaHora(ahora())
                .motivo(motivo)
                .build());
    }

    private String ahora() {
        return LocalDateTime.now().format(FORMATO_FECHA_HORA);
    }

    private boolean esEstado(String estado) {
        return BORRADOR.equals(estado) || REGISTRADO.equals(estado) || OBSERVADO.equals(estado)
                || FORMALIZADO.equals(estado) || ANULADO.equals(estado);
    }

    private String nombreEstado(String estado) {
        switch (estado) {
            case BORRADOR:
                return "Borrador";
            case REGISTRADO:
                return "Registrado en terreno";
            case OBSERVADO:
                return "Observado";
            case FORMALIZADO:
                return "Formalizado";
            case ANULADO:
                return "Anulado";
            default:
                return estado;
        }
    }

    private boolean esResultado(String resultado) {
        return "APROBADO".equals(resultado) || "OBSERVADO".equals(resultado) || "RECHAZADO".equals(resultado);
    }

    private ProtocoloResponse aRespuesta(Protocolo protocolo) {
        return new ProtocoloResponse(
                protocolo.getId(),
                protocolo.getNumero(),
                protocolo.getIdVisita(),
                protocolo.getIdEquipo(),
                protocolo.getIdUsuarioRegistra(),
                protocolo.getFechaHoraPrueba(),
                protocolo.getTemperaturaC(),
                protocolo.getHumedadPct(),
                protocolo.getEstado(),
                protocolo.getObservaciones()
        );
    }

    private PruebaResponse aPruebaResponse(Prueba prueba) {
        Map<String, Double> mediciones = new LinkedHashMap<>();
        for (Medicion medicion : medicionRepository.findByIdPruebaOrderByIdAsc(prueba.getId())) {
            try {
                mediciones.put(medicion.getCodigoParametro(), Double.parseDouble(medicion.getValor()));
            } catch (NumberFormatException ex) {
                mediciones.put(medicion.getCodigoParametro(), 0.0);
            }
        }
        return new PruebaResponse(
                prueba.getId(),
                prueba.getCodigoTipoPrueba(),
                prueba.getElemento(),
                prueba.getMomento(),
                prueba.getRealizada(),
                prueba.getMotivoNoRealizada(),
                prueba.getIdInstrumento(),
                prueba.getResultado(),
                prueba.getComentario(),
                mediciones
        );
    }

    private HistorialResponse aHistorialResponse(HistorialEstado paso) {
        return new HistorialResponse(
                paso.getId(),
                paso.getEstadoAnterior(),
                paso.getEstadoNuevo(),
                paso.getIdUsuario(),
                paso.getFechaHora(),
                paso.getMotivo()
        );
    }
}
