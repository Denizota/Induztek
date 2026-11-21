package cl.induztek.solicitudes_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "solicitud_acceso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Integer id;

    @Column(name = "sol_nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "sol_apellido", nullable = false, length = 80)
    private String apellido;

    @Column(name = "sol_pais", nullable = false, length = 60)
    private String pais;

    @Column(name = "sol_ciudad", nullable = false, length = 60)
    private String ciudad;

    @Column(name = "sol_correo", nullable = false, length = 120)
    private String correo;

    @Column(name = "sol_password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "sol_justificacion", nullable = false, length = 600)
    private String justificacion;

    @Column(name = "sol_estado", nullable = false, length = 30)
    private String estado;

    @Column(name = "sol_tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "sol_id_usuario")
    private Integer idUsuario;

    @Column(name = "sol_campo", nullable = false, length = 40)
    private String campo;

    @Column(name = "sol_valor", nullable = false, length = 200)
    private String valor;
}
