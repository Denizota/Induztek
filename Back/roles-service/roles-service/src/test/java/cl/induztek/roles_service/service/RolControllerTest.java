package cl.induztek.roles_service.service;

import cl.induztek.roles_service.controller.RolController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RolControllerTest {
    @Mock private RolService service;
    @InjectMocks private RolController controller;

    @Test void delegaListado() { 
        controller.listar(); 
        verify(service).listar(); 
    }

    @Test void delegaObtencion() { 
        controller.obtener(2); 
        verify(service).obtener(2); 
    }

    @Test void delegaBusquedaPorCodigo() { 
        controller.porCodigo("CLIENTE"); 
        verify(service).porCodigo("CLIENTE"); 
    }
}
