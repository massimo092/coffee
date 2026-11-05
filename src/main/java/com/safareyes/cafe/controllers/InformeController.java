package com.safareyes.cafe.controllers;

import com.safareyes.cafe.Datos.InformeVentasDTO;
import com.safareyes.cafe.Datos.ProductoTopDTO;
import com.safareyes.cafe.service.InformeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/informes")
@RequiredArgsConstructor
public class InformeController {

    private final InformeService informeService;

    @GetMapping("/ventas-dia")
    public InformeVentasDTO ventasDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return informeService.ventasDia(fecha);
    }

    @GetMapping("/productos-top")
    public List<ProductoTopDTO> productosTop(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "10") Integer limite) {
        return informeService.productosTop(desde, hasta, limite);
    }
}