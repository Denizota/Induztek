package cl.induztek.tareas_service.model;

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
@Table(name = "visita")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_visita")
    private Integer id;

    @Column(name = "numero_ot", nullable = false, unique = true, length = 30)
    private String numeroOt;

    @Column(name = "id_instalacion", nullable = false)
    private Integer idInstalacion;

    @Column(nullable = false, length = 10)
    private String fecha;

    @Column(name = "tipo_visita", length = 30)
    private String tipoVisita;

    @Column(name = "id_ingeniero_cargo")
    private Integer idIngenieroCargo;

    @Column(name = "fecha_hora_cierre", length = 20)
    private String fechaHoraCierre;

    @Column(name = "descripcion_mantenimiento", length = 600)
    private String descripcionMantenimiento;
}
