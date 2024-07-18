package org.fgum;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class FgumServiceMain {
    public static void main(String[] args) throws InterruptedException, IOException {
        int port = 9903;
        Server server = ServerBuilder
                .forPort(port)
                .addService(new FgumGpcServiceImp())
                .build()
                .start();
        System.out.println("server started, port : " + port);
        server.awaitTermination();
    }
}