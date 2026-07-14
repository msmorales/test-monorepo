package com.smartsoft.persona.controller;

import com.smartsoft.persona.dto.PersonaDTO;
import com.smartsoft.persona.dto.PersonaResponseDTO;
import com.smartsoft.persona.service.PersonaService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Capa de controller (recurso REST). Unica responsabilidad: exponer el
 * endpoint HTTP y delegar la logica (incluida la validacion) a PersonaService.
 */
@Path("/api/personas")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PersonaController {

    @Inject
    PersonaService personaService;

    @POST
    public Response crearPersona(PersonaDTO personaDTO) {
        PersonaResponseDTO respuesta = personaService.registrarPersona(personaDTO);

        if (respuesta.isExito()) {
            return Response.status(Response.Status.OK).entity(respuesta).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).entity(respuesta).build();
    }
}
