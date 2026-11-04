package com.safareyes.cafe.controllers;

import com.safareyes.cafe.Datos.InformeVentasDTO;
import com.safareyes.cafe.Datos.ProductoTopDTO;
import com.safareyes.cafe.repository.pedidoRepository;
import com.safareyes.cafe.repository.productoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/informes")
public class InformeController {

    @Autowired
    private pedidoRepository pedidoRepository;

    @Autowired
    private productoRepository productoRepository;

    // 15. GET /api/v1/informes/ventas-dia?fecha= - Nº pedidos, total, base e IVA del día - ADMIN
    @GetMapping("/ventas-dia")
    public ResponseEntity<InformeVentasDTO> ventasDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(23, 59, 59);

        Object[] resultado = pedidoRepository.contarInformeVentas(inicio, fin);
        if (resultado == null || resultado.length < 4) {
            return ResponseEntity.ok(new InformeVentasDTO(0L,
                    java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO));
        }

        long numeroPedidos = ((Number) resultado[0]).longValue();
        java.math.BigDecimal total = (java.math.BigDecimal) resultado[1];
        java.math.BigDecimal baseImponible = (java.math.BigDecimal) resultado[2];
        java.math.BigDecimal iva = (java.math.BigDecimal) resultado[3];

        return ResponseEntity.ok(new InformeVentasDTO(numeroPedidos, total, baseImponible, iva));
    }

    // 16. GET /api/v1/informes/productos-top?desde=&hasta=&limite= - Ranking de productos más vendidos - ADMIN
    @GetMapping("/productos-top")
    public ResponseEntity<List<ProductoTopDTO>> productosTop(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "10") Integer limite) {

        LocalDateTime desdeDateTime = desde.atStartOfDay();
        LocalDateTime hastaDateTime = hasta.atTime(23, 59, 59);

        List<Object[]> resultados = productoRepository.findProductosTop(desdeDateTime, hastaDateTime);

        List<ProductoTopDTO> top = resultados.stream()
                .limit(limite)
                .map(row -> new ProductoTopDTO(
                        ((Number) row[0]).intValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(top);
    }
}
