package com.smartsoft.persona.dto;

/**
 * DTO de salida del servicio REST.
 * "exito" indica si la validacion (persona + cuenta) paso.
 * "mensaje" es "Success" cuando es valido, o el detalle del error cuando no.
 * "numeroCuenta" viene de cuenta-service (via gRPC) y solo se llena cuando exito=true.
 */
public class PersonaResponseDTO {

    private boolean exito;
    private String mensaje;
    private String nombreCompleto;
    private String numeroIdentificacion;
    private String numeroCuenta;

    public PersonaResponseDTO() {
    }

    public PersonaResponseDTO(boolean exito, String mensaje, String nombreCompleto,
                               String numeroIdentificacion, String numeroCuenta) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.nombreCompleto = nombreCompleto;
        this.numeroIdentificacion = numeroIdentificacion;
        this.numeroCuenta = numeroCuenta;
    }

    public boolean isExito() {
        return exito;
    }

    public void setExito(boolean exito) {
        this.exito = exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }
}
