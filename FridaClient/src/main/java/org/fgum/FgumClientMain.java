package org.fgum;

import com.fgum.type.GrpcMessage;
import com.fgum.type.GrpcStatus;
import com.fgum.type.GrpcType;
import com.google.protobuf.ByteString;


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.fgum.service.protocol.FgumServiceGrpc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FgumClientMain {
    public static void main(String[] args) {

        String host = "127.0.0.1";      //remote android ip
        int port = 9903;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().maxInboundMessageSize(Integer.MAX_VALUE).build();

//        FgumServiceGrpc.FgumServiceBlockingStub iServerInface = FgumServiceGrpc.newBlockingStub(channel);

        FgumServiceGrpc.FgumServiceStub stub = FgumServiceGrpc.newStub(channel);

        StreamObserver<GrpcMessage> requestStreamObserver = stub.subscribe(new StreamObserver<GrpcMessage>() {

            @Override
            public void onNext(GrpcMessage response) {
                switch (response.getType()){
                    case  init: {
                        System.out.print("start process:"+response.getPid());
                        break;
                    }
                    case  log:{
                        String log = response.getContent().toStringUtf8();
                        System.out.print(log+"\n");
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server closed the stream.");
            }
        });
        requestStreamObserver.onNext(GrpcMessage.newBuilder().setType(GrpcType.init).setPid(UUID.randomUUID().variant()).build());
//        用requestStreamObserver 

        String filePath = "D:\\git\\FGum\\FridaClient\\FridaScrpit\\hook.js";
        try {
            String  js_script = Files.readString(Paths.get(filePath));
            GrpcMessage.Builder grpcMessage_build = GrpcMessage.newBuilder();
            grpcMessage_build.setType(GrpcType.file);
            grpcMessage_build.setContent(ByteString.copyFromUtf8(js_script));
            grpcMessage_build.setStatus(GrpcStatus.successful);
            requestStreamObserver.onNext(grpcMessage_build.build());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CountDownLatch latch = new CountDownLatch(1);

        // Wait for the server to complete
        try {
            latch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        channel.shutdown();

    }
}