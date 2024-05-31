package org.example;

import com.google.protobuf.ByteString;

import com.test.fgum.service.protocol.FridaServiceGrpc;
import com.test.fgum.type.GrpcMessage;
import com.test.fgum.type.GrpcType;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {

        String host = "192.168.0.79";      //remote android ip
        int port = 9903;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().maxInboundMessageSize(Integer.MAX_VALUE).build();

        FridaServiceGrpc.FridaServiceBlockingStub iServerInface = FridaServiceGrpc.newBlockingStub(channel);

        FridaServiceGrpc.FridaServiceStub stub = FridaServiceGrpc.newStub(channel);

        StreamObserver<GrpcMessage> requestStreamObserver = stub.subscribe( new StreamObserver<GrpcMessage>() {

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

        String filePath = "D:\\Project\\git\\FGum\\FridaClient\\FridaScrpit\\hook.js";
        try {
            byte[] js_byte = Files.readAllBytes(Paths.get(filePath));
            GrpcMessage file =  GrpcMessage.newBuilder().setType(GrpcType.file).setContent(ByteString.copyFrom(js_byte)).build();
            requestStreamObserver.onNext(file);

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