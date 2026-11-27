package com.safareyes.cafe.service;

import com.safareyes.cafe.Datos.CambiarEstadoRequest;
import com.safareyes.cafe.modelo.Cupon;
import com.safareyes.cafe.modelo.Pedido;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceEstadoTest {

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

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void permiteLaSecuenciaDeTransicionesDeRn08() {
        Pedido pedido = Pedido.builder().id(1).estado("pendiente").build();
        when(pedidoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        pedidoService.cambiarEstado(1, new CambiarEstadoRequest("en_preparacion"));
        assertEquals("en_preparacion", pedido.getEstado());

        pedidoService.cambiarEstado(1, new CambiarEstadoRequest("listo"));
        assertEquals("listo", pedido.getEstado());

        Pedido actualizado = pedidoService.cambiarEstado(1, new CambiarEstadoRequest("entregado"));

        assertEquals("entregado", actualizado.getEstado());
        verify(pedidoRepository, times(3)).save(pedido);
    }

    @Test
    void aceptaEstadosDeRn08EnMayusculas() {
        Pedido pedido = Pedido.builder().id(1).estado("PENDIENTE").build();
        when(pedidoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        Pedido actualizado = pedidoService.cambiarEstado(1, new CambiarEstadoRequest("EN_PREPARACION"));

        assertEquals("en_preparacion", actualizado.getEstado());
    }

    @Test
    void rechazaUnaTransicionNoPermitidaSinGuardarElPedido() {
        Pedido pedido = Pedido.builder().id(1).estado("pendiente").build();
        when(pedidoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(pedido));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> pedidoService.cambiarEstado(1, new CambiarEstadoRequest("entregado")));

        assertEquals(422, exception.getStatusCode().value());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void alCancelarRestauraElUsoDelCupon() {
        Cupon cupon = new Cupon();
        cupon.setId(1);
        cupon.setUsosActuales(2);
        Pedido pedido = Pedido.builder().id(1).estado("pendiente").cuponId(cupon).build();
        when(pedidoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(pedido));
        when(cuponRepository.devolverUso(1)).thenReturn(1);
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        Pedido actualizado = pedidoService.cambiarEstado(1, new CambiarEstadoRequest("cancelado"));

        assertEquals("cancelado", actualizado.getEstado());
        assertEquals(2, cupon.getUsosActuales());
        verify(cuponRepository).devolverUso(1);
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void noPermiteCancelarUnPedidoEnPreparacionNiDevuelveElCupon() {
        Cupon cupon = new Cupon();
        cupon.setId(1);
        cupon.setUsosActuales(2);
        Pedido pedido = Pedido.builder().id(1).estado("en_preparacion").cuponId(cupon).build();
        when(pedidoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(pedido));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> pedidoService.cambiarEstado(1, new CambiarEstadoRequest("cancelado")));

        assertEquals(422, exception.getStatusCode().value());
        assertEquals(2, cupon.getUsosActuales());
        verify(cuponRepository, never()).devolverUso(1);
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }
}