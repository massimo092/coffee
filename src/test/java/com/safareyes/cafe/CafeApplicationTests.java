package com.safareyes.cafe;

import com.safareyes.cafe.Datos.ContadorProductos;
import com.safareyes.cafe.Datos.ProducoConAlergenos;
import com.safareyes.cafe.Datos.buscarPorCategoria;
import com.safareyes.cafe.Datos.lineaPedidoDTO;
import com.safareyes.cafe.repository.categoriaRepository;
import com.safareyes.cafe.repository.pedidoRepository;
import com.safareyes.cafe.repository.productoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class CafeApplicationTests {

    @Autowired
    private productoRepository productoRepository;
    @Autowired
    private categoriaRepository categoriaRepository;
    @Autowired
    private pedidoRepository pedidoRepository;


    @Test
    void contextLoads() {

        List<buscarPorCategoria> endpoint1 = productoRepository.BuscarNumeroPorCategoria();
        System.out.println(endpoint1);

        List<ProducoConAlergenos> endpoint2 = productoRepository.BuscarProductosConAlergenos();
        System.out.println(endpoint2);

        List<ContadorProductos>endpoint7 = categoriaRepository.contarProductosActivos();
        System.out.println(endpoint7);


        List<lineaPedidoDTO> endpoint10 = pedidoRepository.pedidoConLineas(1);
        System.out.println(endpoint10);
    }

}
