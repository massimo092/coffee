package com.safareyes.cafe.controllers;

import com.safareyes.cafe.Datos.CuponCreateDto;
import com.safareyes.cafe.Datos.CuponValidarDTO;
import com.safareyes.cafe.response.ValidarCuponResponse;
import com.safareyes.cafe.service.CuponService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cupones")
public class CuponController {

    @Autowired
    private CuponService cuponService;

    @PostMapping
    public ResponseEntity<CuponCreateDto> crearCupon(@Valid @RequestBody CuponCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cuponService.crearCupon(dto));
    }
    @PostMapping("/validar")
    public ValidarCuponResponse validar(@Valid @RequestBody CuponValidarDTO req) {
        return cuponService.validar(req, req.getEmailCliente());
    }

}
