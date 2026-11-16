package com.safareyes.cafe.repository;

import com.safareyes.cafe.Datos.ContadorProductos;
import com.safareyes.cafe.Datos.ProducoConAlergenos;
import com.safareyes.cafe.Datos.buscarPorCategoria;
import com.safareyes.cafe.Datos.lineaPedidoDTO;
import com.safareyes.cafe.modelo.Categoria;
import com.safareyes.cafe.modelo.Pedido;
import com.safareyes.cafe.modelo.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface pedidoRepository extends JpaRepository<Pedido, Integer> {

    @Query(nativeQuery = true, value = "SELECT p.id AS pedido_id, p.numero_turno, p.estado, " +
            "p.subtotal, p.descuento, p.total, " +
            "lp.id AS linea_id, lp.cantidad, lp.precio_unitario, lp.producto_id " +
            "FROM cafeteria.pedido p " +
            "LEFT JOIN cafeteria.linea_pedido lp ON p.id = lp.pedido_id " +
            "WHERE p.id = ?1")

    List<lineaPedidoDTO> pedidoConLineas(Integer id);

    //he tenido que añadir el CAST ya que en postman me daba error
    @Query("SELECT p FROM Pedido p " +
            "WHERE (:estado IS NULL OR p.estado = :estado) " +
            "AND (CAST(:fechaInicio AS LocalDateTime) IS NULL OR p.fechaHora >= :fechaInicio) " +
            "AND (CAST(:fechaFin AS LocalDateTime) IS NULL OR p.fechaHora <= :fechaFin)")
    Page<Pedido> listarPedidos(String estado, LocalDateTime fechaInicio,
                               LocalDateTime fechaFin, Pageable pageable);

    long countByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query(nativeQuery = true, value =
            "SELECT COUNT(p.id) AS numeroPedidos, " +
            "COALESCE(SUM(p.total), 0) AS total, " +
            "COALESCE(SUM(p.base_imponible), 0) AS baseImponible, " +
            "COALESCE(SUM(p.iva), 0) AS iva " +
            "FROM cafeteria.pedido p " +
            "WHERE p.estado = 'ENTREGADO' " +
            "AND p.fecha_hora >= ?1 AND p.fecha_hora <= ?2")
    Object[] contarInformeVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
