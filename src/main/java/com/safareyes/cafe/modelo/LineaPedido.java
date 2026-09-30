package com.safareyes.cafe.modelo;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Entity
@Table(name = "linea_pedido", schema = "cafeteria")
public class LineaPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    private Integer cantidad;

    private Integer precio_unitario;

    @ManyToOne(Fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;
}
