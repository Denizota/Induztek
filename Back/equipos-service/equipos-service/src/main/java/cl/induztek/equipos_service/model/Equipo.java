package cl.induztek.equipos_service.model;

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
@Table(name = "equipo", uniqueConstraints = @UniqueConstraint(columnNames = {"id_instalacion", "tag"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipo")
    private Integer id;

    @Column(name = "id_instalacion", nullable = false)
    private Integer idInstalacion;

    @Column(nullable = false, length = 40)
    private String tag;

    @Column(name = "id_tipo_equipo", nullable = false)
    private Integer idTipoEquipo;

    @Column(length = 60)
    private String marca;

    @Column(length = 60)
    private String modelo;

    @Column(name = "numero_serie", length = 60)
    private String numeroSerie;

    @Column(name = "anio_fabricacion")
    private Integer anioFabricacion;

    @Column(name = "potencia_kva")
    private Double potenciaKva;

    @Column(name = "tension_primaria_kv")
    private Double tensionPrimariaKv;

    @Column(name = "tension_secundaria_kv")
    private Double tensionSecundariaKv;

    @Column(name = "corriente_nominal_a")
    private Double corrienteNominalA;

    @Column(name = "max_contactos_uohm")
    private Double maxContactosUohm;

    @Column(length = 300)
    private String observaciones;
}
