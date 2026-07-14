package com.smartsoft.persona.service.impl;

import com.smartsoft.cuenta.grpc.CuentaGrpcServiceGrpc;
import com.smartsoft.cuenta.grpc.CuentaRequest;
import com.smartsoft.cuenta.grpc.CuentaResponse;
import com.smartsoft.persona.dto.PersonaDTO;
import com.smartsoft.persona.dto.PersonaResponseDTO;
import com.smartsoft.persona.service.PersonaService;

import io.quarkus.grpc.GrpcClient;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion de PersonaService. Anotada como @ApplicationScoped
 * para que Quarkus (CDI) la inyecte donde se necesite la interfaz.
 *
 * Flujo:
 * 1. Valida nombre, apellido y numeroIdentificacion.
 * 2. Si son validos, llama por gRPC a cuenta-service enviando numeroIdentificacion
 *    y el nombre completo, para que cree el numero de cuenta.
 * 3. Devuelve un unico PersonaResponseDTO combinando ambos resultados.
 */
@ApplicationScoped
public class PersonaServiceImpl implements PersonaService {

    private static final String REGEX_NUMERO_IDENTIFICACION = "\\d{6,10}";

    @GrpcClient("cuenta")
    CuentaGrpcServiceGrpc.CuentaGrpcServiceBlockingStub cuentaClient;

    @Override
    public PersonaResponseDTO registrarPersona(PersonaDTO personaDTO) {
        List<String> errores = validar(personaDTO);

        if (!errores.isEmpty()) {
            return new PersonaResponseDTO(false, String.join("; ", errores), null, null, null);
        }

        String nombreCompleto = personaDTO.getNombre().trim() + " " + personaDTO.getApellido().trim();

        CuentaResponse cuentaResponse = solicitarNumeroCuenta(personaDTO.getNumeroIdentificacion(), nombreCompleto);

        if (!cuentaResponse.getExito()) {
            return new PersonaResponseDTO(
                    false,
                    "Error al crear la cuenta: " + cuentaResponse.getMensaje(),
                    null, null, null);
        }

        return new PersonaResponseDTO(
                true,
                "Success",
                nombreCompleto,
                personaDTO.getNumeroIdentificacion(),
                cuentaResponse.getNumeroCuenta());
    }

    private CuentaResponse solicitarNumeroCuenta(String numeroIdentificacion, String nombreCompleto) {
        CuentaRequest request = CuentaRequest.newBuilder()
                .setNumeroIdentificacion(numeroIdentificacion)
                .setNombreCompleto(nombreCompleto)
                .build();

        return cuentaClient.crearCuenta(request);
    }

    private List<String> validar(PersonaDTO personaDTO) {
        List<String> errores = new ArrayList<>();

        if (personaDTO == null) {
            errores.add("El cuerpo de la peticion no puede estar vacio");
            return errores;
        }

        if (esVacio(personaDTO.getNombre())) {
            errores.add("El nombre no puede estar vacio");
        }

        if (esVacio(personaDTO.getApellido())) {
            errores.add("El apellido no puede estar vacio");
        }

        String numeroIdentificacion = personaDTO.getNumeroIdentificacion();
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
}
