package com.safareyes.cafe.service;

import com.safareyes.cafe.Datos.CuponValidarDTO;
import com.safareyes.cafe.Exceptions.CuponInvalidoException;
import com.safareyes.cafe.modelo.Cupon;
import com.safareyes.cafe.repository.cuponRepository;
import com.safareyes.cafe.response.ValidarCuponResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuponServiceTest {

    @Mock
    private cuponRepository cuponRepository;

    @InjectMocks
    private CuponService cuponService;

    @Test
    void acceptsActiveCouponWithinDatesAndMatchingPersonalEmail() {
        Cupon cupon = cuponValido();
        cupon.setImporteMinimo(new BigDecimal("100.00"));
        cupon.setEmailCliente("cliente@example.com");
        when(cuponRepository.findByCodigoIgnoreCase("prueba25")).thenReturn(Optional.of(cupon));

        ValidarCuponResponse response = cuponService.validar(
                new CuponValidarDTO(" prueba25 ", new BigDecimal("100.01"), " CLIENTE@example.com "),
                " CLIENTE@example.com ");

        assertEquals("PRUEBA25", response.getCodigo());
        assertEquals(new BigDecimal("25.00"), response.getDescuento());
        assertEquals(new BigDecimal("75.01"), response.getTotalConDescuento());
        assertEquals(0, cupon.getUsosActuales());
    }

    @Test
    void acceptsCouponWithoutOptionalDatesOrUsageLimit() {
        Cupon cupon = cuponValido();
        cupon.setFechaInicio(null);
        cupon.setFechaFin(null);
        cupon.setUsosActuales(null);
        cupon.setUsosMaximos(null);
        when(cuponRepository.findByCodigoIgnoreCase("PRUEBA25")).thenReturn(Optional.of(cupon));

        ValidarCuponResponse response = cuponService.validar(
                new CuponValidarDTO("PRUEBA25", new BigDecimal("100.00"), null), null);

        assertEquals(new BigDecimal("75.00"), response.getTotalConDescuento());
    }

    @Test
    void rejectsUnknownCoupon() {
        when(cuponRepository.findByCodigoIgnoreCase("NO-EXISTE")).thenReturn(Optional.empty());

        assertThrows(CuponInvalidoException.class, () -> validar("NO-EXISTE", "100.00", null));
    }

    @Test
    void rejectsInactiveCoupon() {
        Cupon cupon = cuponValido();
        cupon.setActivo(false);

        assertRejected(cupon, "100.00", null);
    }

    @Test
    void rejectsCouponBeforeStartDate() {
        Cupon cupon = cuponValido();
        cupon.setFechaInicio(LocalDateTime.now().plusDays(1));

        assertRejected(cupon, "100.00", null);
    }

    @Test
    void rejectsExpiredCoupon() {
        Cupon cupon = cuponValido();
        cupon.setFechaFin(LocalDateTime.now().minusDays(1));

        assertRejected(cupon, "100.00", null);
    }

    @Test
    void rejectsCouponAtUsageLimit() {
        Cupon cupon = cuponValido();
        cupon.setUsosActuales(5);
        cupon.setUsosMaximos(5);

        assertRejected(cupon, "100.00", null);
    }

    @Test
    void rejectsSubtotalBelowMinimum() {
        Cupon cupon = cuponValido();
        cupon.setImporteMinimo(new BigDecimal("20.00"));

        assertRejected(cupon, "19.99", null);
    }

    @Test
    void rejectsSubtotalEqualToMinimum() {
        Cupon cupon = cuponValido();
        cupon.setImporteMinimo(new BigDecimal("20.00"));

        assertRejected(cupon, "20.00", null);
    }

    @Test
    void couponDiscountCannotMakeTheTotalNegative() {
        Cupon cupon = cuponValido();
        cupon.setValor(new BigDecimal("150"));
        when(cuponRepository.findByCodigoIgnoreCase("PRUEBA25")).thenReturn(Optional.of(cupon));

        ValidarCuponResponse response = cuponService.validar(
                new CuponValidarDTO("PRUEBA25", new BigDecimal("100.00"), null), null);

        assertEquals(new BigDecimal("100.00"), response.getDescuento());
        assertEquals(new BigDecimal("0.00"), response.getTotalConDescuento());
    }

    @Test
    void rejectsPersonalCouponWithoutEmail() {
        Cupon cupon = cuponValido();
        cupon.setEmailCliente("cliente@example.com");

        assertRejected(cupon, "100.00", null);
    }

    @Test
    void rejectsPersonalCouponWithDifferentEmail() {
        Cupon cupon = cuponValido();
        cupon.setEmailCliente("cliente@example.com");

        assertRejected(cupon, "100.00", "otro@example.com");
    }

    private void assertRejected(Cupon cupon, String subtotal, String emailCliente) {
        when(cuponRepository.findByCodigoIgnoreCase("PRUEBA25")).thenReturn(Optional.of(cupon));
        assertThrows(CuponInvalidoException.class, () -> validar("PRUEBA25", subtotal, emailCliente));
    }

    private void validar(String codigo, String subtotal, String emailCliente) {
        cuponService.validar(new CuponValidarDTO(codigo, new BigDecimal(subtotal), emailCliente), emailCliente);
    }

    private Cupon cuponValido() {
        Cupon cupon = new Cupon();
        cupon.setCodigo("PRUEBA25");
        cupon.setTipo("PORCENTAJE");
        cupon.setValor(new BigDecimal("25"));
        cupon.setFechaInicio(LocalDateTime.now().minusDays(1));
        cupon.setFechaFin(LocalDateTime.now().plusDays(1));
        cupon.setUsosActuales(0);
        cupon.setUsosMaximos(10);
        cupon.setImporteMinimo(new BigDecimal("10.00"));
        cupon.setActivo(true);
        return cupon;
    }
}