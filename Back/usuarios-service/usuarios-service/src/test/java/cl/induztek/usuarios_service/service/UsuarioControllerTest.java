package cl.induztek.usuarios_service.service;

import cl.induztek.usuarios_service.controller.UsuarioController;
import cl.induztek.usuarios_service.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {
 @Mock UsuarioService service; 
 @InjectMocks UsuarioController controller;

 @Test void listar(){
    controller.listar("Ana");
    verify(service).listar("Ana");
}

 @Test void obtener(){
    controller.obtener(2);
    verify(service).obtener(2);
}

 @Test void rol(){
    controller.listarPorRol(4);
    verify(service).listarPorRol(4);
}

 @Test void activo(){
    controller.listarPorActivo(true);
    verify(service).listarPorActivo(true);
}

 @Test void registrar(){
    RegistroRequest d=new RegistroRequest(
        "1-9",
        "Ana",
        "a@ejemplo.cl",
        "clave123",
        "9",
        4,
        1,
        "",
        "");
        controller.registrar(d);
        verify(service).registrar(d);
    }

 @Test void login(){
    LoginRequest d=new LoginRequest(
        "a@ejemplo.cl",
        "clave123");
        controller.iniciarSesion(d);
        verify(service).iniciarSesion(d);
    }

 @Test void actualizar(){
    ActualizarRequest d=new ActualizarRequest(
        "Ana",
        "a@ejemplo.cl",
        "9",
        4,
        1,
        "",
        "",
        "",
        "",
        "",
        "",
        null,
        null);
        controller.actualizar(2,d);
        verify(service).actualizar(2,d);
    }

 @Test void cambiarClave(){
    CambiarContrasenaRequest d=new CambiarContrasenaRequest(
        "anterior",
        "nueva123");
        controller.cambiarContrasena(2,d);
        verify(service).cambiarContrasena(2,d);
    }

 @Test void marcarActivo(){
    ActivoRequest d=new ActivoRequest(false);
    controller.marcarActivo(2,d);
    verify(service).marcarActivo(2,false);
}

 @Test void eliminar(){
    controller.eliminar(2);
    verify(service).eliminar(2);
}
}
