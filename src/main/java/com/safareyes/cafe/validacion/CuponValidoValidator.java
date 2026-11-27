package com.safareyes.cafe.validacion;

import com.safareyes.cafe.Datos.CuponValidarDTO;
import com.safareyes.cafe.Exceptions.CuponInvalidoException;
import com.safareyes.cafe.service.CuponService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class CuponValidoValidator implements ConstraintValidator<CuponValido, CuponValidarDTO> {

    @Autowired
    private CuponService cuponService;

    @Override
    public boolean isValid(CuponValidarDTO dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        try {
            cuponService.validarCupon(dto.getCodigo(), dto.getSubtotal(), dto.getEmailCliente());
            return true;
        } catch (CuponInvalidoException exception) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(exception.getMessage())
                    .addConstraintViolation();
            return false;
        }
    }
}