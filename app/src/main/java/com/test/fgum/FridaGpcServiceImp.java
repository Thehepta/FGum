package com.test.fgum;


import android.util.ArrayMap;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.test.fgum.service.protocol.FridaServiceGrpc;
import com.test.fgum.type.Empty;
import com.test.fgum.type.Filebuff;
import com.test.fgum.type.GrpcMessage;
import com.test.fgum.type.GrpcType;
import com.test.fgum.type.Use;
import com.test.fgum.type.UseType;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import io.grpc.stub.StreamObserver;

public class FridaGpcServiceImp extends FridaServiceGrpc.FridaServiceImplBase {

    private boolean stopReading;
    //    public Map<Integer,>
    public Map<Integer,StreamObserver<GrpcMessage>> publish_list = new ArrayMap<>();
    public Map<Integer,StreamObserver<GrpcMessage>> subscribe_list = new ArrayMap<>();
    @Override
    public void loadJS(Filebuff request, StreamObserver<Empty> responseObserver) {
        byte [] js_buff = request.getContent().toByteArray();
        LoadEntry.loadScript(js_buff);
        Empty empty = Empty.newBuilder().build();
        responseObserver.onNext(empty);
        responseObserver.onCompleted();
    }
    public FridaGpcServiceImp(){

//        new Thread(){
//            @Override
//            public void run() {
//
//                LoadEntry.startWritingThread();
//            }
//        }.start();
//        new Thread(){
//            @Override
//            public void run() {
//
//                LoadEntry.startFridaThread();
//            }
//        }.start();
    }

    @Override
    public StreamObserver<GrpcMessage> subscribe(StreamObserver<GrpcMessage> responseObserver) {
        Log.e("rzx","subscribe");

        return  new StreamObserver<GrpcMessage>() {
            @Override
            public void onNext(GrpcMessage request) {
                HandleSubscribeMsg(responseObserver,request);

            }

            @Override
            public void onError(Throwable t) {
                // 处理流出现错误的情况
                System.err.println("Error from client: " + t.getMessage());
                subscribe_list.remove(responseObserver);

            }

            @Override
            public void onCompleted() {
                // 客户端流结束时执行的操作
                System.out.println("Client disconnected");
                subscribe_list.remove(responseObserver);
            }
        };
    }

    public void HandleSubscribeMsg(StreamObserver<GrpcMessage> responseObserver,GrpcMessage request){
        // 处理客户端发送的请求
        switch (request.getType()){
            case  init: {
                int id = request.getPid();
                subscribe_list.put(id,responseObserver);
                Log.e("rzx","HandleSubscribeMsg init");
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

    @Override
    public void regist(Use request, StreamObserver<Empty> responseObserver) {
        Log.e("rzx","regist:"+request.getId());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<GrpcMessage> publishes(StreamObserver<GrpcMessage> responseObserver) {

        return new StreamObserver<GrpcMessage>() {
            @Override
            public void onNext(GrpcMessage request) {
                HandlePublishesMsg(responseObserver,request);
            }

            @Override
            public void onError(Throwable t) {
                // 处理流出现错误的情况
                System.err.println("Error from client: " + t.getMessage());


            }

            @Override
            public void onCompleted() {
                // 客户端流结束时执行的操作
                System.out.println("Client disconnected");

            }
        };
    }


    public void HandlePublishesMsg(StreamObserver<GrpcMessage> responseObserver,GrpcMessage request){
        // 处理客户端发送的请求

        switch (request.getType()){
            case  init: {
                int pid = request.getPid();
                publish_list.put(pid,responseObserver);
                for ( int id :subscribe_list.keySet())
                {
                    subscribe_list.get(id).onNext(request);
                }
                Log.e("rzx","HandlePublishesMsg init");
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

//        try {
//            if(pushResponseStreamObserver == null){
//                return;
//            }
//            GrpcMessage response = GrpcMessage.newBuilder().setContent(ByteString.copyFromUtf8(log)).setType(GrpcType.log).setPid(android.os.Process.myPid()).build();
//            pushResponseStreamObserver.onNext(response);

//        }catch (Exception e){
//            Log.i("sendlog","sendlog exception");
//        }

        return;
    }
}
