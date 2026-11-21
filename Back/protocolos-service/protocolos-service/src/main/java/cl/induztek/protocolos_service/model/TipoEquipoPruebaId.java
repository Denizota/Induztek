package cl.induztek.protocolos_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TipoEquipoPruebaId implements Serializable {

    @Column(name = "id_tipo_equipo")
    private Integer idTipoEquipo;

    @Column(name = "id_tipo_prueba")
    private Integer idTipoPrueba;
}
