package cl.induztek.tareas_service.model;

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
public class VisitaParticipanteId implements Serializable {

    @Column(name = "id_visita")
    private Integer idVisita;

    @Column(name = "id_usuario")
    private Integer idUsuario;
}
