package com.safareyes.cafe.repository;

import com.safareyes.cafe.Datos.ContadorProductos;
import com.safareyes.cafe.Datos.ProducoConAlergenos;
import com.safareyes.cafe.Datos.buscarPorCategoria;
import com.safareyes.cafe.modelo.Categoria;
import com.safareyes.cafe.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface categoriaRepository extends JpaRepository<Categoria,Integer> {

    @Query(nativeQuery = true,value = "SELECT c.nombre AS categoria, COUNT(p.*) AS productos " +
            "FROM cafeteria.producto p " +
            "JOIN cafeteria.categoria c ON p.categoria_id = c.id " +
            "WHERE p.activo = true " +
            "GROUP BY c.nombre")

    List<ContadorProductos>contarProductosActivos();

}
