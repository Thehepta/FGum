package org.example;


import com.test.fgum.client.protocol.FridaClientGrpc;
import com.test.fgum.service.protocol.FridaServiceGrpc;
import com.test.fgum.type.Empty;
import com.test.fgum.type.StringArgument;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

public class FridaGpcClientImp extends FridaClientGrpc.FridaClientImplBase {

    FridaServiceGrpc.FridaServiceBlockingStub iServerInface;

    FridaGpcClientImp(ManagedChannel channel){
        iServerInface = FridaServiceGrpc.newBlockingStub(channel);


    }


    @Override
    public void onMessage(StringArgument request, StreamObserver<Empty> responseObserver) {
        String data = request.getData();
        System.out.println(data);
        Empty empty = Empty.newBuilder().build();
        responseObserver.onNext(empty);
        responseObserver.onCompleted();
    }




}
