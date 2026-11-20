package cl.induztek.equipos_service.service;

import cl.induztek.equipos_service.dto.EquipoRequest;
import cl.induztek.equipos_service.dto.EquipoResponse;
import cl.induztek.equipos_service.model.Equipo;
import cl.induztek.equipos_service.model.Instalacion;
import cl.induztek.equipos_service.model.Instrumento;
import cl.induztek.equipos_service.model.TipoEquipo;
import cl.induztek.equipos_service.repository.EquipoRepository;
import cl.induztek.equipos_service.repository.InstalacionRepository;
import cl.induztek.equipos_service.repository.InstrumentoRepository;
import cl.induztek.equipos_service.repository.TipoEquipoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final InstalacionRepository instalacionRepository;
    private final TipoEquipoRepository tipoEquipoRepository;
    private final InstrumentoRepository instrumentoRepository;

    public List<Instalacion> listarInstalaciones(Integer idCliente) {
        if (idCliente == null) {
            return instalacionRepository.findAll();
        }
        return instalacionRepository.findByIdUsuarioClienteOrderByNombreAsc(idCliente);
    }

    public List<TipoEquipo> listarTipos() {
        return tipoEquipoRepository.findAll();
    }

    public List<Instrumento> listarInstrumentos() {
        return instrumentoRepository.findAllByOrderByFechaVenceAsc();
    }

    public List<Instrumento> instrumentosVencidos(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta la fecha de hoy (aaaa-mm-dd)");
        }
        return instrumentoRepository.findByFechaVenceLessThanOrderByFechaVenceAsc(fecha);
    }

    public List<EquipoResponse> listar(Integer idCliente, Integer idInstalacion) {
        List<Equipo> equipos = new ArrayList<>();

        if (idInstalacion != null) {
            equipos.addAll(equipoRepository.findByIdInstalacionOrderByTagAsc(idInstalacion));
        } else if (idCliente != null) {
            List<Instalacion> instalaciones =
                    instalacionRepository.findByIdUsuarioClienteOrderByNombreAsc(idCliente);
            for (Instalacion instalacion : instalaciones) {
                equipos.addAll(equipoRepository.findByIdInstalacionOrderByTagAsc(instalacion.getId()));
            }
        } else {
            equipos.addAll(equipoRepository.findAll());
        }

        Map<Integer, TipoEquipo> tipos = tipoEquipoRepository.findAll().stream()
                .collect(Collectors.toMap(TipoEquipo::getId, tipo -> tipo));
        Map<String, Instalacion> instalaciones = instalacionRepository.findAll().stream()
                .collect(Collectors.toMap(instalacion -> String.valueOf(instalacion.getId()), instalacion -> instalacion));

        List<EquipoResponse> lista = new ArrayList<>();
        for (Equipo equipo : equipos) {
            lista.add(aRespuesta(equipo, tipos.get(equipo.getIdTipoEquipo()),
                    instalaciones.get(String.valueOf(equipo.getIdInstalacion()))));
        }
        return lista;
    }

    public EquipoResponse obtener(Integer id) {
        return aRespuesta(buscar(id));
    }

    public EquipoResponse crear(EquipoRequest datos) {
        validar(datos);
        revisarTagRepetido(datos.idInstalacion(), datos.tag(), null);

        Equipo equipo = new Equipo();
        copiar(datos, equipo);
        return aRespuesta(equipoRepository.save(equipo));
    }

    public EquipoResponse actualizar(Integer id, EquipoRequest datos) {
        Equipo equipo = buscar(id);
        validar(datos);
        revisarTagRepetido(datos.idInstalacion(), datos.tag(), id);

        copiar(datos, equipo);
        return aRespuesta(equipoRepository.save(equipo));
    }

    public void eliminar(Integer id) {
        equipoRepository.delete(buscar(id));
    }

    private void validar(EquipoRequest datos) {
        if (datos.idInstalacion() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El equipo necesita una instalación");
        }
        if (datos.idTipoEquipo() == null || tipoEquipoRepository.findById(datos.idTipoEquipo()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de equipo no existe");
        }
        if (datos.tag() == null || datos.tag().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El equipo necesita un tag");
        }
        if (instalacionRepository.findById(datos.idInstalacion()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La instalación indicada no existe");
        }
    }

    private void revisarTagRepetido(Integer idInstalacion, String tag, Integer idEquipo) {
        Optional<Equipo> repetido = equipoRepository.findByIdInstalacionAndTag(idInstalacion, tag);
        if (repetido.isEmpty()) {
            return;
        }
        if (idEquipo == null || !repetido.get().getId().equals(idEquipo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese tag ya existe en la instalación");
        }
    }

    private void copiar(EquipoRequest datos, Equipo equipo) {
        equipo.setIdInstalacion(datos.idInstalacion());
        equipo.setTag(datos.tag());
        equipo.setIdTipoEquipo(datos.idTipoEquipo());
        equipo.setMarca(datos.marca() != null ? datos.marca() : "");
        equipo.setModelo(datos.modelo() != null ? datos.modelo() : "");
        equipo.setNumeroSerie(datos.numeroSerie() != null ? datos.numeroSerie() : "");
        equipo.setAnioFabricacion(datos.anioFabricacion());
        equipo.setPotenciaKva(datos.potenciaKva());
        equipo.setTensionPrimariaKv(datos.tensionPrimariaKv());
        equipo.setTensionSecundariaKv(datos.tensionSecundariaKv());
        equipo.setCorrienteNominalA(datos.corrienteNominalA());
        equipo.setMaxContactosUohm(datos.maxContactosUohm());
        equipo.setObservaciones(datos.observaciones() != null ? datos.observaciones() : "");
    }

    private Equipo buscar(Integer id) {
        return equipoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipo no encontrado"));
    }

    private EquipoResponse aRespuesta(Equipo equipo) {
        TipoEquipo tipo = tipoEquipoRepository.findById(equipo.getIdTipoEquipo()).orElse(null);
        Instalacion instalacion = instalacionRepository.findById(equipo.getIdInstalacion()).orElse(null);
        return aRespuesta(equipo, tipo, instalacion);
    }

    private EquipoResponse aRespuesta(Equipo equipo, TipoEquipo tipo, Instalacion instalacion) {
        return new EquipoResponse(
                equipo.getId(),
                equipo.getIdInstalacion(),
                instalacion != null ? instalacion.getNombre() : "",
                equipo.getTag(),
                equipo.getIdTipoEquipo(),
                tipo != null ? tipo.getNombre() : "",
                tipo != null ? tipo.getClasificacion() : "",
                equipo.getMarca(),
                equipo.getModelo(),
                equipo.getNumeroSerie(),
                equipo.getAnioFabricacion(),
                equipo.getPotenciaKva(),
                equipo.getTensionPrimariaKv(),
                equipo.getTensionSecundariaKv(),
                equipo.getCorrienteNominalA(),
                equipo.getMaxContactosUohm(),
                equipo.getObservaciones()
        );
    }
}
