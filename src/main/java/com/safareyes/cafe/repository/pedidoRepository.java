package com.safareyes.cafe.repository;

import com.safareyes.cafe.Datos.ContadorProductos;
import com.safareyes.cafe.Datos.ProducoConAlergenos;
import com.safareyes.cafe.Datos.buscarPorCategoria;
import com.safareyes.cafe.Datos.lineaPedidoDTO;
import com.safareyes.cafe.modelo.Categoria;
import com.safareyes.cafe.modelo.Pedido;
import com.safareyes.cafe.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface pedidoRepository extends JpaRepository<Pedido,Integer> {

    @Query(nativeQuery = true, value = "SELECT p.id AS pedido_id, p.numero_turno, p.estado, " +
            "p.subtotal, p.descuento, p.total, " +
            "lp.id AS linea_id, lp.cantidad, lp.precio_unitario, lp.producto_id " +
            "FROM cafeteria.pedido p " +
            "LEFT JOIN cafeteria.linea_pedido lp ON p.id = lp.pedido_id " +
            "WHERE p.id = ?1")

    List<lineaPedidoDTO> pedidoConLineas(Integer id);
}
