package cl.induztek.usuarios_service.repository;

import cl.induztek.usuarios_service.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByRut(String rut);

    boolean existsByCorreo(String correo);

    boolean existsByCorreoAndIdNot(String correo, Integer id);

    boolean existsByRut(String rut);

    List<Usuario> findAllByOrderByNombreAsc();

    List<Usuario> findByNombreContainingIgnoreCaseOrRutContainingIgnoreCaseOrCorreoContainingIgnoreCaseOrderByNombreAsc(
            String nombre, String rut, String correo);

    List<Usuario> findByIdRolOrderByNombreAsc(Integer idRol);

    List<Usuario> findByActivoOrderByNombreAsc(Boolean activo);
}
