package cl.induztek.tareas_service.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "visita_participante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitaParticipante {

    @EmbeddedId
    private VisitaParticipanteId id;
}
