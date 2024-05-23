package org.example;

import com.kone.pbdemo.protocol.Empty;
import com.kone.pbdemo.protocol.FridaClientGrpc;
import com.kone.pbdemo.protocol.StringArgument;
import io.grpc.stub.StreamObserver;

public class FridaGpcClientImp extends FridaClientGrpc.FridaClientImplBase {

    @Override
    public void onMessage(StringArgument request, StreamObserver<Empty> responseObserver) {
        String data = request.getData();
        System.out.println(data);
        Empty empty = Empty.newBuilder().build();
        responseObserver.onNext(empty);
        responseObserver.onCompleted();
    }
}
