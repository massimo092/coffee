package com.safareyes.cafe.service;

import com.safareyes.cafe.Datos.CambiarEstadoRequest;
import com.safareyes.cafe.Datos.CrearPedidoRequest;
import com.safareyes.cafe.Datos.InformeVentasDTO;
import com.safareyes.cafe.Datos.LineaPedidoRequest;
import com.safareyes.cafe.Exceptions.CuponInvalidoException;
import com.safareyes.cafe.modelo.*;
import com.safareyes.cafe.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private static final BigDecimal IVA_RATE = new BigDecimal("1.10");
    private static final BigDecimal SOCIO_DESCUENTO = new BigDecimal("0.10");
    private static final int TURNO_LOCK_NAMESPACE = 6_842_391;
    private static final int MIN_LINEAS = 1;
    private static final int MAX_LINEAS = 15;
    private static final int MIN_CANTIDAD = 1;
    private static final int MAX_CANTIDAD = 20;

    @Autowired
    private pedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private cuponRepository cuponRepository;

    @Autowired
    private CuponService cuponService;

    @Autowired
    private usuarioRepository usuarioRepository;

    @Autowired
    private lineaPedidoRepository lineaPedidoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Transiciones válidas de estado (RN-08)
    private static final Map<String, List<String>> TRANSICIONES_VALIDAS = Map.of(
            "pendiente", List.of("en_preparacion", "cancelado"),
            "en_preparacion", List.of("listo"),
            "listo", List.of("entregado"),
            "entregado", List.of(),
            "cancelado", List.of()
    );


    @Transactional
    public Pedido crearPedido(CrearPedidoRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Los datos del pedido son obligatorios");
        }

        // RN-04: Validar número de líneas
        List<LineaPedidoRequest> lineas = request.getLineas();
        if (lineas == null || lineas.size() < MIN_LINEAS) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "El pedido debe tener al menos " + MIN_LINEAS + " línea");
        }
        if (lineas.size() > MAX_LINEAS) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "El pedido puede tener como máximo " + MAX_LINEAS + " líneas");
        }

        // RN-04: Agrupar líneas del mismo producto
        Map<Integer, Integer> productosAgrupados = new LinkedHashMap<>();
        for (LineaPedidoRequest linea : lineas) {
            if (linea == null || linea.getProductoId() == null || linea.getCantidad() == null) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Cada línea debe incluir producto y cantidad");
            }
            if (linea.getCantidad() < MIN_CANTIDAD || linea.getCantidad() > MAX_CANTIDAD) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "La cantidad por línea debe estar entre " + MIN_CANTIDAD + " y " + MAX_CANTIDAD);
            }
            productosAgrupados.merge(linea.getProductoId(), linea.getCantidad(), Integer::sum);
        }

        // RN-02 y RN-04: Validar cada producto y cantidad agrupada
        List<LineaPedido> lineasPedido = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : productosAgrupados.entrySet()) {
            Integer productoId = entry.getKey();
            Integer cantidad = entry.getValue();

            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Producto no encontrado: ID " + productoId));

            // RN-01 y RN-02: Validar que el producto esté activo
            if (!Boolean.TRUE.equals(producto.getActivo())) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "El producto \"" + producto.getNombre() + "\" está dado de baja y no se puede pedir");
            }

            // RN-02: Validar que el producto esté disponible
            if (!Boolean.TRUE.equals(producto.getDisponible())) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "El producto \"" + producto.getNombre() + "\" está agotado");
            }

            // RN-04: Validar cantidad después de agrupar
            if (cantidad < MIN_CANTIDAD || cantidad > MAX_CANTIDAD) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "La cantidad del producto \"" + producto.getNombre() + "\" (" + cantidad +
                        ") supera el límite de " + MAX_CANTIDAD + " unidades por línea");
            }

            if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "El producto \"" + producto.getNombre() + "\" tiene un precio inválido");
            }

            // RN-03: Guardar el precio unitario vigente
            LineaPedido lineaPedido = LineaPedido.builder()
                    .producto(producto)
                    .cantidad(cantidad)
                    .precio_unitario(producto.getPrecio())
                    .build();
            lineasPedido.add(lineaPedido);
        }

        // Si tras agrupar hay más de MAX_LINEAS, rechazar
        if (lineasPedido.size() > MAX_LINEAS) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "El pedido agrupado tiene " + lineasPedido.size() + " líneas, el máximo es " + MAX_LINEAS);
        }

        // Crear el pedido
        LocalDateTime fechaHoraCreacion = LocalDateTime.now();
        Pedido pedido = Pedido.builder()
                .fechaHora(fechaHoraCreacion)
                .estado("pendiente")
                .build();

        // RN-09: Serializar el cálculo del turno dentro de la transacción.
        LocalDate hoy = fechaHoraCreacion.toLocalDate();
        jdbcTemplate.execute("SELECT pg_advisory_xact_lock(" + TURNO_LOCK_NAMESPACE + ", " + hoy.toEpochDay() + ")");
        long pedidosHoy = pedidoRepository.contarPedidosEnIntervalo(
                hoy.atStartOfDay(), hoy.plusDays(1).atStartOfDay());
        pedido.setNumeroTurno((int) pedidosHoy + 1);

        // Asignar usuario si se proporciona dniSocio
        if (request.getDniSocio() != null && !request.getDniSocio().isBlank()) {
            Usuario usuario = usuarioRepository.findByEmail(request.getDniSocio())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Usuario no encontrado: " + request.getDniSocio()));
            pedido.setUsuarioId(usuario);
        }

        // Guardar pedido para obtener ID
        pedido = pedidoRepository.save(pedido);

        // Asignar pedido a cada línea
        for (LineaPedido linea : lineasPedido) {
            linea.setPedido(pedido);
        }
        lineaPedidoRepository.saveAll(lineasPedido);

        // Calcular subtotal (RN-05)
        BigDecimal subtotal = lineasPedido.stream()
                .map(l -> l.getPrecio_unitario().multiply(BigDecimal.valueOf(l.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        pedido.setSubtotal(subtotal);

        // Validar y aplicar cupón si se proporciona (RN-06)
        Cupon cuponValidado = null;
        if (request.getCodigoCupon() != null && !request.getCodigoCupon().isBlank()) {
            cuponValidado = validarYAplicarCupon(request.getCodigoCupon(), subtotal, pedido.getUsuarioId());
        }

        // RN-07: los descuentos no se acumulan; solo se consume el cupón si ofrece el mayor descuento.
        BigDecimal descuentoSocio = calcularDescuentoSocio(pedido, subtotal);
        BigDecimal descuentoCupon = cuponValidado != null
                ? cuponService.calcularDescuentoCupon(cuponValidado, subtotal)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        boolean usarCupon = cuponValidado != null && descuentoCupon.compareTo(descuentoSocio) > 0;
        BigDecimal descuento = (usarCupon ? descuentoCupon : descuentoSocio)
                .min(subtotal)
                .setScale(2, RoundingMode.HALF_UP);

        if (usarCupon) {
            pedido.setCuponId(cuponValidado);
            int usosActualizados = cuponRepository.incrementarUsoSiDisponible(cuponValidado.getId());
            if (usosActualizados == 0) {
                throw new CuponInvalidoException("El cupón ha agotado sus usos");
            }
        }
        pedido.setDescuento(descuento);

        // RN-05: Calcular totales
        BigDecimal total = subtotal.subtract(descuento).setScale(2, RoundingMode.HALF_UP);

        // RN-07: El total nunca puede ser negativo
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        pedido.setTotal(total);

        // RN-05: Base imponible e IVA
        BigDecimal baseImponible = total.divide(IVA_RATE, 2, RoundingMode.HALF_UP);
        BigDecimal iva = total.subtract(baseImponible).setScale(2, RoundingMode.HALF_UP);
        pedido.setBaseImponible(baseImponible);
        pedido.setIva(iva);

        return pedidoRepository.save(pedido);
    }

    /**
     * Valida un cupón según RN-06.
     */
    private Cupon validarYAplicarCupon(String codigo, BigDecimal subtotal, Usuario usuario) {
        String emailCliente = usuario != null ? usuario.getEmail() : null;
        return cuponService.validarCupon(codigo, subtotal, emailCliente);
    }

    /**
     * Calcula el descuento según RN-07.
     */
    private BigDecimal calcularDescuentoSocio(Pedido pedido, BigDecimal subtotal) {
        // RN-07: Descuento de socio FitZone (10%)
        if (pedido.getUsuarioId() != null && "SOCIO_FITZONE".equalsIgnoreCase(pedido.getUsuarioId().getRol())) {
            return subtotal.multiply(SOCIO_DESCUENTO).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Cambia el estado de un pedido según RN-08.
     */
    @Transactional
    public Pedido cambiarEstado(Integer pedidoId, CambiarEstadoRequest request) {
        Pedido pedido = pedidoRepository.findByIdForUpdate(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pedido no encontrado: ID " + pedidoId));

        String estadoActual = pedido.getEstado() == null
                ? ""
                : pedido.getEstado().trim().toLowerCase(Locale.ROOT);
        String nuevoEstado = request == null || request.getEstado() == null
                ? ""
                : request.getEstado().trim().toLowerCase(Locale.ROOT);

        // RN-08: Validar transición
        List<String> estadosPermitidos = TRANSICIONES_VALIDAS.getOrDefault(estadoActual, List.of());
        if (!estadosPermitidos.contains(nuevoEstado)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "No se puede cambiar el estado de \"" + estadoActual + "\" a \"" + nuevoEstado +
                    "\". Transiciones válidas desde \"" + estadoActual + "\": " +
                    (estadosPermitidos.isEmpty() ? "ninguna" : estadosPermitidos));
        }

        // RN-08: Si se cancela, devolver el uso del cupón
        if ("cancelado".equals(nuevoEstado) && pedido.getCuponId() != null) {
            Cupon cupon = pedido.getCuponId();
            cuponRepository.devolverUso(cupon.getId());
        }

        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    /**
     * Genera el informe de ventas según RN-10.
     */
    public InformeVentasDTO obtenerInformeVentas(LocalDate fechaInicio, LocalDate fechaFin) {

        LocalDate fin = fechaFin.plusDays(1);

        List<Object[]> resultados = pedidoRepository.contarInformeVentas(fechaInicio, fin);

        if (resultados.isEmpty()) {
            return new InformeVentasDTO(0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        Object[] resultado = resultados.get(0);

        long numeroPedidos = ((Number) resultado[0]).longValue();
        BigDecimal total = (BigDecimal) resultado[1];
        BigDecimal baseImponible = (BigDecimal) resultado[2];
        BigDecimal iva = (BigDecimal) resultado[3];

        return new InformeVentasDTO(numeroPedidos, total, baseImponible, iva);
    }
}
