package cl.induztek.gateway_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}

	@Bean
	RouteLocator rutas(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("protocolos", r -> r
						.path("/api/protocolos/**", "/api/tipos-prueba/**", "/api/tipos-equipo/*/pruebas")
						.uri("lb://protocolos-service"))
				.route("equipos", r -> r
						.path("/api/equipos/**", "/api/instalaciones/**", "/api/instrumentos/**", "/api/tipos-equipo")
						.uri("lb://equipos-service"))
				.route("tareas", r -> r
						.path("/api/visitas/**")
						.uri("lb://tareas-service"))
				.route("solicitudes", r -> r
						.path("/api/solicitudes/**")
						.uri("lb://solicitudes-service"))
				.route("roles", r -> r
						.path("/api/roles/**")
						.uri("lb://roles-service"))
				.route("usuarios", r -> r
						.path("/api/usuarios/**")
						.uri("lb://usuarios-service"))
				.build();
	}

}
