package com.safareyes.cafe.controllers;

import com.safareyes.cafe.Datos.ValidarCuponResponse;
import com.safareyes.cafe.modelo.Cupon;
import com.safareyes.cafe.repository.cuponRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cupones")
public class CuponController {

    @Autowired
    private cuponRepository cuponRepository;

    // 13. POST /api/v1/cupones - Crear cupón - ADMIN
    @PostMapping
    public ResponseEntity<Cupon> crearCupon(@Valid @RequestBody Cupon cupon) {
        // Validar que el código sea único
        if (cuponRepository.findByCodigo(cupon.getCodigo()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Ya existe un cupón con el código: " + cupon.getCodigo());
        }

        if (cupon.getUsosActuales() == null) {
            cupon.setUsosActuales(0);
        }
        if (cupon.getActivo() == null) {
            cupon.setActivo(true);
        }

        Cupon saved = cuponRepository.save(cupon);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 14. POST /api/v1/cupones/validar - Validar código para un subtotal y devolver descuento - Autenticado
    @PostMapping("/validar")
    public ResponseEntity<ValidarCuponResponse> validarCupon(@RequestBody Map<String, Object> body) {
        String codigo = (String) body.get("codigo");
        BigDecimal subtotal = (BigDecimal) body.get("subtotal");

        if (codigo == null || codigo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "El código del cupón es obligatorio");
        }
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "El subtotal debe ser mayor a 0");
        }

        Cupon cupon = cuponRepository.findByCodigo(codigo)
                .orElse(new Cupon());

        // Si no existe, devolver no válido
        if (cupon.getId() == null) {
            return ResponseEntity.ok(new ValidarCuponResponse(false, BigDecimal.ZERO,
                    "Cupón no encontrado: " + codigo));
        }

        // RN-06: Verificar que está activo
        if (Boolean.FALSE.equals(cupon.getActivo())) {
            return ResponseEntity.ok(new ValidarCuponResponse(false, BigDecimal.ZERO,
                    "El cupón \"" + codigo + "\" no está activo"));
        }

        // RN-06: Verificar fecha de vigencia
        LocalDateTime ahora = LocalDateTime.now();
        if (cupon.getFechaInicio() != null && ahora.isBefore(cupon.getFechaInicio())) {
            return ResponseEntity.ok(new ValidarCuponResponse(false, BigDecimal.ZERO,
                    "El cupón \"" + codigo + "\" aún no ha comenzado a ser válido"));
        }
        if (cupon.getFechaFin() != null && ahora.isAfter(cupon.getFechaFin())) {
            return ResponseEntity.ok(new ValidarCuponResponse(false, BigDecimal.ZERO,
                    "El cupón \"" + codigo + "\" ha expirado"));
        }

        // RN-06: Verificar usos máximos
        if (cupon.getUsosMaximos() != null && cupon.getUsosActuales() >= cupon.getUsosMaximos()) {
            return ResponseEntity.ok(new ValidarCuponResponse(false, BigDecimal.ZERO,
                    "El cupón \"" + codigo + "\" ha alcanzado su límite de usos"));
        }

        // RN-06: Verificar importe mínimo
        if (cupon.getImporteMinimo() != null && subtotal.compareTo(cupon.getImporteMinimo()) < 0) {
            return ResponseEntity.ok(new ValidarCuponResponse(false, BigDecimal.ZERO,
                    "El subtotal no alcanza el importe mínimo para el cupón \"" + codigo +
                            "\" (mínimo: " + cupon.getImporteMinimo() + ")"));
        }

        // Si pasa todas las validaciones, es válido
        return ResponseEntity.ok(new ValidarCuponResponse(true, cupon.getValor(),
                "Cupón válido. Descuento: " + cupon.getValor()));
    }
}
