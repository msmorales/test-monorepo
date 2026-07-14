package com.smartsoft.persona.dto;

/**
 * DTO de entrada del servicio REST.
 * Representa el JSON recibido con los datos de la persona.
 * La validacion de las reglas de negocio se realiza en PersonaServiceImpl.
 */
public class PersonaDTO {

    private String nombre;

    private String apellido;

    private String numeroIdentificacion;

    public PersonaDTO() {
    }

    public PersonaDTO(String nombre, String apellido, String numeroIdentificacion) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }
}
