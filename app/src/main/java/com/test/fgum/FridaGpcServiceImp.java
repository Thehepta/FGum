package com.test.fgum;

import com.kone.pbdemo.protocol.Empty;
import com.kone.pbdemo.protocol.Filebuff;
import com.kone.pbdemo.protocol.FridaClientGrpc;
import com.kone.pbdemo.protocol.FridaServiceGrpc;
import com.kone.pbdemo.protocol.StringArgument;

import io.grpc.Server;
import io.grpc.stub.StreamObserver;

public class FridaGpcServiceImp extends FridaServiceGrpc.FridaServiceImplBase {

    static {
        System.loadLibrary("fgum");
    }

    public native boolean loadbuff(byte [] js_buff);

    @Override
    public void loadJS(Filebuff request, StreamObserver<Empty> responseObserver) {
        byte [] js_buff = request.getContent().toByteArray();
        loadbuff(js_buff);
        Empty empty = Empty.newBuilder().build();
        responseObserver.onNext(empty);
        responseObserver.onCompleted();
    }

    @Override
    public void streamMessages(StringArgument request, StreamObserver<StringArgument> responseObserver) {
        // 构造消息对象并发送给客户端

        responseObserver.onNext(request);
    }

    public void sendlog(String log){

    }

}
