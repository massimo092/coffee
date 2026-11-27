package com.safareyes.cafe.service;

import com.safareyes.cafe.Datos.InformeVentasDTO;
import com.safareyes.cafe.Datos.ProductoTopDTO;
import com.safareyes.cafe.repository.ProductoRepository;
import com.safareyes.cafe.repository.pedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InformeService {

    private final pedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;


    // Ventas del día
    public InformeVentasDTO ventasDia(LocalDate fecha) {

        LocalDate inicio = fecha;
        LocalDate fin = fecha.plusDays(1);

        List<Object[]> resultados = pedidoRepository.contarInformeVentas(inicio, fin);

        Object[] resultado = resultados.get(0);

        long numeroPedidos = ((Number) resultado[0]).longValue();
        BigDecimal total = new BigDecimal(resultado[1].toString());
        BigDecimal baseImponible = new BigDecimal(resultado[2].toString());
        BigDecimal iva = new BigDecimal(resultado[3].toString());

        return new InformeVentasDTO(numeroPedidos, total, baseImponible, iva);
    }


    // Ranking de productos más vendidos
    public List<ProductoTopDTO> productosTop(LocalDate desde, LocalDate hasta, Integer limite) {
        LocalDate hastaFin = hasta.plusDays(1);

        List<Object[]> resultados = productoRepository.findProductosTop(desde, hastaFin);

        return resultados.stream()
                .limit(limite)
                .map(row -> new ProductoTopDTO(
                        ((Number) row[0]).intValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue()))
                .toList();
    }
}