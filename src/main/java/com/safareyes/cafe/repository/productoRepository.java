package com.safareyes.cafe.repository;

import com.safareyes.cafe.Datos.ProducoConAlergenos;
import com.safareyes.cafe.Datos.buscarPorCategoria;
import com.safareyes.cafe.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface productoRepository extends JpaRepository<Producto,Integer> {

    @Query(nativeQuery = true,value = "SELECT c.nombre AS categoria, COUNT(p.*) AS productos " +
        "FROM cafeteria.producto p " +
        "JOIN cafeteria.categoria c ON p.categoria_id = c.id " +
        "GROUP BY c.nombre")

    List<buscarPorCategoria> BuscarNumeroPorCategoria();


    @Query(nativeQuery = true,value = "SELECT p.nombre AS producto, a.descripcion AS alergenos " +
        "FROM cafeteria.producto p " +
        "INNER JOIN cafeteria.producto_alergeno pa ON p.id = pa.producto_id " +
        "INNER JOIN cafeteria.alergeno a ON pa.alergeno_id = a.id")


    List<ProducoConAlergenos> BuscarProductosConAlergenos();
}
