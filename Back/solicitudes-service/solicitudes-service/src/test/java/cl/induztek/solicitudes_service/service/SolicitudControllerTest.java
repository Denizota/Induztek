package cl.induztek.solicitudes_service.service;

import cl.induztek.solicitudes_service.controller.SolicitudController;
import cl.induztek.solicitudes_service.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SolicitudControllerTest {
 @Mock SolicitudService service; @InjectMocks SolicitudController controller;
 @Test void listar(){
    controller.listar(
        "PENDIENTE",
        "INGRESO");
        verify(service).listar("PENDIENTE","INGRESO");
    }

 @Test void obtener(){
    controller.obtener(2);
    verify(service).obtener(2);
}

 @Test void pendientes(){
    controller.pendientes();
    verify(service).pendientes();
}

 @Test void crear(){
    SolicitudRequest d=new SolicitudRequest(
        "Juanito",
        "Pérez",
        "Chile",
        "Santiago",
        "a@ejemplo.cl",
        "clave123",
        "Acceso",
        "INGRESO",
        1,
        null,
        null);
        controller.crear(d);
        verify(service).crear(d);
    }

 @Test void estado(){
    EstadoSolicitudRequest d=new EstadoSolicitudRequest("APROBADA");
    controller.cambiarEstado(2,d);
    verify(service).cambiarEstado(2,"APROBADA");
}

 @Test void eliminar(){
    controller.eliminar(2);
    verify(service).eliminar(2);
}
}
