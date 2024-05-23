package com.test.fgum;

import com.kone.pbdemo.protocol.Empty;
import com.kone.pbdemo.protocol.Filebuff;
import com.kone.pbdemo.protocol.FridaClientGrpc;
import com.kone.pbdemo.protocol.FridaServiceGrpc;

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

    public void sendlog(String log){

    }

    public void setChannel(Server server) {
//        iFridaGpcClient = FridaClientGrpc.newBlockingStub(server);
    }
}
