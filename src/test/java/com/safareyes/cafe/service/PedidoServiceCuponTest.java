package com.safareyes.cafe.service;

import com.safareyes.cafe.Datos.CrearPedidoRequest;
import com.safareyes.cafe.Datos.LineaPedidoRequest;
import com.safareyes.cafe.modelo.Cupon;
import com.safareyes.cafe.modelo.Pedido;
import com.safareyes.cafe.modelo.Producto;
import com.safareyes.cafe.repository.ProductoRepository;
import com.safareyes.cafe.repository.cuponRepository;
import com.safareyes.cafe.repository.lineaPedidoRepository;
import com.safareyes.cafe.repository.pedidoRepository;
import com.safareyes.cafe.repository.usuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceCuponTest {

    @Mock
    private pedidoRepository pedidoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private cuponRepository cuponRepository;

    @Mock
    private usuarioRepository usuarioRepository;

    @Mock
    private lineaPedidoRepository lineaPedidoRepository;

    @Mock
    private CuponService cuponService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void applyingCouponToOrderAssociatesItAndIncrementsUsageOnce() {
        Producto producto = Producto.builder()
                .precio(new BigDecimal("10.00"))
                .activo(true)
                .disponible(true)
                .build();
        Cupon cupon = new Cupon();
        cupon.setId(1);
        cupon.setTipo("IMPORTE");
        cupon.setUsosActuales(2);
        cupon.setValor(new BigDecimal("5.00"));

        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(pedidoRepository.contarPedidosEnIntervalo(any(), any())).thenReturn(0L);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cuponService.validarCupon("PRUEBA", new BigDecimal("10.00"), null)).thenReturn(cupon);
        when(cuponService.calcularDescuentoCupon(cupon, new BigDecimal("10.00")))
                .thenReturn(new BigDecimal("5.00"));
        when(cuponRepository.incrementarUsoSiDisponible(1)).thenReturn(1);

        CrearPedidoRequest request = new CrearPedidoRequest(
                List.of(new LineaPedidoRequest(1, 1)), "PRUEBA", null);
        Pedido pedido = pedidoService.crearPedido(request);

        assertSame(cupon, pedido.getCuponId());
        assertEquals(2, cupon.getUsosActuales());
        verify(cuponRepository).incrementarUsoSiDisponible(1);
    }

    @Test
    void rejectsUnavailableProductWith422AndProductName() {
        Producto producto = Producto.builder()
                .nombre("Cappuccino")
                .activo(true)
                .disponible(false)
                .build();
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> pedidoService.crearPedido(pedidoRequest()));

        assertEquals(422, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Cappuccino"));
        assertTrue(exception.getReason().contains("agotado"));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void rejectsInactiveProductWith422AndProductName() {
        Producto producto = Producto.builder()
                .nombre("Tarta de queso")
                .activo(false)
                .disponible(true)
                .build();
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> pedidoService.crearPedido(pedidoRequest()));

        assertEquals(422, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Tarta de queso"));
        assertTrue(exception.getReason().contains("dado de baja"));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    private CrearPedidoRequest pedidoRequest() {
        return new CrearPedidoRequest(List.of(new LineaPedidoRequest(1, 1)), null, null);
    }
}