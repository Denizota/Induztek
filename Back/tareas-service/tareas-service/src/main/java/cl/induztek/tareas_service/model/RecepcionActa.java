package cl.induztek.tareas_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "recepcion_acta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecepcionActa {

    @Id
    @Column(name = "id_visita")
    private Integer idVisita;

    @Column(name = "receptor_nombre", nullable = false, length = 120)
    private String receptorNombre;

    @Column(name = "receptor_rut", nullable = false, length = 15)
    private String receptorRut;

    @Column(name = "receptor_cargo", length = 80)
    private String receptorCargo;

    @Column(name = "fecha_hora_recepcion", nullable = false, length = 20)
    private String fechaHoraRecepcion;
}
