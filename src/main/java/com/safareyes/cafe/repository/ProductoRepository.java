package com.safareyes.cafe.repository;

import com.safareyes.cafe.Datos.ProducoConAlergenos;
import com.safareyes.cafe.Datos.buscarPorCategoria;
import com.safareyes.cafe.modelo.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    //GET 1 Carta paginada con filtros opcionales
    Page<Producto> findByActivoTrueAndNombreContainingIgnoreCase(String texto, Pageable pageable);

    Page<Producto> findByActivoTrueAndCategoriaIdAndNombreContainingIgnoreCase(
            Integer categoriaId, String texto, Pageable pageable);
    //GET 2 Detalle con alérgenos
    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN FETCH p.alergenos WHERE p.id = :id")
    Optional<Producto> findByIdWithAlergenos(@Param("id") Integer id);

    //GET 3
    @Query(nativeQuery = true, value = "SELECT c.nombre AS categoria, COUNT(p.*) AS productos " +
            "FROM cafeteria.producto p " +
            "JOIN cafeteria.categoria c ON p.categoria_id = c.id " +
            "GROUP BY c.nombre")
    List<buscarPorCategoria> BuscarNumeroPorCategoria();

    // PUT Marcar disponible o agotado
    @Modifying
    @Query("UPDATE Producto p SET p.disponible = :disponible WHERE p.id = :id")
    void updateDisponible(@Param("id") Integer id, @Param("disponible") Boolean disponible);


    // GET 6 Para informes: productos más vendidos
    @Query(nativeQuery = true, value = """
        SELECT lp.producto_id, p.nombre, SUM(lp.cantidad) AS total_vendido
        FROM cafeteria.linea_pedido lp
        JOIN cafeteria.producto p ON lp.producto_id = p.id
        JOIN cafeteria.pedido ped ON lp.pedido_id = ped.id
        WHERE ped.estado = 'entregado'
          AND CAST(ped.fecha_hora AS DATE) >= :desde
          AND CAST(ped.fecha_hora AS DATE) < :hasta
        GROUP BY lp.producto_id, p.nombre
        ORDER BY total_vendido DESC
        """)

    //GET 7
    List<Object[]> findProductosTop(@Param("desde") java.time.LocalDate desde,
                                    @Param("hasta") java.time.LocalDate hasta);
    // Buscar productos por categoría (para agotar/reponer categoría)
    List<Producto> findByCategoriaIdAndActivoTrue(Integer categoriaId);
}
