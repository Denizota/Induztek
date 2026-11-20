package cl.induztek.equipos_service.model;

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
@Table(name = "instalacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instalacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_instalacion")
    private Integer id;

    @Column(name = "id_usuario_cliente", nullable = false)
    private Integer idUsuarioCliente;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 120)
    private String ubicacion;

    @Column(length = 80)
    private String comuna;

    @Column(name = "observaciones_acceso", length = 300)
    private String observacionesAcceso;
}
