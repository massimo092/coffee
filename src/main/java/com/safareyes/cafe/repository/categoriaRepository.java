package com.safareyes.cafe.repository;

import com.safareyes.cafe.Datos.ContadorProductos;
import com.safareyes.cafe.modelo.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface categoriaRepository extends JpaRepository<Categoria, Integer> {

    @Query(nativeQuery = true, value = "SELECT c.id AS id, c.nombre AS categoria, COUNT(p.id) AS productos, " +
            "c.disponibilidad AS disponibilidad " +
            "FROM cafeteria.categoria c " +
            "LEFT JOIN cafeteria.producto p ON p.categoria_id = c.id AND p.activo = true " +
            "GROUP BY c.id, c.nombre, c.disponibilidad")
    List<ContadorProductos> contarProductosActivos();
}
