package cl.induztek.usuarios_service.model;

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
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    @Column(name = "id_rol", nullable = false)
    private Integer idRol;

    @Column(nullable = false, unique = true, length = 20)
    private String rut;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(unique = true, length = 120)
    private String correo;

    @Column(length = 30)
    private String telefono;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "id_empresa")
    private Integer idEmpresa;

    @Column(length = 80)
    private String cargo;

    @Column(length = 80)
    private String giro;

    @Column(length = 600)
    private String bio;

    @Column(length = 200)
    private String especialidades;

    @Column(length = 200)
    private String formacion;

    @Column(length = 200)
    private String certificaciones;

    private Integer desde;

    private Integer calificacion;
}
