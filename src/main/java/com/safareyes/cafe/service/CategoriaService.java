package com.safareyes.cafe.service;

import com.safareyes.cafe.Datos.CategoriaDisponibilidadResponse;
import com.safareyes.cafe.modelo.Categoria;
import com.safareyes.cafe.modelo.Producto;
import com.safareyes.cafe.repository.ProductoRepository;
import com.safareyes.cafe.repository.categoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final categoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Transactional
    public CategoriaDisponibilidadResponse actualizarDisponibilidad(Integer id, Boolean disponible) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Categoría no encontrada: ID " + id));

        categoria.setDisponibilidad(disponible);
        categoriaRepository.save(categoria);

        List<Producto> productos = productoRepository.findByCategoriaIdAndActivoTrue(categoria.getId());
        productos.forEach(producto -> producto.setDisponible(disponible));
        productoRepository.saveAll(productos);

        return new CategoriaDisponibilidadResponse(categoria.getId(), disponible, productos.size());
    }
}