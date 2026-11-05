package com.safareyes.cafe.controllers;

import com.safareyes.cafe.Datos.ProductoResponse;
import com.safareyes.cafe.modelo.Alergeno;
import com.safareyes.cafe.modelo.Producto;
import com.safareyes.cafe.repository.productoRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final productoRepository productoRepository;

    public ProductoController(productoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // Carta paginada con filtros opcionales (RF-02) - Público
    @GetMapping
    @Transactional(readOnly = true)
    public Page<ProductoResponse> getCarta(
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(defaultValue = "") String buscar,
            Pageable pageable) {

        Page<Producto> pagina;
        if (categoriaId == null) {
            pagina = productoRepository.findByActivoTrueAndNombreContainingIgnoreCase(buscar, pageable);
        } else {
            pagina = productoRepository.findByActivoTrueAndCategoriaIdAndNombreContainingIgnoreCase(
                    categoriaId, buscar, pageable);
        }
        return pagina.map(this::aRespuesta);
    }

    // Detalle con alérgenos - Público
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ProductoResponse> getDetalle(@PathVariable Integer id) {
        Producto producto = productoRepository.findByIdWithAlergenos(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado: ID " + id));
        return ResponseEntity.ok(aRespuesta(producto));
    }

    // Alta de producto - ADMIN
    @PostMapping
    @Transactional
    public ResponseEntity<ProductoResponse> crearProducto(@Valid @RequestBody Producto producto) {
        producto.setActivo(true);
        producto.setDisponible(true);
        Producto saved = productoRepository.save(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(aRespuesta(saved));
    }

    // Modificación de producto - ADMIN
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ProductoResponse> modificarProducto(@PathVariable Integer id,
                                                              @Valid @RequestBody Producto productoRequest) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado: ID " + id));

        producto.setNombre(productoRequest.getNombre());
        producto.setDescripcion(productoRequest.getDescripcion());
        producto.setPrecio(productoRequest.getPrecio());
        producto.setCategoria(productoRequest.getCategoria());

        Producto saved = productoRepository.save(producto);
        return ResponseEntity.ok(aRespuesta(saved));
    }

    // Marcar disponible o agotado - ADMIN, BARISTA
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

    // Baja lógica - ADMIN
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

    // Convierte Producto -> ProductoResponse
    private ProductoResponse aRespuesta(Producto p) {
        String categoria = (p.getCategoria() != null) ? p.getCategoria().getNombre() : null;

        List<String> alergenos = (p.getAlergenos() != null)
                ? p.getAlergenos().stream().map(Alergeno::getNombre).toList()
                : List.of();

        return new ProductoResponse(
                p.getId(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecio(),
                p.getDisponible(),
                categoria,
                alergenos);
    }
}