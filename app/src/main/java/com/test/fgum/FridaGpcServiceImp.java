package com.test.fgum;


import android.util.Log;

import com.google.protobuf.ByteString;
import com.kone.pbdemo.protocol.Empty;
import com.kone.pbdemo.protocol.Filebuff;
import com.kone.pbdemo.protocol.FridaServiceGrpc;

import com.kone.pbdemo.protocol.GrpcMessage;
import com.kone.pbdemo.protocol.GrpcType;

import java.nio.ByteBuffer;

import io.grpc.stub.StreamObserver;

public class FridaGpcServiceImp extends FridaServiceGrpc.FridaServiceImplBase {

    private boolean stopReading;

    public static StreamObserver<GrpcMessage> pushResponseStreamObserver = null;
    @Override
    public void loadJS(Filebuff request, StreamObserver<Empty> responseObserver) {
        byte [] js_buff = request.getContent().toByteArray();
        LoadEntry.loadScript(js_buff);
        Empty empty = Empty.newBuilder().build();
        responseObserver.onNext(empty);
        responseObserver.onCompleted();
    }
    public FridaGpcServiceImp(){
        Log.e("rzx","FridaGpcServiceImp");
    }

    @Override
    public StreamObserver<GrpcMessage> subscribe(StreamObserver<GrpcMessage> responseObserver) {

        pushResponseStreamObserver = responseObserver;

        return  new StreamObserver<GrpcMessage>() {
            @Override
            public void onNext(GrpcMessage request) {
                // 处理客户端发送的请求
                System.out.println("Received data from client: " + request.getType());
                switch (request.getType()){
                    case  file:{
                        byte [] js_buff = request.getContent().toByteArray();
                        LoadEntry.loadScript(js_buff);
                    }
                }
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
                pushResponseStreamObserver = null;
            }
        };

    }

    private static ByteBuffer buffer;

    private synchronized void notifyWritingThread() {
        buffer.notify();
    }

    private   void stopReadingThread(){
        stopReading = true;
    }

    public  void startReadingThread(){
        new Thread(){
            @Override
            public void run() {
                stopReading = false;
                while (!stopReading) {
                    synchronized (buffer) {
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);
                        buffer.clear();
                        String message = new String(data).trim();
                        System.out.println("Message from C++: " + message);
                        notifyWritingThread();
                    }
                    try {
                        Thread.sleep(100);  // Sleep for 100ms
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public static void sendlog(String log){

        if(pushResponseStreamObserver == null){
            return;
        }
        GrpcMessage response = GrpcMessage.newBuilder().setContent(ByteString.copyFromUtf8(log)).setType(GrpcType.log).build();
        pushResponseStreamObserver.onNext(response);

    }
}
