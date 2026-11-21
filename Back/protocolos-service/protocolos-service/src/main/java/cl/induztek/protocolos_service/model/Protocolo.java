package cl.induztek.protocolos_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "protocolo", uniqueConstraints = @UniqueConstraint(columnNames = {"id_visita", "id_equipo"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Protocolo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_protocolo")
    private Integer id;

    @Column(nullable = false, unique = true, length = 40)
    private String numero;

    @Column(name = "id_visita", nullable = false)
    private Integer idVisita;

    @Column(name = "id_equipo", nullable = false)
    private Integer idEquipo;

    @Column(name = "id_usuario_registra", nullable = false)
    private Integer idUsuarioRegistra;

    @Column(name = "fecha_hora_prueba", nullable = false, length = 20)
    private String fechaHoraPrueba;

    @Column(name = "temperatura_c")
    private Double temperaturaC;

    @Column(name = "humedad_pct")
    private Double humedadPct;

    @Column(nullable = false, length = 30)
    private String estado;

    @Column(length = 600)
    private String observaciones;
}
