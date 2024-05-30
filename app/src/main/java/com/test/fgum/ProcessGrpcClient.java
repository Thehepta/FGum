package com.test.fgum;

import android.os.Process;
import android.util.Log;

import com.test.fgum.service.protocol.AppProcessClientGrpc;
import com.test.fgum.service.protocol.FridaServiceGrpc;
import com.test.fgum.type.Empty;
import com.test.fgum.type.Filebuff;
import com.test.fgum.type.GrpcMessage;
import com.test.fgum.type.GrpcType;
import com.test.fgum.type.Use;
import com.test.fgum.type.UseType;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class ProcessGrpcClient extends AppProcessClientGrpc.AppProcessClientImplBase {

    @Override
    public void loadJS(Filebuff request, StreamObserver<Empty> responseObserver) {
        super.loadJS(request, responseObserver);
    }

    void startClient(int port) {
        while (true){
            try {
                String host = "127.0.0.1";      //remote android ip
                ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().maxInboundMessageSize(Integer.MAX_VALUE).build();
                FridaServiceGrpc.FridaServiceStub stub = FridaServiceGrpc.newStub(channel);
                StreamObserver<GrpcMessage> requestStreamObserver = stub.publishes(new StreamObserver<GrpcMessage>() {
                    @Override
                    public void onNext(GrpcMessage value) {

                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
                requestStreamObserver.onNext(GrpcMessage.newBuilder().setPid(Process.myPid()).setType(GrpcType.init).build());

                return;
            }catch (Exception e){
                Log.e("Rzx","startClient Failed");
            }
        }
    }
}
