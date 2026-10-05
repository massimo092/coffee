package com.safareyes.cafe;

import com.safareyes.cafe.Datos.buscarPorCategoria;
import com.safareyes.cafe.repository.productoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class CafeApplicationTests {

    @Autowired
    private productoRepository productoRepository;

    @Test
    void contextLoads() {

        List<buscarPorCategoria> endpont1 = productoRepository.BuscarNumeroPorCategoria();
        System.out.println(endpont1);


    }

}
