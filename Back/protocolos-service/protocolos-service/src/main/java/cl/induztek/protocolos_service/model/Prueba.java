package cl.induztek.protocolos_service.model;

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
@Table(name = "prueba")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prueba {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prueba")
    private Integer id;

    @Column(name = "id_protocolo", nullable = false)
    private Integer idProtocolo;

    @Column(name = "codigo_tipo_prueba", nullable = false, length = 40)
    private String codigoTipoPrueba;

    @Column(length = 80)
    private String elemento;

    @Column(length = 20)
    private String momento;

    @Column(nullable = false)
    private Boolean realizada;

    @Column(name = "motivo_no_realizada", length = 200)
    private String motivoNoRealizada;

    @Column(name = "id_instrumento")
    private Integer idInstrumento;

    @Column(length = 20)
    private String resultado;

    @Column(length = 400)
    private String comentario;
}
