package com.smartsoft.cuenta.service.impl;

import com.smartsoft.cuenta.dto.CuentaResultado;
import com.smartsoft.cuenta.service.CuentaService;

import jakarta.enterprise.context.ApplicationScoped;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion de CuentaService.
 *
 * Reglas de validacion (independientes de las que ya aplico persona-service,
 * cada microservicio valida su propia entrada):
 * - nombreCompleto no puede estar vacio.
 * - numeroIdentificacion debe contener solo digitos y tener entre 6 y 10 caracteres.
 *
 * Si todo es valido, genera un numero de cuenta de 16 digitos.
 */
@ApplicationScoped
public class CuentaServiceImpl implements CuentaService {

    private static final String REGEX_NUMERO_IDENTIFICACION = "\\d{6,10}";
    private static final int LONGITUD_NUMERO_CUENTA = 16;

    private final SecureRandom random = new SecureRandom();

    @Override
    public CuentaResultado crearCuenta(String numeroIdentificacion, String nombreCompleto) {
        List<String> errores = validar(numeroIdentificacion, nombreCompleto);

        if (!errores.isEmpty()) {
            return new CuentaResultado(false, String.join("; ", errores), null);
        }

        String numeroCuenta = generarNumeroCuenta();
        return new CuentaResultado(true, "Success", numeroCuenta);
    }

    private List<String> validar(String numeroIdentificacion, String nombreCompleto) {
        List<String> errores = new ArrayList<>();

        if (esVacio(nombreCompleto)) {
            errores.add("El nombre completo no puede estar vacio");
        }

        if (esVacio(numeroIdentificacion)) {
            errores.add("El numero de identificacion no puede estar vacio");
        } else if (!numeroIdentificacion.matches(REGEX_NUMERO_IDENTIFICACION)) {
            errores.add("El numero de identificacion debe contener solo digitos y tener entre 6 y 10 caracteres");
        }

        return errores;
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private String generarNumeroCuenta() {
        StringBuilder sb = new StringBuilder(LONGITUD_NUMERO_CUENTA);
        // Primer digito entre 1 y 9 para evitar ceros a la izquierda.
        sb.append(1 + random.nextInt(9));
        for (int i = 1; i < LONGITUD_NUMERO_CUENTA; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
