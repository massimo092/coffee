package com.safareyes.cafe.service;

import com.safareyes.cafe.Datos.InformeVentasDTO;
import com.safareyes.cafe.Datos.ProductoTopDTO;
import com.safareyes.cafe.repository.pedidoRepository;
import com.safareyes.cafe.repository.productoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InformeService {

    private final pedidoRepository pedidoRepository;
    private final productoRepository productoRepository;

    // Ventas del día
    public InformeVentasDTO ventasDia(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(23, 59, 59);

        Object[] resultado = pedidoRepository.contarInformeVentas(inicio, fin);
        if (resultado == null || resultado.length < 4) {
            return new InformeVentasDTO(0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        long numeroPedidos = ((Number) resultado[0]).longValue();
        BigDecimal total = (BigDecimal) resultado[1];
        BigDecimal baseImponible = (BigDecimal) resultado[2];
        BigDecimal iva = (BigDecimal) resultado[3];

        return new InformeVentasDTO(numeroPedidos, total, baseImponible, iva);
    }

    // Ranking de productos más vendidos
    public List<ProductoTopDTO> productosTop(LocalDate desde, LocalDate hasta, Integer limite) {
        LocalDateTime desdeDateTime = desde.atStartOfDay();
        LocalDateTime hastaDateTime = hasta.atTime(23, 59, 59);

        List<Object[]> resultados = productoRepository.findProductosTop(desdeDateTime, hastaDateTime);

        return resultados.stream()
                .limit(limite)
                .map(row -> new ProductoTopDTO(
                        ((Number) row[0]).intValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue()))
                .toList();
    }
}