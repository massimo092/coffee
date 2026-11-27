package com.safareyes.cafe.service;

import com.safareyes.cafe.Datos.CategoriaDisponibilidadResponse;
import com.safareyes.cafe.modelo.Categoria;
import com.safareyes.cafe.modelo.Producto;
import com.safareyes.cafe.repository.ProductoRepository;
import com.safareyes.cafe.repository.categoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceDisponibilidadTest {

    @Mock
    private categoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void agotaTodosLosProductosActivosDeLaCategoria() {
        Categoria categoria = Categoria.builder().id(1).build();
        List<Producto> productos = List.of(
                Producto.builder().id(10).disponible(true).activo(true).build(),
                Producto.builder().id(11).disponible(true).activo(true).build());
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(productoRepository.findByCategoriaIdAndActivoTrue(1)).thenReturn(productos);

        CategoriaDisponibilidadResponse respuesta = categoriaService.actualizarDisponibilidad(1, false);

        assertEquals(1, respuesta.id());
        assertFalse(respuesta.disponible());
        assertFalse(categoria.getDisponibilidad());
        assertEquals(2, respuesta.productosActualizados());
        assertTrue(productos.stream().noneMatch(Producto::getDisponible));
        verify(categoriaRepository).save(categoria);
        verify(productoRepository).saveAll(productos);
    }

    @Test
    void reponeTodosLosProductosActivosDeLaCategoria() {
        Categoria categoria = Categoria.builder().id(1).build();
        List<Producto> productos = List.of(Producto.builder().id(10).disponible(false).activo(true).build());
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(productoRepository.findByCategoriaIdAndActivoTrue(1)).thenReturn(productos);

        CategoriaDisponibilidadResponse respuesta = categoriaService.actualizarDisponibilidad(1, true);

        assertTrue(respuesta.disponible());
        assertTrue(categoria.getDisponibilidad());
        assertEquals(1, respuesta.productosActualizados());
        assertTrue(productos.getFirst().getDisponible());
        verify(categoriaRepository).save(categoria);
        verify(productoRepository).saveAll(productos);
    }

    @Test
    void devuelve404CuandoNoExisteLaCategoria() {
        when(categoriaRepository.findById(42)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> categoriaService.actualizarDisponibilidad(42, true));

        assertEquals(404, exception.getStatusCode().value());
        verify(productoRepository, never()).findByCategoriaIdAndActivoTrue(42);
    }
}