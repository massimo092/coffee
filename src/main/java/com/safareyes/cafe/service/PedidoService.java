package com.safareyes.cafe.service;

import com.safareyes.cafe.Datos.CambiarEstadoRequest;
import com.safareyes.cafe.Datos.CrearPedidoRequest;
import com.safareyes.cafe.Datos.InformeVentasDTO;
import com.safareyes.cafe.Datos.LineaPedidoRequest;
import com.safareyes.cafe.modelo.*;
import com.safareyes.cafe.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private static final int MIN_LINEAS = 1;
    private static final int MAX_LINEAS = 15;
    private static final int MIN_CANTIDAD = 1;
    private static final int MAX_CANTIDAD = 20;

    @Autowired
    private pedidoRepository pedidoRepository;

    @Autowired
    private productoRepository productoRepository;

    @Autowired
    private cuponRepository cuponRepository;

    @Autowired
    private usuarioRepository usuarioRepository;

    @Autowired
    private lineaPedidoRepository lineaPedidoRepository;

    // Transiciones válidas de estado (RN-08)
    private static final Map<String, List<String>> TRANSICIONES_VALIDAS = Map.of(
            "PENDIENTE", List.of("EN_PREPARACION", "CANCELADO"),
            "EN_PREPARACION", List.of("LISTO"),
            "LISTO", List.of("ENTREGADO"),
            "ENTREGADO", List.of(),
            "CANCELADO", List.of()
    );

    /**
     * Crea un pedido aplicando todas las reglas de negocio (RN-01 a RN-09).
     */
    @Transactional
    public Pedido crearPedido(CrearPedidoRequest request) {
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
            if (Boolean.FALSE.equals(producto.getActivo())) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "El producto \"" + producto.getNombre() + "\" está dado de baja y no se puede pedir");
            }

            // RN-02: Validar que el producto esté disponible
            if (Boolean.FALSE.equals(producto.getDisponible())) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "El producto \"" + producto.getNombre() + "\" está agotado");
            }

            // RN-04: Validar cantidad después de agrupar
            if (cantidad < MIN_CANTIDAD || cantidad > MAX_CANTIDAD) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "La cantidad del producto \"" + producto.getNombre() + "\" (" + cantidad +
                        ") supera el límite de " + MAX_CANTIDAD + " unidades por línea");
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
        Pedido pedido = Pedido.builder()
                .fechaHora(LocalDateTime.now())
                .estado("PENDIENTE")
                .build();

        // RN-09: Calcular número de turno
        LocalDate hoy = LocalDate.now();
        long pedidosHoy = pedidoRepository.countByFechaHoraBetween(
                hoy.atStartOfDay(), hoy.plusDays(1).atStartOfDay());
        pedido.setNumeroTurno((int) pedidosHoy + 1);

        // Asignar usuario si se proporciona dniSocio
        if (request.getDniSocio() != null && !request.getDniSocio().isEmpty()) {
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
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setSubtotal(subtotal);

        // Validar y aplicar cupón si se proporciona (RN-06)
        Cupon cuponAplicado = null;
        if (request.getCodigoCupon() != null && !request.getCodigoCupon().isEmpty()) {
            cuponAplicado = validarYAplicarCupon(request.getCodigoCupon(), subtotal, pedido);
        }

        // Calcular descuento (RN-07)
        BigDecimal descuento = calcularDescuento(pedido, cuponAplicado, subtotal);
        pedido.setDescuento(descuento);

        // RN-05: Calcular totales
        BigDecimal total = subtotal.subtract(descuento);

        // RN-07: El total nunca puede ser negativo
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }
        pedido.setTotal(total);

        // RN-05: Base imponible e IVA
        BigDecimal baseImponible = total.divide(IVA_RATE, 2, RoundingMode.HALF_UP);
        BigDecimal iva = total.subtract(baseImponible);
        pedido.setBaseImponible(baseImponible);
        pedido.setIva(iva);

        return pedidoRepository.save(pedido);
    }

    /**
     * Valida un cupón según RN-06.
     */
    private Cupon validarYAplicarCupon(String codigo, BigDecimal subtotal, Pedido pedido) {
        Cupon cupon = cuponRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Cupón no encontrado: " + codigo));

        // RN-06: Verificar que está activo
        if (Boolean.FALSE.equals(cupon.getActivo())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "El cupón \"" + codigo + "\" no está activo");
        }

        // RN-06: Verificar fecha de vigencia
        LocalDateTime ahora = LocalDateTime.now();
        if (cupon.getFechaInicio() != null && ahora.isBefore(cupon.getFechaInicio())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "El cupón \"" + codigo + "\" aún no ha comenzado a ser válido");
        }
        if (cupon.getFechaFin() != null && ahora.isAfter(cupon.getFechaFin())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "El cupón \"" + codigo + "\" ha expirado");
        }

        // RN-06: Verificar usos máximos
        if (cupon.getUsosMaximos() != null && cupon.getUsosActuales() >= cupon.getUsosMaximos()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "El cupón \"" + codigo + "\" ha alcanzado su límite de usos");
        }

        // RN-06: Verificar importe mínimo
        if (cupon.getImporteMinimo() != null && subtotal.compareTo(cupon.getImporteMinimo()) < 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "El subtotal no alcanza el importe mínimo para el cupón \"" + codigo +
                    "\" (mínimo: " + cupon.getImporteMinimo() + ")");
        }

        // RN-06: Si es personal, verificar email del cliente
        if ("PERSONAL".equalsIgnoreCase(cupon.getTipo()) && cupon.getEmailCliente() != null) {
            if (pedido.getUsuarioId() == null) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "El cupón \"" + codigo + "\" es personal y requiere un usuario registrado");
            }
            String emailUsuario = pedido.getUsuarioId().getEmail();
            if (emailUsuario == null || !emailUsuario.equalsIgnoreCase(cupon.getEmailCliente())) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "El cupón \"" + codigo + "\" es personal y no coincide con el email del cliente");
            }
        }

        // Incrementar usos actuales
        cupon.setUsosActuales(cupon.getUsosActuales() + 1);
        cuponRepository.save(cupon);

        pedido.setCuponId(cupon);
        return cupon;
    }

    /**
     * Calcula el descuento según RN-07.
     */
    private BigDecimal calcularDescuento(Pedido pedido, Cupon cupon, BigDecimal subtotal) {
        BigDecimal descuentoSocio = BigDecimal.ZERO;
        BigDecimal descuentoCupon = BigDecimal.ZERO;

        // RN-07: Descuento de socio FitZone (10%)
        if (pedido.getUsuarioId() != null && "SOCIO_FITZONE".equalsIgnoreCase(pedido.getUsuarioId().getRol())) {
            descuentoSocio = subtotal.multiply(SOCIO_DESCUENTO);
        }

        // RN-07: Descuento del cupón
        if (cupon != null) {
            descuentoCupon = cupon.getValor();
        }

        // RN-07: No se acumulan, se aplica el mayor
        BigDecimal descuento = BigDecimal.ZERO;
        if (descuentoSocio.compareTo(descuentoCupon) >= 0) {
            descuento = descuentoSocio;
        } else {
            descuento = descuentoCupon;
        }

        return descuento;
    }

    /**
     * Cambia el estado de un pedido según RN-08.
     */
    @Transactional
    public Pedido cambiarEstado(Integer pedidoId, CambiarEstadoRequest request) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pedido no encontrado: ID " + pedidoId));

        String estadoActual = pedido.getEstado();
        String nuevoEstado = request.getEstado();

        // RN-08: Validar transición
        List<String> estadosPermitidos = TRANSICIONES_VALIDAS.getOrDefault(estadoActual, List.of());
        if (!estadosPermitidos.contains(nuevoEstado)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "No se puede cambiar el estado de \"" + estadoActual + "\" a \"" + nuevoEstado +
                    "\". Transiciones válidas desde \"" + estadoActual + "\": " +
                    (estadosPermitidos.isEmpty() ? "ninguna" : estadosPermitidos));
        }

        // RN-08: Si se cancela, devolver el uso del cupón
        if ("CANCELADO".equals(nuevoEstado) && pedido.getCuponId() != null) {
            Cupon cupon = pedido.getCuponId();
            cupon.setUsosActuales(Math.max(0, cupon.getUsosActuales() - 1));
            cuponRepository.save(cupon);
        }

        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    /**
     * Genera el informe de ventas según RN-10.
     */
    public InformeVentasDTO obtenerInformeVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);

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
}
