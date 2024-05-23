package org.example;

import com.kone.pbdemo.protocol.FridaServiceGrpc;
import com.kone.pbdemo.protocol.StringArgument;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class Main {
    public static void main(String[] args) {

        String host = "192.168.1.2";      //remote android ip
        String dir = "D:\\apk\\work\\vip\\dump";   //pc dex dir
        int port = 9091;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().maxInboundMessageSize(Integer.MAX_VALUE).build();

        FridaGpcClientImp service = new FridaGpcClientImp(channel);

        FridaServiceGrpc.FridaServiceStub stub = FridaServiceGrpc.newStub(channel);

        // 调用服务方法，并接收来自服务端推送的消息
        StringArgument stringArgument = StringArgument.newBuilder().build();
        stub.streamMessages(stringArgument, new StreamObserver<StringArgument>() {
            @Override
            public void onNext(StringArgument response) {
                System.out.println("Received message: " + response.getData());
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
//        System.out.println("Hello world!");
    }
}