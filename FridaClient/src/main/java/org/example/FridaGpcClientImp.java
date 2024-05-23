package org.example;

import com.kone.pbdemo.protocol.Empty;
import com.kone.pbdemo.protocol.FridaClientGrpc;
import com.kone.pbdemo.protocol.FridaServiceGrpc;
import com.kone.pbdemo.protocol.StringArgument;
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
