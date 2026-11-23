package cl.induztek.protocolos_service.service;

import cl.induztek.protocolos_service.controller.ProtocoloController;
import cl.induztek.protocolos_service.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProtocoloControllerTest {
 @Mock ProtocoloService service; 
 @InjectMocks ProtocoloController controller;

 @Test void tipos(){
    controller.listarTipos();
    verify(service).listarTipos();
}

 @Test void tiposEquipo(){
    controller.listarTiposDeEquipo(2);
    verify(service).listarTiposDeEquipo(2);
}

 @Test void listar(){
    controller.listar(3,4);
    verify(service).listar(3,4);
}

 @Test void obtener(){
    controller.obtener(5);
    verify(service).obtener(5);
}

 @Test void detalle(){
    controller.detalle(5);
    verify(service).detalle(5);
}

 @Test void historial(){
    controller.historial(5);
    verify(service).historial(5);
}

 @Test void crear(){
    ProtocoloRequest d=new ProtocoloRequest(1,2,3,20.0,40.0,"");
    controller.crear(d);
    verify(service).crear(d);
}

 @Test void ambientes(){
    AmbientesRequest d=new AmbientesRequest(20.0,40.0,"");
    controller.actualizarAmbientes(5,d);
    verify(service).actualizarAmbientes(5,d);
}

 @Test void guardarPrueba(){
    PruebaRequest d=new PruebaRequest(
        "TTR",
        "",
        true,
        "",
        1,
        "APROBADO",
        "",
        java.util.Map.of());
        controller.guardarPrueba(5,d);
        verify(service).guardarPrueba(5,d);
    }

 @Test void cambiarEstado(){
    EstadoProtocoloRequest d=new EstadoProtocoloRequest(
        "REGISTRADO_TERRENO",
        3,
        "");
        controller.cambiarEstado(5,d);
        verify(service).cambiarEstado(5,d);
    }
}
