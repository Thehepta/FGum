package com.test.fgum;


import android.content.Context;
import android.util.Log;

import com.fgum.type.Empty;
import com.fgum.type.Use;
import com.fgum.type.UseType;

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
        String host = "192.168.12.104";  //remote android ip
        port = 9903;
        Log.e("LoadEntry","client:" +host+" ip: " + port);
        CountDownLatch latch = new CountDownLatch(1);
        //不能在主线程中加载frida，单开一个线程加载，主线程等待加载完成
        new Thread(){
            @Override
            public void run() {
                ProcessGrpcClient processGrpcClient = new ProcessGrpcClient();
                processGrpcClient.startClient(host, port);
//                latch.countDown();
            }
        }.start();
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }

    private static void
    startServer() {  //grpc 中专server，目前分离开来，不在客户端使用
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


    public static native void loadScript(byte[] js_buff);
    public static native void startLogThread();
    public static native void startFridaThread();


    public static boolean sendlog(String log){
        return ProcessGrpcClient.sendlog(log);
    }





}
