package com.smartsoft.persona.service.impl;

import com.smartsoft.cuenta.grpc.CuentaGrpcServiceGrpc;
import com.smartsoft.cuenta.grpc.CuentaResponse;
import com.smartsoft.persona.dto.PersonaDTO;
import com.smartsoft.persona.dto.PersonaResponseDTO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Prueba unitaria pura (sin Quarkus, sin red real) de PersonaServiceImpl,
 * simulando la respuesta de cuenta-service con Mockito.
 *
 * Vive en el mismo paquete que PersonaServiceImpl (com.smartsoft.persona.service.impl)
 * porque el campo cuentaClient es package-private, siguiendo el mismo estilo
 * de inyeccion que @Inject en el resto del proyecto.
 */
@ExtendWith(MockitoExtension.class)
class PersonaServiceImplTest {

    @Mock
    CuentaGrpcServiceGrpc.CuentaGrpcServiceBlockingStub cuentaClient;

    private PersonaServiceImpl personaService;

    private PersonaServiceImpl construirServicio() {
        PersonaServiceImpl impl = new PersonaServiceImpl();
        impl.cuentaClient = cuentaClient;
        return impl;
    }

    @Test
    void datosValidosLlamaGrpcYRetornaSuccess() {
        when(cuentaClient.crearCuenta(any())).thenReturn(
                CuentaResponse.newBuilder()
                        .setExito(true)
                        .setMensaje("Success")
                        .setNumeroCuenta("1234567890123456")
                        .build());

        personaService = construirServicio();
        PersonaDTO dto = new PersonaDTO("Juan", "Perez", "123456789");
        PersonaResponseDTO respuesta = personaService.registrarPersona(dto);

        assertTrue(respuesta.isExito());
        assertEquals("Success", respuesta.getMensaje());
        assertEquals("Juan Perez", respuesta.getNombreCompleto());
        assertEquals("1234567890123456", respuesta.getNumeroCuenta());
    }

    @Test
    void nombreVacioNoLlamaGrpcYDevuelveError() {
        personaService = construirServicio();
        PersonaDTO dto = new PersonaDTO("", "Perez", "123456789");

        PersonaResponseDTO respuesta = personaService.registrarPersona(dto);

        assertFalse(respuesta.isExito());
        assertEquals("El nombre no puede estar vacio", respuesta.getMensaje());
        verifyNoInteractions(cuentaClient);
    }

    @Test
    void errorDeCuentaServiceSePropagaEnElMensaje() {
        when(cuentaClient.crearCuenta(any())).thenReturn(
                CuentaResponse.newBuilder()
                        .setExito(false)
                        .setMensaje("El numero de identificacion debe contener solo digitos y tener entre 6 y 10 caracteres")
                        .build());

        personaService = construirServicio();
        PersonaDTO dto = new PersonaDTO("Juan", "Perez", "123456789");

        PersonaResponseDTO respuesta = personaService.registrarPersona(dto);

        assertFalse(respuesta.isExito());
        assertTrue(respuesta.getMensaje().startsWith("Error al crear la cuenta"));
    }
}
