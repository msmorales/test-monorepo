# persona-cuenta

Monorepo con dos microservicios Quarkus: `persona-service` (REST, expuesto al exterior) y `cuenta-service` (gRPC, interno). `persona-service` recibe los datos de una persona, los valida y le pide a `cuenta-service` que genere un número de cuenta.

## Arquitectura

```
Cliente (Postman, front, etc.)
        |
        | HTTP/JSON  POST /api/personas
        v
 ┌─────────────────┐        gRPC (interno)        ┌─────────────────┐
 │  persona-service │ ────────────────────────────> │  cuenta-service │
 │  (REST, :8080)   │ <──────────────────────────── │  (gRPC, :9000)  │
 └─────────────────┘                                └─────────────────┘
```

`persona-service` es siempre el cliente gRPC; `cuenta-service` es siempre el servidor y nunca se expone fuera del pod/red interna.

En OpenShift, ambos contenedores viven en el **mismo Pod** (patrón sidecar): comparten namespace de red, por lo que `persona-service` llama a `cuenta-service` por `localhost:9000`. En Docker Compose (contenedores separados) se comunican por el nombre del servicio (`cuenta-service`) sobre una red Docker compartida.

## Estructura del repositorio

```
persona-cuenta/
├── persona-service/        # REST, capas controller/service/serviceImpl
│   ├── src/main/java/com/smartsoft/persona/
│   │   ├── controller/     # PersonaController (endpoint REST)
│   │   ├── dto/            # PersonaDTO (request), PersonaResponseDTO (response)
│   │   └── service/impl/   # PersonaServiceImpl (validacion + cliente gRPC)
│   ├── src/main/proto/      # cuenta.proto (contrato gRPC, copia identica a cuenta-service)
│   ├── src/main/docker/     # Dockerfile.jvm
│   └── src/test/            # Pruebas unitarias e integracion
├── cuenta-service/          # gRPC, capas grpc/service/serviceImpl
│   ├── src/main/java/com/smartsoft/cuenta/
│   │   ├── grpc/            # CuentaGrpcServiceImpl (endpoint gRPC)
│   │   ├── dto/              # CuentaResultado (resultado interno)
│   │   └── service/impl/     # CuentaServiceImpl (validacion + generacion de cuenta)
│   ├── src/main/proto/       # cuenta.proto (contrato gRPC)
│   ├── src/main/docker/      # Dockerfile.jvm
│   └── src/test/
├── docker-compose.yml        # Levanta ambos servicios en contenedores separados
├── openshift/
│   ├── persona-cuenta.yaml   # Manifiesto consolidado (ImageStream, BuildConfig, DC, Service, Route)
│   ├── cm-persona-service.yaml
│   ├── cm-cuenta-service.yaml
│   ├── dc-persona-service.yaml
│   ├── svc-persona-service.yaml
│   └── route-persona-service.yaml
├── deploy-persona-cuenta.ps1  # Automatiza clonar/pull + build + docker compose (Windows)
└── deploy-persona-cuenta.sh   # Mismo script para Git Bash / WSL / macOS / Linux
```

## Tecnologias

- Java 17, Maven
- Quarkus 3.15.x
- REST: `quarkus-resteasy-reactive-jackson`
- gRPC: `quarkus-grpc`
- Health checks: `quarkus-smallrye-health` (`/q/health/live`, `/q/health/ready`)
- Tests: JUnit 5, RestAssured, Mockito
- Docker (imagenes JVM sobre `ubi8/openjdk-17`)
- OpenShift (DeploymentConfig, Service, Route, ImageStream, BuildConfig)

## persona-service (REST)

Endpoint: `POST /api/personas`

Request:
```json
{
  "nombre": "Juan",
  "apellido": "Perez",
  "numeroIdentificacion": "123456789"
}
```

Reglas de validacion:
- `nombre` y `apellido` no pueden estar vacios.
- `numeroIdentificacion` debe ser solo digitos, entre 6 y 10 caracteres.

