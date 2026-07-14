package com.smartsoft.cuenta.service;

import com.smartsoft.cuenta.dto.CuentaResultado;

/**
 * Capa de servicio (contrato). El endpoint gRPC depende de esta interfaz,
 * nunca de la implementacion concreta.
 */
public interface CuentaService {

    CuentaResultado crearCuenta(String numeroIdentificacion, String nombreCompleto);
}
