package com.safareyes.cafe.controllers;

import com.safareyes.cafe.Datos.ContadorProductos;
import com.safareyes.cafe.repository.categoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaController {

    @Autowired
    private categoriaRepository categoriaRepository;

    //Categorías con número de productos activos - Público
    @GetMapping
    public ResponseEntity<List<ContadorProductos>> getCategorias() {
        List<ContadorProductos> categorias = categoriaRepository.contarProductosActivos();
        return ResponseEntity.ok(categorias);
    }
}