Response (200, exito):
```json
{
  "exito": true,
  "mensaje": "Success",
  "nombreCompleto": "Juan Perez",
  "numeroIdentificacion": "123456789",
  "numeroCuenta": "4839201847561023"
}
```

Response (400, error de validacion o de cuenta-service):
```json
{
  "exito": false,
  "mensaje": "El nombre no puede estar vacio",
  "nombreCompleto": null,
  "numeroIdentificacion": null,
  "numeroCuenta": null
}
```

## cuenta-service (gRPC)

Contrato (`cuenta.proto`): recibe `numeroIdentificacion` + `nombreCompleto`, valida de forma independiente (mismas reglas de numeroIdentificacion, nombreCompleto no vacio) y genera un numero de cuenta de 16 digitos si todo es correcto.

## Como ejecutar en local (sin Docker)

En IntelliJ IDEA: File > Open, selecciona `persona-service` (o `cuenta-service`), IntelliJ detecta el `pom.xml` e importa el proyecto Maven.

Cada servicio se corre por separado:
```bash
cd cuenta-service
mvn quarkus:dev
```
```bash
cd persona-service
mvn quarkus:dev
```
`persona-service` necesita que `cuenta-service` este corriendo (por defecto se conecta a `localhost:9000`).

## Como ejecutar con Docker

```bash
docker compose up --build
```

Levanta `cuenta-service` (puertos 8081 health, 9000 gRPC) y `persona-service` (puerto 8080) en una red compartida. Prueba con:
```bash
curl -X POST http://localhost:8080/api/personas -H "Content-Type: application/json" -d '{"nombre":"Juan","apellido":"Perez","numeroIdentificacion":"123456789"}'
```

### Automatizar descarga + despliegue local

`deploy-persona-cuenta.ps1` (Windows) / `deploy-persona-cuenta.sh` (Git Bash/WSL/macOS/Linux): clonan o actualizan el repo, compilan ambos servicios con Maven y levantan `docker compose`, esperando a que `persona-service` responda antes de terminar.

```powershell
.\deploy-persona-cuenta.ps1
```

## Como desplegar en OpenShift

Los dos contenedores van en el mismo Pod (`dc-persona-service.yaml` / `openshift/persona-cuenta.yaml`), comunicandose por `localhost`. Solo `persona-service` tiene `Service` y `Route`; `cuenta-service` queda inaccesible desde fuera del pod.

```bash
oc apply -f openshift/persona-cuenta.yaml
mvn -f persona-service clean package && oc start-build persona-service --from-dir=persona-service --follow
mvn -f cuenta-service clean package && oc start-build cuenta-service --from-dir=cuenta-service --follow
oc get route persona-service
```

Los archivos `cm-*.yaml`, `dc-*.yaml`, `svc-*.yaml`, `route-*.yaml` son la version separada por tipo de recurso (un archivo por ConfigMap/DeploymentConfig/Service/Route) que pidio el equipo de infraestructura; describen exactamente los mismos recursos que `persona-cuenta.yaml`.

## Pruebas

```bash
cd persona-service && mvn test
cd cuenta-service && mvn test
```

Las pruebas de exito de `persona-service` usan Mockito para simular la respuesta de `cuenta-service` (no requieren tenerlo corriendo). Las pruebas de integracion HTTP (`@QuarkusTest`) solo cubren los casos de validacion que fallan antes de llamar por gRPC.

## Notas

- El puerto 8080 (persona-service, HTTP) y el 9000 (cuenta-service, gRPC) no deben chocar con otros contenedores del mismo pod en OpenShift, ya que los contenedores de un mismo Pod comparten namespace de red.
- Si usas PowerShell y el script `.ps1` no corre por politica de ejecucion, usa `powershell -ExecutionPolicy Bypass -File .\deploy-persona-cuenta.ps1` o `Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned`.
