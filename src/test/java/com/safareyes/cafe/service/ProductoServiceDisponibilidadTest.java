package com.safareyes.cafe.service;

import com.safareyes.cafe.Datos.ProductoDisponibilidadResponse;
import com.safareyes.cafe.modelo.Producto;
import com.safareyes.cafe.repository.ProductoRepository;
import com.safareyes.cafe.repository.alergenoRepository;
import com.safareyes.cafe.repository.categoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceDisponibilidadTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private categoriaRepository categoriaRepository;

    @Mock
    private alergenoRepository alergenoRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void marcaProductoComoAgotado() {
        Producto producto = Producto.builder().id(1).disponible(true).build();
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(productoRepository.save(producto)).thenReturn(producto);

        ProductoDisponibilidadResponse respuesta = productoService.actualizarDisponibilidad(1, false);

        assertEquals(1, respuesta.id());
        assertFalse(respuesta.disponible());
        verify(productoRepository).save(producto);
    }

    @Test
    void marcaProductoComoDisponible() {
        Producto producto = Producto.builder().id(1).disponible(false).build();
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(productoRepository.save(producto)).thenReturn(producto);

        ProductoDisponibilidadResponse respuesta = productoService.actualizarDisponibilidad(1, true);

        assertEquals(1, respuesta.id());
        assertTrue(respuesta.disponible());
        verify(productoRepository).save(producto);
    }

    @Test
    void devuelve404CuandoNoExisteElProducto() {
        when(productoRepository.findById(42)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> productoService.actualizarDisponibilidad(42, true));

        assertEquals(404, exception.getStatusCode().value());
    }
}