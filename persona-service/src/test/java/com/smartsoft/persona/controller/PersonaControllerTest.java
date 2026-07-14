package com.smartsoft.persona.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * Pruebas de integracion HTTP. Solo se cubren aqui los casos de validacion
 * que NUNCA llegan a invocar a cuenta-service por gRPC (fallan antes).
 *
 * El caso "datos validos -> Success" requiere que cuenta-service este
 * corriendo (por eso se prueba con docker-compose o manualmente via Postman).
 * La logica de exito, incluyendo la llamada gRPC simulada, se cubre con
 * PersonaServiceImplTest (mock del cliente gRPC).
 */
@QuarkusTest
class PersonaControllerTest {

    @Test
    void testCrearPersonaSinNombreDevuelveError() {
        String json = """
                {
                  "nombre": "",
                  "apellido": "Perez",
                  "numeroIdentificacion": "123456789"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(json)
        .when()
                .post("/api/personas")
        .then()
                .statusCode(400)
                .body("exito", equalTo(false))
                .body("mensaje", equalTo("El nombre no puede estar vacio"));
    }

    @Test
    void testCrearPersonaConNumeroIdentificacionCortoDevuelveError() {
        String json = """
                {
                  "nombre": "Juan",
                  "apellido": "Perez",
                  "numeroIdentificacion": "123"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(json)
        .when()
                .post("/api/personas")
        .then()
                .statusCode(400)
                .body("exito", equalTo(false))
                .body("mensaje", equalTo("El numero de identificacion debe contener solo digitos y tener entre 6 y 10 caracteres"));
    }

    @Test
    void testCrearPersonaConNumeroIdentificacionNoNumericoDevuelveError() {
        String json = """
                {
                  "nombre": "Juan",
                  "apellido": "Perez",
                  "numeroIdentificacion": "abc123456"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(json)
        .when()
                .post("/api/personas")
        .then()
                .statusCode(400)
                .body("exito", equalTo(false));
    }
}
