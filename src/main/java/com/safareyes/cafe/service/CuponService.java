package com.safareyes.cafe.service;

import com.safareyes.cafe.Datos.CrearCuponDTO;
import com.safareyes.cafe.modelo.Cupon;
import com.safareyes.cafe.repository.ProductoRepository;
import com.safareyes.cafe.repository.cuponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CuponService {
    private final cuponRepository cuponRepository;

    @Transactional
    public CrearCuponDTO crearCupon(CrearCuponDTO dto){
        Cupon cupon = new Cupon();
        cupon.setCodigo(dto.getCodigo());
        cupon.setTipo(dto.getTipo());
        cupon.setValor(dto.getValor());
        cupon.setFechaInicio(dto.getFechaInicio());
        cupon.setFechaFin(dto.getFechaFin());
        cupon.setUsosMaximos(dto.getUsosMaximos());
        cupon.setUsosActuales(dto.getUsosActuales());
        cupon.setImporteMinimo(dto.getImporteMinimo());
        cupon.setEmailCliente(dto.getEmailCliente());
        cupon.setActivo(dto.getActivo());

        return null;
    }
}
