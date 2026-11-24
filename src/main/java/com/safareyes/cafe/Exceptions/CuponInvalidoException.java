package com.safareyes.cafe.Exceptions;

public class CuponInvalidoException extends RuntimeException {
    public CuponInvalidoException(String mensaje) {
        super(mensaje);
    }
}