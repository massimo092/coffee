package com.safareyes.cafe.controllers;

import com.safareyes.cafe.Datos.ContadorProductos;
import com.safareyes.cafe.modelo.Producto;
import com.safareyes.cafe.repository.categoriaRepository;
import com.safareyes.cafe.repository.productoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaController {

    @Autowired
    private categoriaRepository categoriaRepository;

    @Autowired
    private productoRepository productoRepository;

    // 7. GET /api/v1/categorias - Categorías con número de productos activos - Público
    @GetMapping
    public ResponseEntity<List<ContadorProductos>> getCategorias() {
        List<ContadorProductos> categorias = categoriaRepository.contarProductosActivos();
        return ResponseEntity.ok(categorias);
    }

    // 8. PATCH /api/v1/categorias/{id}/disponibilidad - Agotar o reponer toda una categoría - ADMIN, BARISTA
    @PatchMapping("/{id}/disponibilidad")
    @Transactional
    public ResponseEntity<Map<String, String>> cambiarDisponibilidadCategoria(
            @PathVariable Integer id,
            @RequestBody Map<String, Boolean> body) {

        Boolean disponible = body.get("disponible");
        if (disponible == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "El campo 'disponible' es obligatorio");
        }

        categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Categoría no encontrada: ID " + id));

        List<Producto> productos = productoRepository.findByCategoriaIdAndActivoTrue(id);
        for (Producto producto : productos) {
            productoRepository.updateDisponible(producto.getId(), disponible);
        }

        return ResponseEntity.ok(Map.of(
                "mensaje", "Todos los productos de la categoría marcados como " +
                        (disponible ? "disponibles" : "agotados"),
                "productosActualizados", String.valueOf(productos.size())
        ));
    }
}
