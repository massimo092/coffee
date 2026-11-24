package com.safareyes.cafe.service;

import com.safareyes.cafe.Datos.CuponCreateDto;
import com.safareyes.cafe.Datos.CuponValidarDTO;
import com.safareyes.cafe.Exceptions.CuponInvalidoException;
import com.safareyes.cafe.modelo.Cupon;
import com.safareyes.cafe.repository.cuponRepository;
import com.safareyes.cafe.response.ValidarCuponResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CuponService {
    private final cuponRepository cuponRepository;

    @Transactional
    public CuponCreateDto crearCupon(CuponCreateDto dto) {
        Cupon cupon = new Cupon();
        cupon.setCodigo(dto.getCodigo());
        cupon.setTipo(dto.getTipo());
        cupon.setValor(dto.getValor());
        cupon.setFechaInicio(dto.getFechaInicio());
        cupon.setFechaFin(dto.getFechaFin());
        cupon.setUsosMaximos(dto.getUsosMaximos());
        cupon.setUsosActuales(0);
        cupon.setImporteMinimo(dto.getImporteMinimo());
        cupon.setEmailCliente(dto.getEmailCliente());
        cupon.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        Cupon guardado = cuponRepository.save(cupon);




        CuponCreateDto resultado = new CuponCreateDto();
        resultado.setId(guardado.getId());
        resultado.setCodigo(guardado.getCodigo());
        resultado.setTipo(guardado.getTipo());
        resultado.setValor(guardado.getValor());
        resultado.setFechaInicio(guardado.getFechaInicio());
        resultado.setFechaFin(guardado.getFechaFin());
        resultado.setUsosMaximos(guardado.getUsosMaximos());
        resultado.setUsosActuales(guardado.getUsosActuales());
        resultado.setImporteMinimo(guardado.getImporteMinimo());
        resultado.setEmailCliente(guardado.getEmailCliente());
        resultado.setActivo(guardado.getActivo());
        return resultado;
    }

    @Transactional(readOnly = true)
    public ValidarCuponResponse validar(CuponValidarDTO req, String emailCliente) {
        if (req == null) {
            throw new CuponInvalidoException("Los datos de validación son obligatorios");
        }

        Cupon cupon = validarCupon(req.getCodigo(), req.getSubtotal(), emailCliente);
        if (cupon.getTipo() == null || cupon.getValor() == null) {
            throw new CuponInvalidoException("El cupón tiene una configuración inválida");
        }

        BigDecimal descuento = switch (cupon.getTipo().toLowerCase(Locale.ROOT)) {
            case "porcentaje" -> req.getSubtotal().multiply(cupon.getValor())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case "importe" -> cupon.getValor().min(req.getSubtotal());
            default -> throw new CuponInvalidoException("Tipo de cupón desconocido: " + cupon.getTipo());
        };

        return new ValidarCuponResponse(cupon.getCodigo(), descuento,
                req.getSubtotal().subtract(descuento));
    }

    @Transactional(readOnly = true)
    public Cupon validarCupon(String codigo, BigDecimal subtotal, String emailCliente) {
        if (codigo == null || codigo.isBlank()) {
            throw new CuponInvalidoException("El código del cupón es obligatorio");
        }
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new CuponInvalidoException("El subtotal debe ser mayor o igual a cero");
        }

        Cupon cupon = cuponRepository.findByCodigoIgnoreCase(codigo.trim())
                .orElseThrow(() -> new CuponInvalidoException("El cupón no existe"));
        LocalDateTime ahora = LocalDateTime.now();

        if (!Boolean.TRUE.equals(cupon.getActivo())) {
            throw new CuponInvalidoException("El cupón no está activo");
        }
        if ((cupon.getFechaInicio() != null && ahora.isBefore(cupon.getFechaInicio()))
                || (cupon.getFechaFin() != null && ahora.isAfter(cupon.getFechaFin()))) {
            throw new CuponInvalidoException("El cupón está fuera de su periodo de validez");
        }

        int usosActuales = cupon.getUsosActuales() != null ? cupon.getUsosActuales() : 0;
        if (cupon.getUsosMaximos() != null && usosActuales >= cupon.getUsosMaximos()) {
            throw new CuponInvalidoException("El cupón ha agotado sus usos");
        }
        if (cupon.getImporteMinimo() != null && subtotal.compareTo(cupon.getImporteMinimo()) < 0) {
            throw new CuponInvalidoException("El importe mínimo es " + cupon.getImporteMinimo());
        }

        String emailAsignado = cupon.getEmailCliente();
        if (emailAsignado != null && !emailAsignado.isBlank()) {
            if (emailCliente == null || emailCliente.isBlank()) {
                throw new CuponInvalidoException("Cupón personal: indica el email del cliente");
            }
            if (!emailAsignado.trim().equalsIgnoreCase(emailCliente.trim())) {
                throw new CuponInvalidoException("Este cupón no es válido para este cliente");
            }
        }

        return cupon;
    }
}
