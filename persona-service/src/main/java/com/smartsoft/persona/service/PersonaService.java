package com.smartsoft.persona.service;

import com.smartsoft.persona.dto.PersonaDTO;
import com.smartsoft.persona.dto.PersonaResponseDTO;

/**
 * Capa de servicio (contrato). El controller depende de esta interfaz,
 * nunca de la implementacion concreta.
 */
public interface PersonaService {

    PersonaResponseDTO registrarPersona(PersonaDTO personaDTO);
}
