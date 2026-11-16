package com.safareyes.cafe;

import com.safareyes.cafe.Datos.ContadorProductos;
import com.safareyes.cafe.Datos.InformeVentasDTO;
import com.safareyes.cafe.Datos.ProductoCreateDto;
import com.safareyes.cafe.Datos.ProductoTopDTO;
import com.safareyes.cafe.Datos.ProducoConAlergenos;
import com.safareyes.cafe.Datos.buscarPorCategoria;
import com.safareyes.cafe.Datos.lineaPedidoDTO;
import com.safareyes.cafe.modelo.Pedido;
import com.safareyes.cafe.modelo.Producto;
import com.safareyes.cafe.repository.categoriaRepository;
import com.safareyes.cafe.repository.pedidoRepository;
import com.safareyes.cafe.repository.ProductoRepository;
import com.safareyes.cafe.service.InformeService;
import com.safareyes.cafe.service.PedidoService;
import com.safareyes.cafe.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
class CafeApplicationTests {

    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private categoriaRepository categoriaRepository;
    @Autowired
    private pedidoRepository pedidoRepository;
    @Autowired
    private ProductoService productoService;
    @Autowired
    private PedidoService pedidoService;
    @Autowired
    private InformeService informeService;


    @Test
    void contextLoads() {

        // GET 1 - Carta paginada con filtros opcionales
        Page<ProductoCreateDto> endpoint1 = productoService.listarCarta(null, "", PageRequest.of(0, 10));
        System.out.println(endpoint1);

        // GET 2 - Detalle con alérgenos
        ProductoCreateDto endpoint2 = productoService.detalle(1);
        System.out.println(endpoint2);

        // GET 3 - Categorías con número de productos activos
        List<ContadorProductos> endpoint3 = categoriaRepository.contarProductosActivos();
        System.out.println(endpoint3);

        // GET 4 - Detalle pedido con líneas
        List<lineaPedidoDTO> endpoint4 = pedidoRepository.pedidoConLineas(1);
        System.out.println(endpoint4);

        // GET 5 - Listado paginado de pedidos
        Page<Pedido> endpoint5 = pedidoRepository.listarPedidos(null, null, null, PageRequest.of(0, 10));
        System.out.println(endpoint5);

        // GET 6 - Ventas del día
        InformeVentasDTO endpoint6 = informeService.ventasDia(LocalDate.now());
        System.out.println(endpoint6);

        // GET 7 - Ranking de productos más vendidos
        List<ProductoTopDTO> endpoint7 = informeService.productosTop(LocalDate.now().minusDays(30), LocalDate.now(), 10);
        System.out.println(endpoint7);

        // Productos por categoría
        List<Producto> endpoint8 = productoRepository.findByCategoriaIdAndActivoTrue(1);
        System.out.println(endpoint8);

        // Buscar productos por nombre
        Page<Producto> endpoint9 = productoRepository.findByActivoTrueAndNombreContainingIgnoreCase("", PageRequest.of(0, 10));
        System.out.println(endpoint9);

        // Productos con alérgenos
        List<ProducoConAlergenos> endpoint10 = productoRepository.BuscarProductosConAlergenos();
        System.out.println(endpoint10);

        // Número de productos por categoría
        List<buscarPorCategoria> endpoint11 = productoRepository.BuscarNumeroPorCategoria();
        System.out.println(endpoint11);
    }

}
