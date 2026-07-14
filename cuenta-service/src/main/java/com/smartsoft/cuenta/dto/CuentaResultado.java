package com.smartsoft.cuenta.dto;

/**
 * Resultado interno (no expuesto directamente por gRPC) que produce
 * CuentaServiceImpl y que CuentaGrpcServiceImpl traduce a CuentaResponse.
 */
public class CuentaResultado {

    private final boolean exito;
    private final String mensaje;
    private final String numeroCuenta;

    public CuentaResultado(boolean exito, String mensaje, String numeroCuenta) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.numeroCuenta = numeroCuenta;
    }

    public boolean isExito() {
        return exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }
}
