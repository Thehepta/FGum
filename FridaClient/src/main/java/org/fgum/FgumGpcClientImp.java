package org.fgum;



import com.fgum.client.protocol.FgumClientGrpc;
import com.fgum.type.Empty;
import com.fgum.type.StringArgument;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.fgum.service.protocol.FgumServiceGrpc;

public class FgumGpcClientImp extends FgumClientGrpc.FgumClientImplBase {

    FgumServiceGrpc.FgumServiceBlockingStub iServerInface;

    FgumGpcClientImp(ManagedChannel channel){
        iServerInface = FgumServiceGrpc.newBlockingStub(channel);


    }


    @Override
    public void onMessage(StringArgument request, StreamObserver<Empty> responseObserver) {
        String data = request.getData();
        System.out.println(data);
        Empty empty = Empty.newBuilder().build();
        responseObserver.onNext(empty);
        responseObserver.onCompleted();
    }



}
