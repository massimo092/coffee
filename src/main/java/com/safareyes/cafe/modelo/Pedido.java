package com.safareyes.cafe.modelo;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "pedido", schema = "cafeteria")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private Integer id;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(name = "numero_turno")
    private Integer numeroTurno;

    @Column(name = "estado")
    private String estado;

    @Builder.Default
    @Column(name = "subtotal")
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "descuento")
    private BigDecimal descuento = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total")
    private BigDecimal total = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "base_imponible")
    private BigDecimal baseImponible = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "iva")
    private BigDecimal iva = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private Usuario usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cupon_id")
    @JsonIgnore
    private Cupon cuponId;

    @OneToMany(mappedBy = "pedido")
    @JsonIgnore
    private List<LineaPedido> lineas;

    @PrePersist
    public void prePersist() {
        if (this.subtotal == null) this.subtotal = BigDecimal.ZERO;
        if (this.descuento == null) this.descuento = BigDecimal.ZERO;
        if (this.total == null) this.total = BigDecimal.ZERO;
        if (this.baseImponible == null) this.baseImponible = BigDecimal.ZERO;
        if (this.iva == null) this.iva = BigDecimal.ZERO;
    }
}