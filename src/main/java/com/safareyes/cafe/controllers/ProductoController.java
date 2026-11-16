package com.safareyes.cafe.controllers;

import com.safareyes.cafe.Datos.ProductoCreateDto;
import com.safareyes.cafe.modelo.Producto;
import com.safareyes.cafe.repository.ProductoRepository;
import com.safareyes.cafe.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }
    //Carta paginada con filtros opcionales -GET 1
    @GetMapping
    public Page<ProductoCreateDto> getCarta(
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false, defaultValue = "") String buscar,
            Pageable pageable) {
        return productoService.listarCarta(categoriaId, buscar, pageable);
    }

    //detalle con alérgenos -GET 2
    @GetMapping("/{id}")
    public ResponseEntity<ProductoCreateDto> getDetalle(@PathVariable Integer id) {
        return ResponseEntity.ok(productoService.detalle(id));
    }
    //Creacion de un producto -POST
    @PostMapping
    public ResponseEntity<ProductoCreateDto> crear(@Valid @RequestBody ProductoCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(dto));
    }
}