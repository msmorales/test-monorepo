package com.smartsoft.cuenta.service;

import com.smartsoft.cuenta.dto.CuentaResultado;
import com.smartsoft.cuenta.service.impl.CuentaServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba unitaria pura (sin Quarkus/gRPC) de las reglas de negocio.
 */
class CuentaServiceImplTest {

    private final CuentaServiceImpl cuentaService = new CuentaServiceImpl();

    @Test
    void datosValidosGeneraNumeroCuentaDe16Digitos() {
        CuentaResultado resultado = cuentaService.crearCuenta("123456789", "Juan Perez");

        assertTrue(resultado.isExito());
        assertEquals("Success", resultado.getMensaje());
        assertEquals(16, resultado.getNumeroCuenta().length());
        assertTrue(resultado.getNumeroCuenta().matches("\\d{16}"));
    }

    @Test
    void nombreCompletoVacioDevuelveError() {
        CuentaResultado resultado = cuentaService.crearCuenta("123456789", "");

        assertFalse(resultado.isExito());
        assertEquals("El nombre completo no puede estar vacio", resultado.getMensaje());
    }

    @Test
    void numeroIdentificacionCortoDevuelveError() {
        CuentaResultado resultado = cuentaService.crearCuenta("123", "Juan Perez");

        assertFalse(resultado.isExito());
        assertEquals("El numero de identificacion debe contener solo digitos y tener entre 6 y 10 caracteres",
                resultado.getMensaje());
    }

    @Test
    void numeroIdentificacionNoNumericoDevuelveError() {
        CuentaResultado resultado = cuentaService.crearCuenta("abc123456", "Juan Perez");

        assertFalse(resultado.isExito());
    }
}
