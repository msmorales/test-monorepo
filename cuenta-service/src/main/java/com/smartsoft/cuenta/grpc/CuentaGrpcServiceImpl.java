package com.smartsoft.cuenta.grpc;

import com.smartsoft.cuenta.dto.CuentaResultado;
import com.smartsoft.cuenta.service.CuentaService;

import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;

import jakarta.inject.Inject;

/**
 * Capa "controller" para gRPC: unica responsabilidad es exponer el metodo
 * remoto y delegar la logica de negocio a CuentaService.
 *
 * CuentaGrpcServiceGrpc, CuentaRequest y CuentaResponse son generados por
 * Quarkus/protoc a partir de src/main/proto/cuenta.proto (mismo paquete
 * com.smartsoft.cuenta.grpc, por eso no requieren import explicito aqui).
 */
@GrpcService
public class CuentaGrpcServiceImpl extends CuentaGrpcServiceGrpc.CuentaGrpcServiceImplBase {

    @Inject
    CuentaService cuentaService;

    @Override
    public void crearCuenta(CuentaRequest request, StreamObserver<CuentaResponse> responseObserver) {
        CuentaResultado resultado = cuentaService.crearCuenta(
                request.getNumeroIdentificacion(),
                request.getNombreCompleto());

        CuentaResponse.Builder builder = CuentaResponse.newBuilder()
                .setExito(resultado.isExito())
                .setMensaje(resultado.getMensaje());

        if (resultado.getNumeroCuenta() != null) {
            builder.setNumeroCuenta(resultado.getNumeroCuenta());
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
