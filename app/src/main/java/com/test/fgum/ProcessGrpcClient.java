package com.test.fgum;

import android.os.Process;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.fgum.type.Empty;
import com.fgum.type.Filebuff;
import com.fgum.type.GrpcMessage;
import com.fgum.type.GrpcStatus;
import com.fgum.type.GrpcType;


import org.fgum.service.protocol.FgumServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class ProcessGrpcClient  {

    public static StreamObserver<GrpcMessage> requestStreamObserver =null;



    ProcessGrpcClient(){
        //单独开一个线程读取日志
        new Thread(){
            @Override
            public void run() {
                LoadEntry.startLogThread();
            }
        }.start();
    }


    void startClient(String host,int port) {
        StreamObserver<GrpcMessage> tmp_requestStreamObserver;
        while (true){
            try {
                ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().maxInboundMessageSize(Integer.MAX_VALUE).build();
                FgumServiceGrpc.FgumServiceStub stub = FgumServiceGrpc.newStub(channel);
                tmp_requestStreamObserver = stub.publishes(new StreamObserver<GrpcMessage>() {
                    @Override
                    public void onNext(GrpcMessage value) {
                        HandleSubscribeMsg(value);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e("Rzx","ProcessGrpcClient onError");
                        requestStreamObserver = null;
                    }

                    @Override
                    public void onCompleted() {
                        Log.e("Rzx","ProcessGrpcClient onCompleted");
                        requestStreamObserver = null;
                    }
                });
                tmp_requestStreamObserver.onNext(GrpcMessage.newBuilder().setPid(Process.myPid()).setType(GrpcType.init).build());
                requestStreamObserver = tmp_requestStreamObserver;
                while (true) {
                    try {
                        FgumServiceGrpc.FgumServiceBlockingStub iServerInface = FgumServiceGrpc.newBlockingStub(channel);
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
                Log.e("Rzx","ProcessGrpcClient load script");
                byte [] js_buff = request.getContent().toByteArray();

                new Thread(){
                    @Override
                    public void run() {
                        LoadEntry.loadScript(js_buff);
                    }
                }.start();

            }
            case  cmd: {
            }
            case  log: {
            }

        }
    }

    public static boolean sendlog(String log){

        try {

            GrpcMessage response = GrpcMessage.newBuilder().setContent(ByteString.copyFromUtf8(log)).setType(GrpcType.log).setPid(android.os.Process.myPid()).build();
            requestStreamObserver.onNext(response);

        }catch (Exception e){
            Log.i("sendlog","sendlog exception");
        }
        return true;
    }
}
