package com.safareyes.cafe.controllers;

import com.safareyes.cafe.Datos.CambiarEstadoRequest;
import com.safareyes.cafe.Datos.CrearPedidoRequest;
import com.safareyes.cafe.Datos.InformeVentasDTO;
import com.safareyes.cafe.Datos.lineaPedidoDTO;
import com.safareyes.cafe.modelo.Pedido;
import com.safareyes.cafe.repository.pedidoRepository;
import com.safareyes.cafe.service.PedidoService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    @Autowired
    private pedidoRepository pedidoRepository;

    @Autowired
    private PedidoService pedidoService;

    // 10.- Detalle con líneas - ADMIN, BARISTA, CLIENTE (propio)
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDetallePedido(@PathVariable Integer id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pedido no encontrado: ID " + id));

        List<lineaPedidoDTO> lineas = pedidoRepository.pedidoConLineas(id);

        return ResponseEntity.ok(Map.of(
                "pedido", pedido,
                "lineas", lineas
        ));
    }

    // 11. - Listado paginado por estado y fecha, un cliente solo ve los suyos
    @GetMapping
    public Page<Pedido> listarPedidos(
            @RequestParam(required = false) String estado,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin,

            Pageable pageable) {

        return pedidoRepository.listarPedidos(
                estado,
                fechaInicio != null ? fechaInicio.atStartOfDay() : null,
                fechaFin != null ? fechaFin.atTime(23, 59, 59) : null,
                pageable
        );
    }

    //creacion del pedido
    @PostMapping
    public ResponseEntity<Pedido> crearPedido(@Valid @RequestBody CrearPedidoRequest request) {
        Pedido pedido = pedidoService.crearPedido(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    //cambiar estado de el pedido
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Pedido> cambiarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody CambiarEstadoRequest request) {
        Pedido pedido = pedidoService.cambiarEstado(id, request);
        return ResponseEntity.ok(pedido);
    }

    //informe ventas
    @GetMapping("/informes/ventas")
    public ResponseEntity<InformeVentasDTO> obtenerInformeVentas(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin) {

        InformeVentasDTO informe = pedidoService.obtenerInformeVentas(fechaInicio, fechaFin);
        return ResponseEntity.ok(informe);
    }
}