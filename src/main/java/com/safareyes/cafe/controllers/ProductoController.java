package com.safareyes.cafe.controllers;

import com.safareyes.cafe.modelo.Producto;
import com.safareyes.cafe.repository.productoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    @Autowired
    private productoRepository productoRepository;

    // 1.  - Carta paginada con filtros opcionales (RF-02) - Público
    @GetMapping
    public Page<Producto> getCarta(
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Boolean disponible,
            @RequestParam(required = false) String buscar,
            Pageable pageable) {
        return productoRepository.findByCartaFiltros(categoriaId, disponible, buscar, pageable);
    }

    // 2. - Detalle con alérgenos - Público
    @GetMapping("/{id}")
    public ResponseEntity<Producto> getDetalle(@PathVariable Integer id) {
        Producto producto = productoRepository.findByIdWithAlergenos(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado: ID " + id));
        return ResponseEntity.ok(producto);
    }

    // 3. - Alta de producto - ADMIN
    @PostMapping
    public ResponseEntity<Producto> crearProducto(@Valid @RequestBody Producto producto) {
        producto.setActivo(true);
        producto.setDisponible(true);
        Producto saved = productoRepository.save(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 4. - Modificación de producto - ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<Producto> modificarProducto(@PathVariable Integer id,
                                                       @Valid @RequestBody Producto productoRequest) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado: ID " + id));

        producto.setNombre(productoRequest.getNombre());
        producto.setDescripcion(productoRequest.getDescripcion());
        producto.setPrecio(productoRequest.getPrecio());
        producto.setCategoria(productoRequest.getCategoria());

        Producto saved = productoRepository.save(producto);
        return ResponseEntity.ok(saved);
    }

    // 5. - Marcar disponible o agotado - ADMIN, BARISTA
    @PatchMapping("/{id}/disponibilidad")
    @Transactional
    public ResponseEntity<Map<String, String>> cambiarDisponible(@PathVariable Integer id,
                                                                  @RequestBody Map<String, Boolean> body) {
        Boolean disponible = body.get("disponible");
        if (disponible == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "El campo 'disponible' es obligatorio");
        }

        productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado: ID " + id));

        productoRepository.updateDisponible(id, disponible);
        return ResponseEntity.ok(Map.of("mensaje",
                "Producto marcado como " + (disponible ? "disponible" : "agotado")));
    }

    // 6. - Baja lógica - ADMIN
    @PatchMapping("/{id}/baja")
    @Transactional
    public ResponseEntity<Map<String, String>> darBaja(@PathVariable Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado: ID " + id));

        producto.setActivo(false);
        productoRepository.save(producto);
        return ResponseEntity.ok(Map.of("mensaje", "Producto dado de baja correctamente"));
    }
}
