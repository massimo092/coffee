package com.safareyes.cafe.service;

import com.safareyes.cafe.Datos.ProductoCreateDto;
import com.safareyes.cafe.modelo.Alergeno;
import com.safareyes.cafe.modelo.Categoria;
import com.safareyes.cafe.modelo.Producto;
import com.safareyes.cafe.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final categoriaRepository categoriaRepository;
    private final alergenoRepository alergenoRepository;

    // POST
    @Transactional
    public ProductoCreateDto crear(ProductoCreateDto dto) {
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setCategoria(categoria);
        producto.setDisponible(true);
        producto.setActivo(true);

        if (dto.getAlergenosIds() != null && !dto.getAlergenosIds().isEmpty()) {
            producto.setAlergenos(alergenoRepository.findAllById(dto.getAlergenosIds()));
        } else {
            producto.setAlergenos(new ArrayList<>());
        }

        Producto guardado = productoRepository.save(producto);
        dto.setId(guardado.getId());
        return dto;
    }

    // GET carta
    @Transactional(readOnly = true)
    public Page<ProductoCreateDto> listarCarta(Integer categoriaId, String buscar, Pageable pageable) {
        Page<Producto> pagina = (categoriaId != null)
                ? productoRepository.findByActivoTrueAndCategoriaIdAndNombreContainingIgnoreCase(categoriaId, buscar, pageable)
                : productoRepository.findByActivoTrueAndNombreContainingIgnoreCase(buscar, pageable);
        return pagina.map(this::toDto);
    }

    // GET detalle
    @Transactional(readOnly = true)
    public ProductoCreateDto detalle(Integer id) {
        Producto p = productoRepository.findByIdWithAlergenos(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: ID " + id));
        return toDto(p);
    }

    // Entidad -> DTO
    private ProductoCreateDto toDto(Producto p) {
        ProductoCreateDto dto = new ProductoCreateDto();
        dto.setId(p.getId());
        dto.setNombre(p.getNombre());
        dto.setDescripcion(p.getDescripcion());
        dto.setPrecio(p.getPrecio());
        dto.setCategoriaId(p.getCategoria() != null ? p.getCategoria().getId() : null);
        dto.setAlergenosIds(p.getAlergenos().stream()
                .map(Alergeno::getId)
                .collect(Collectors.toSet()));
        return dto;
    }
}