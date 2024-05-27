package com.test.fgum;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

public class LoadEntry {

    static FridaGpcServiceImp fridaGpcServiceImp;
    public static void Entry(Context context, String source, String argument){
        PreLoadNativeSO(context,source);
        CountDownLatch latch = new CountDownLatch(1);

        Thread thread = new Thread(){
            @Override
            public void run() {
                int port = 9093;
                Server server = null;
                try {
                    server = NettyServerBuilder
                            .forPort(port)
                            .addService(new FridaGpcServiceImp(latch))
                            .maxInboundMessageSize(Integer.MAX_VALUE)
                            .build()
                            .start();
                    Log.e("LoadEntry","server started, port : " + port);
                    server.awaitTermination();

                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        thread.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
        FridaGpcServiceImp.sendlog(log);
        return true;
    }





}
