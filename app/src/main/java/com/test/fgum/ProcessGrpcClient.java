package com.test.fgum;

import android.os.Process;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.test.fgum.service.protocol.FridaServiceGrpc;
import com.test.fgum.type.Empty;
import com.test.fgum.type.Filebuff;
import com.test.fgum.type.GrpcMessage;
import com.test.fgum.type.GrpcStatus;
import com.test.fgum.type.GrpcType;


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class ProcessGrpcClient  {

    public static StreamObserver<GrpcMessage> requestStreamObserver =null;



    ProcessGrpcClient(){
        new Thread(){
            @Override
            public void run() {

                LoadEntry.startWritingThread();
            }
        }.start();
        new Thread(){
            @Override
            public void run() {

                LoadEntry.startFridaThread();
            }
        }.start();
    }


    void startClient(int port) {
        StreamObserver<GrpcMessage> tmp_requestStreamObserver;
        while (true){
            try {
                String host = "127.0.0.1";      //remote android ip
                ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().maxInboundMessageSize(Integer.MAX_VALUE).build();
                FridaServiceGrpc.FridaServiceStub stub = FridaServiceGrpc.newStub(channel);
                tmp_requestStreamObserver = stub.publishes(new StreamObserver<GrpcMessage>() {
                    @Override
                    public void onNext(GrpcMessage value) {
                        HandleSubscribeMsg(value);
                    }

                    @Override
                    public void onError(Throwable t) {
                        requestStreamObserver = null;
                    }

                    @Override
                    public void onCompleted() {
                        requestStreamObserver = null;
                    }
                });
                tmp_requestStreamObserver.onNext(GrpcMessage.newBuilder().setPid(Process.myPid()).setType(GrpcType.init).build());
                requestStreamObserver = tmp_requestStreamObserver;
                while (true) {
                    try {
                        FridaServiceGrpc.FridaServiceBlockingStub iServerInface = FridaServiceGrpc.newBlockingStub(channel);
                        GrpcMessage grpcMessage = iServerInface.getCurJScript(Empty.newBuilder().build());
                        if(grpcMessage.getStatus() == GrpcStatus.failed){
                            continue;
                        }
                        LoadEntry.loadScript(grpcMessage.getContent().toByteArray());
                        break;
                    }catch (Exception e){

                    }
                }
                return;
            }catch (Exception e){
                Log.e("Rzx","startClient Failed");
            }
        }



    }


    public void HandleSubscribeMsg(GrpcMessage request){
        // 处理客户端发送的请求
        switch (request.getType()){
            case  init: {

                break;
            }
            case  file:{
                byte [] js_buff = request.getContent().toByteArray();
                LoadEntry.loadScript(js_buff);
            }
            case  cmd: {
            }
            case  log: {
            }

        }
    }

    public static void sendlog(String log){

        try {
            if(requestStreamObserver == null){
                return;
            }
            GrpcMessage response = GrpcMessage.newBuilder().setContent(ByteString.copyFromUtf8(log)).setType(GrpcType.log).setPid(android.os.Process.myPid()).build();
            requestStreamObserver.onNext(response);

        }catch (Exception e){
            Log.i("sendlog","sendlog exception");
        }

    }
}
