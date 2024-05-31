package com.test.fgum;


import android.content.Context;
import android.util.Log;

import com.test.fgum.service.protocol.FridaServiceGrpc;
import com.test.fgum.type.Empty;
import com.test.fgum.type.Use;
import com.test.fgum.type.UseType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;


public class LoadEntry {

    static int port ;
    public static void Entry(Context context, String source, String argument){
        PreLoadNativeSO(context,source);
        port = 9903;
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(){
            @Override
            public void run() {
                startServer();

            }
        }.start();

        new Thread(){
            @Override
            public void run() {
                ProcessGrpcClient processGrpcClient = new ProcessGrpcClient();
                processGrpcClient.startClient(port);
                latch.countDown();
            }
        }.start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void
    startServer() {

        Server server = null;
        try {
            server = NettyServerBuilder
                    .forPort(port)
                    .addService(new FridaGpcServiceImp())
                    .maxInboundMessageSize(Integer.MAX_VALUE)
                    .build()
                    .start();
            Log.e("LoadEntry","server started, port : " + port);
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {

        }
    }



    public static void PreLoadNativeSO(Context context, String source) {
        try {
            String abi= "arm64-v8a";
            if(!android.os.Process.is64Bit()){
                abi = "armeabi-v7a";
            }
            String libdump = source+"!/lib/"+abi+"/libfgum.so";
            System.load(libdump);
        }catch (Exception e){
            Log.e("LoadEntry","LoadSo error");
        }
    }


//    static {
//        System.loadLibrary("fgum");
//
//    }


    public static native void loadScript(byte[] js_buff);
    public static native void startWritingThread();
    public static native void startFridaThread();


    public static boolean sendlog(String log){
        ProcessGrpcClient.sendlog(log);
        return true;
    }





}
