package cl.induztek.roles_service.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import cl.induztek.roles_service.model.Rol;
import cl.induztek.roles_service.repository.RolRepository;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final RolRepository rolRepository;

    @Override
    public void run(String... args) {
        if (rolRepository.count() > 0) {
            return;
        }

        rolRepository.saveAll(
                List.of(
                        Rol.builder()
                                .id(1)
                                .codigo("TECNICO")
                                .nombre("Técnico de terreno")
                                .build(),

                        Rol.builder()
                                .id(2)
                                .codigo("INGENIERO")
                                .nombre("Ingeniero en pruebas")
                                .build(),

                        Rol.builder()
                                .id(3)
                                .codigo("JEFE_TECNICO")
                                .nombre("Jefe técnico")
                                .build(),

                        Rol.builder()
                                .id(4)
                                .codigo("CLIENTE")
                                .nombre("Cliente / Visitante")
                                .build()
                )
        );
    }
}