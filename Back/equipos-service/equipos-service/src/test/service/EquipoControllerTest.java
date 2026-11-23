package cl.induztek.equipos_service.service;

import cl.induztek.equipos_service.controller.EquipoController;
import cl.induztek.equipos_service.dto.EquipoRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EquipoControllerTest {
    @Mock EquipoService service;
    @InjectMocks EquipoController controller;

    @Test void instalaciones(){
        controller.listarInstalaciones(3);
        verify(service).listarInstalaciones(3);
    }

    @Test void tipos(){
        controller.listarTipos();
        verify(service).listarTipos();
    }

    @Test void instrumentos(){
        controller.listarInstrumentos();
        verify(service).listarInstrumentos();
    }

    @Test void vencidos(){
        controller.instrumentosVencidos("2026-11-23");
        verify(service).instrumentosVencidos("2026-11-23");
    }

    @Test void listar(){
        controller.listar(4,8);
        verify(service).listar(4,8);
    }

    @Test void obtener(){
        controller.obtener(7);
        verify(service).obtener(7);
    }

    @Test void crear(){
        EquipoRequest d=new EquipoRequest(
            1,
            "TR-1",
            1,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
            controller.crear(d);
            verify(service).crear(d);
        }

    @Test void actualizar(){
        EquipoRequest d=new EquipoRequest(
            1,
            "TR-1",
            1,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
            controller.actualizar(7,d);
            verify(service).actualizar(7,d);
        }

    @Test void eliminar(){
        controller.eliminar(7);
        verify(service).eliminar(7);
    }
}
