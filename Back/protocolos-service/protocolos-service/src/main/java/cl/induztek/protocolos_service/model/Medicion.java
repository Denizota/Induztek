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
@Table(name = "medicion", uniqueConstraints = @UniqueConstraint(columnNames = {"id_prueba", "codigo_parametro"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medicion")
    private Integer id;

    @Column(name = "id_prueba", nullable = false)
    private Integer idPrueba;

    @Column(name = "codigo_parametro", nullable = false, length = 40)
    private String codigoParametro;

    @Column(nullable = false, length = 40)
    private String valor;
}
