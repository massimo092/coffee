package com.safareyes.cafe.service;

import com.safareyes.cafe.Datos.CrearPedidoRequest;
import com.safareyes.cafe.Datos.LineaPedidoRequest;
import com.safareyes.cafe.modelo.Cupon;
import com.safareyes.cafe.modelo.LineaPedido;
import com.safareyes.cafe.modelo.Pedido;
import com.safareyes.cafe.modelo.Producto;
import com.safareyes.cafe.modelo.Usuario;
import com.safareyes.cafe.repository.ProductoRepository;
import com.safareyes.cafe.repository.cuponRepository;
import com.safareyes.cafe.repository.lineaPedidoRepository;
import com.safareyes.cafe.repository.pedidoRepository;
import com.safareyes.cafe.repository.usuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceReglasNegocioTest {

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
    void aplicaElMayorDescuentoYRedondeaTodosLosImportes() {
        Producto producto = Producto.builder()
                .precio(new BigDecimal("100.05"))
                .activo(true)
                .disponible(true)
                .build();
        Usuario socio = Usuario.builder()
                .email("socio@example.com")
                .rol("SOCIO_FITZONE")
                .build();
        Cupon cupon = new Cupon();
        cupon.setId(7);
        cupon.setTipo("PORCENTAJE");
        cupon.setValor(new BigDecimal("15"));
        cupon.setUsosActuales(0);

        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(usuarioRepository.findByEmail("socio@example.com")).thenReturn(Optional.of(socio));
        when(pedidoRepository.contarPedidosEnIntervalo(any(), any())).thenReturn(0L);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cuponService.validarCupon("PROMO", new BigDecimal("200.10"), "socio@example.com"))
                .thenReturn(cupon);
        when(cuponService.calcularDescuentoCupon(cupon, new BigDecimal("200.10")))
                .thenReturn(new BigDecimal("30.02"));
        when(cuponRepository.incrementarUsoSiDisponible(7)).thenReturn(1);

        Pedido pedido = pedidoService.crearPedido(new CrearPedidoRequest(
                List.of(new LineaPedidoRequest(1, 2)), "PROMO", "socio@example.com"));

        assertEquals(new BigDecimal("200.10"), pedido.getSubtotal());
        assertEquals(new BigDecimal("30.02"), pedido.getDescuento());
        assertEquals(new BigDecimal("170.08"), pedido.getTotal());
        assertEquals(new BigDecimal("154.62"), pedido.getBaseImponible());
        assertEquals(new BigDecimal("15.46"), pedido.getIva());
    }

    @Test
    void limitaElDescuentoAlSubtotalParaNoExcederlo() {
        Producto producto = Producto.builder()
                .precio(new BigDecimal("10.00"))
                .activo(true)
                .disponible(true)
                .build();
        Cupon cupon = new Cupon();
        cupon.setId(8);
        cupon.setTipo("IMPORTE");
        cupon.setValor(new BigDecimal("50.00"));
        cupon.setUsosActuales(0);

        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(pedidoRepository.contarPedidosEnIntervalo(any(), any())).thenReturn(0L);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cuponService.validarCupon("IMPORTE", new BigDecimal("10.00"), null)).thenReturn(cupon);
        when(cuponService.calcularDescuentoCupon(cupon, new BigDecimal("10.00")))
                .thenReturn(new BigDecimal("10.00"));
        when(cuponRepository.incrementarUsoSiDisponible(8)).thenReturn(1);

        Pedido pedido = pedidoService.crearPedido(new CrearPedidoRequest(
                List.of(new LineaPedidoRequest(1, 1)), "IMPORTE", null));

        assertEquals(new BigDecimal("10.00"), pedido.getDescuento());
        assertEquals(new BigDecimal("0.00"), pedido.getTotal());
    }

    @Test
    void noConsumeUnCuponCuandoElDescuentoDeSocioEsMayor() {
        Producto producto = Producto.builder()
                .precio(new BigDecimal("100.00"))
                .activo(true)
                .disponible(true)
                .build();
        Usuario socio = Usuario.builder()
                .email("socio@example.com")
                .rol("SOCIO_FITZONE")
                .build();
        Cupon cupon = new Cupon();
        cupon.setId(9);
        cupon.setTipo("PORCENTAJE");
        cupon.setValor(new BigDecimal("5"));

        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(usuarioRepository.findByEmail("socio@example.com")).thenReturn(Optional.of(socio));
        when(pedidoRepository.contarPedidosEnIntervalo(any(), any())).thenReturn(0L);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cuponService.validarCupon("MENOR", new BigDecimal("100.00"), "socio@example.com"))
                .thenReturn(cupon);
        when(cuponService.calcularDescuentoCupon(cupon, new BigDecimal("100.00")))
                .thenReturn(new BigDecimal("5.00"));

        Pedido pedido = pedidoService.crearPedido(new CrearPedidoRequest(
                List.of(new LineaPedidoRequest(1, 1)), "MENOR", "socio@example.com"));

        assertEquals(new BigDecimal("10.00"), pedido.getDescuento());
        assertNull(pedido.getCuponId());
        verify(cuponRepository, never()).incrementarUsoSiDisponible(9);
    }

    @Test
    void agrupaProductosDuplicadosYGuardaElPrecioUnitarioVigente() {
        Producto producto = Producto.builder()
                .precio(new BigDecimal("7.99"))
                .activo(true)
                .disponible(true)
                .build();
        List<LineaPedido> lineasGuardadas = new ArrayList<>();

        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(pedidoRepository.contarPedidosEnIntervalo(any(), any())).thenReturn(0L);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(lineaPedidoRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<LineaPedido> lineas = invocation.getArgument(0);
            lineas.forEach(lineasGuardadas::add);
            return lineasGuardadas;
        });

        pedidoService.crearPedido(new CrearPedidoRequest(List.of(
                new LineaPedidoRequest(1, 3), new LineaPedidoRequest(1, 2)), null, null));

        assertEquals(1, lineasGuardadas.size());
        assertEquals(5, lineasGuardadas.getFirst().getCantidad());
        assertEquals(new BigDecimal("7.99"), lineasGuardadas.getFirst().getPrecio_unitario());
    }

    @Test
    void bloqueaElTurnoAntesDeContarPedidosEnElIntervaloDelDia() {
        Producto producto = Producto.builder()
                .precio(new BigDecimal("1.00"))
                .activo(true)
                .disponible(true)
                .build();

        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(pedidoRepository.contarPedidosEnIntervalo(any(), any())).thenReturn(0L);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido pedido = pedidoService.crearPedido(new CrearPedidoRequest(
                List.of(new LineaPedidoRequest(1, 1)), null, null));

        var inicio = ArgumentCaptor.forClass(LocalDateTime.class);
        var fin = ArgumentCaptor.forClass(LocalDateTime.class);
        var sql = ArgumentCaptor.forClass(String.class);
        var orden = inOrder(jdbcTemplate, pedidoRepository);
        orden.verify(jdbcTemplate).execute(sql.capture());
        orden.verify(pedidoRepository).contarPedidosEnIntervalo(inicio.capture(), fin.capture());
        assertTrue(sql.getValue().contains("pg_advisory_xact_lock"));
        assertEquals(inicio.getValue().toLocalDate().plusDays(1).atStartOfDay(), fin.getValue());
        assertEquals(inicio.getValue().toLocalDate(), pedido.getFechaHora().toLocalDate());
        assertTrue(pedido.getNumeroTurno() > 0);
    }
}