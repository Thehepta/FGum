package com.test.fgum;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

public class LoadEntry {


    public static void Entry(Context context, String source, String argument){
        new Thread(){
            @Override
            public void run() {
                int port = 9091;

                Server server = null;
                try {
                    FridaGpcServiceImp fridaGpcServiceImp = new FridaGpcServiceImp();
                    server = NettyServerBuilder
                            .forPort(port)
                            .addService(fridaGpcServiceImp)
                            .maxInboundMessageSize(Integer.MAX_VALUE)
                            .build()
                            .start();
                    Log.e("LoadEntry","server started, port : " + port);
                    server.awaitTermination();
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();

    }

}
