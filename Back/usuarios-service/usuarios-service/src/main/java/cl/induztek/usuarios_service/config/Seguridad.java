package cl.induztek.usuarios_service.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Seguridad {

    private static final BCryptPasswordEncoder CIFRADOR = new BCryptPasswordEncoder();

    public static String hashear(String clave) {
        return CIFRADOR.encode(clave);
    }

    public static boolean verificar(String clave, String claveCifrada) {
        return CIFRADOR.matches(clave, claveCifrada);
    }
}
