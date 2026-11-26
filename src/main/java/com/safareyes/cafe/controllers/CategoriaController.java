package com.safareyes.cafe.controllers;

import com.safareyes.cafe.Datos.ContadorProductos;
import com.safareyes.cafe.Datos.ActualizarDisponibilidadRequest;
import com.safareyes.cafe.Datos.CategoriaDisponibilidadResponse;
import com.safareyes.cafe.repository.categoriaRepository;
import com.safareyes.cafe.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaController {

    @Autowired
    private categoriaRepository categoriaRepository;

    @Autowired
    private CategoriaService categoriaService;

    //Categorías con número de productos activos - GET 3
    @GetMapping
    public ResponseEntity<List<ContadorProductos>> getCategorias() {
        List<ContadorProductos> categorias = categoriaRepository.contarProductosActivos();
        return ResponseEntity.ok(categorias);
    }

    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<CategoriaDisponibilidadResponse> actualizarDisponibilidad(
            @PathVariable Integer id,
            @Valid @RequestBody ActualizarDisponibilidadRequest request) {
        return ResponseEntity.ok(categoriaService.actualizarDisponibilidad(id, request.disponible()));
    }
}
