package com.safareyes.cafe.validacion;

import com.safareyes.cafe.Datos.CuponValidarDTO;
import com.safareyes.cafe.Exceptions.CuponInvalidoException;
import com.safareyes.cafe.modelo.Cupon;
import com.safareyes.cafe.service.CuponService;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuponValidoValidatorTest {

    @Mock
    private CuponService cuponService;

    @InjectMocks
    private CuponValidoValidator validator;

    @Test
    void acceptsCouponWhenServiceValidationSucceeds() {
        CuponValidarDTO dto = new CuponValidarDTO("PRUEBA25", new BigDecimal("100.00"), "cliente@example.com");
        when(cuponService.validarCupon("PRUEBA25", new BigDecimal("100.00"), "cliente@example.com"))
                .thenReturn(new Cupon());

        assertTrue(validator.isValid(dto, null));
    }

    @Test
    void reportsServiceRejectionAsConstraintViolation() {
        String motivo = "El cupón no está activo";
        CuponValidarDTO dto = new CuponValidarDTO("PRUEBA25", new BigDecimal("100.00"), null);
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder =
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(cuponService.validarCupon("PRUEBA25", new BigDecimal("100.00"), null))
                .thenThrow(new CuponInvalidoException(motivo));
        when(context.buildConstraintViolationWithTemplate(motivo)).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);

        assertFalse(validator.isValid(dto, context));

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(motivo);
        verify(violationBuilder).addConstraintViolation();
    }
}