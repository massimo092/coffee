package com.safareyes.cafe.repository;

import com.safareyes.cafe.Datos.lineaPedidoDTO;
import com.safareyes.cafe.modelo.Pedido;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface pedidoRepository extends JpaRepository<Pedido, Integer> {

    //GET 4 detalle con lineas
    @Query(nativeQuery = true, value = "SELECT p.id AS pedido_id, p.numero_turno, p.estado, " +
            "p.subtotal, p.descuento, p.total, " +
            "lp.id AS linea_id, lp.cantidad, lp.precio_unitario, lp.producto_id " +
            "FROM cafeteria.pedido p " +
            "LEFT JOIN cafeteria.linea_pedido lp ON p.id = lp.pedido_id " +
            "WHERE p.id = ?1")

    List<lineaPedidoDTO> pedidoConLineas(Integer id);

    //he tenido que añadir el CAST ya que en postman me daba error
    //GET 5 listado paginado por estado y fecha
    @Query("SELECT p FROM Pedido p " +
            "WHERE (:estado IS NULL OR p.estado = :estado) " +
            "AND (CAST(:fechaInicio AS LocalDateTime) IS NULL OR p.fechaHora >= :fechaInicio) " +
            "AND (CAST(:fechaFin AS LocalDateTime) IS NULL OR p.fechaHora <= :fechaFin)")
    Page<Pedido> listarPedidos(String estado, LocalDateTime fechaInicio,
                               LocalDateTime fechaFin, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.fechaHora >= :inicio AND p.fechaHora < :fin")
    long contarPedidosEnIntervalo(@Param("inicio") LocalDateTime inicio,
                                  @Param("fin") LocalDateTime fin);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pedido p WHERE p.id = :id")
    Optional<Pedido> findByIdForUpdate(@Param("id") Integer id);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(p.id) AS numeroPedidos,
        COALESCE(SUM(p.total), 0) AS total,
        COALESCE(SUM(p.base_imponible), 0) AS baseImponible,
        COALESCE(SUM(p.iva), 0) AS iva
        FROM cafeteria.pedido p
        WHERE LOWER(p.estado) = 'entregado'
          AND CAST(p.fecha_hora AS DATE) >= :desde
          AND CAST(p.fecha_hora AS DATE) < :hasta
        """)
    List<Object[]> contarInformeVentas(@Param("desde") LocalDate fechaInicio,
                                       @Param("hasta") LocalDate fechaFin);
}
