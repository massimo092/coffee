package com.safareyes.cafe.service;

import com.safareyes.cafe.Datos.ProductoActivoResponse;
import com.safareyes.cafe.modelo.Producto;
import com.safareyes.cafe.repository.ProductoRepository;
import com.safareyes.cafe.repository.alergenoRepository;
import com.safareyes.cafe.repository.categoriaRepository;
import jakarta.persistence.EntityNotFoundException;
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
class ProductoServiceActivoTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private categoriaRepository categoriaRepository;

    @Mock
    private alergenoRepository alergenoRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void daDeBajaLogicaElProducto() {
        Producto producto = Producto.builder().id(1).activo(true).build();
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(productoRepository.save(producto)).thenReturn(producto);

        ProductoActivoResponse respuesta = productoService.actualizarActivo(1, false);

        assertEquals(1, respuesta.id());
        assertFalse(respuesta.activo());
        verify(productoRepository).save(producto);
    }

    @Test
    void permiteReactivarElProducto() {
        Producto producto = Producto.builder().id(1).activo(false).build();
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(productoRepository.save(producto)).thenReturn(producto);

        ProductoActivoResponse respuesta = productoService.actualizarActivo(1, true);

        assertEquals(1, respuesta.id());
        assertTrue(respuesta.activo());
        verify(productoRepository).save(producto);
    }

    @Test
    void devuelve404CuandoNoExisteElProducto() {
        when(productoRepository.findById(42)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> productoService.actualizarActivo(42, false));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void noEncuentraUnProductoDadoDeBajaEnElDetalle() {
        when(productoRepository.findByIdWithAlergenos(42)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> productoService.detalle(42));
    }
}