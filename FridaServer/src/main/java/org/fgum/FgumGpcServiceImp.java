package org.fgum;
import com.fgum.type.*;
import com.google.protobuf.ByteString;
import java.util.HashMap;
import java.util.Map;

import io.grpc.stub.StreamObserver;
import org.fgum.service.protocol.FgumServiceGrpc;

public class FgumGpcServiceImp extends FgumServiceGrpc.FgumServiceImplBase {

    private boolean stopReading;
    //    public Map<Integer,>
    public Map<Integer,StreamObserver<GrpcMessage>> publish_list = new HashMap<>();
    public Map<Integer,StreamObserver<GrpcMessage>> subscribe_list = new HashMap<>();
    byte [] current_js_buff ;

    @Override
    public void getCurJScript(Empty request, StreamObserver<GrpcMessage> responseObserver) {

        GrpcMessage.Builder grpcBuilder = GrpcMessage.newBuilder();
        if(current_js_buff == null){
            grpcBuilder.setStatus(GrpcStatus.failed);
        }else {
            grpcBuilder.setStatus(GrpcStatus.successful).setContent(ByteString.copyFrom(current_js_buff));
        }
        responseObserver.onNext(grpcBuilder.build());
        responseObserver.onCompleted();
    }

    public FgumGpcServiceImp(){
        System.out.println("FgumGpcServiceImp");
    }
    //�Ͷ����ߵ�˫�����������Ƕ�������������ص������Ǹ������ߵ���
    @Override
    public StreamObserver<GrpcMessage> subscribe(StreamObserver<GrpcMessage> responseObserver) {
                System.out.println("subscribe");

        return  new StreamObserver<GrpcMessage>() {
            @Override
            public void onNext(GrpcMessage request) {
                System.out.println("subscribe");
//                HandleSubscribeMsg(responseObserver,request);
            }

            @Override
            public void onError(Throwable t) {
                // ���������ִ�������
                subscribe_list.remove(responseObserver);
//                Log.e("rzx","subscribe onCompleted");

            }

            @Override
            public void onCompleted() {
                // �ͻ���������ʱִ�еĲ���
                subscribe_list.remove(responseObserver);
//                Log.e("rzx","subscribe onCompleted");
            }
        };
    }



    @Override
    public void regist(Use request, StreamObserver<Empty> responseObserver) {
//        Log.e("rzx","regist:"+request.getId());
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
                // ���������ִ�������
                System.err.println("Error from client: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                // �ͻ���������ʱִ�еĲ���
                System.out.println("Client disconnected");

            }
        };
    }



    public void HandleSubscribeMsg(StreamObserver<GrpcMessage> responseObserver,GrpcMessage request){
        // ����ͻ��˷��͵�����
        switch (request.getType()){
            case  init: {
//                Log.e("rzx","HandleSubscribeMsg init");
                //�����ߣ��ͻ���ע��
                int id = request.getPid();
                subscribe_list.put(id,responseObserver);
                break;
            }
            case  file:{
//                Log.e("rzx","HandleSubscribeMsg file");
                //���ܶ����ߴ���������ļ�������ת����ÿһ�������磨���̣�
                if(current_js_buff == null){
                    current_js_buff = request.getContent().toByteArray();
                    break;
                }
                current_js_buff = request.getContent().toByteArray();
                GrpcMessage.Builder grpcMessage_build = GrpcMessage.newBuilder();
                grpcMessage_build.setType(GrpcType.file);
                grpcMessage_build.setContent(ByteString.copyFrom(current_js_buff));
                grpcMessage_build.setStatus(GrpcStatus.successful);
                for ( int id :publish_list.keySet())
                {
                    publish_list.get(id).onNext(grpcMessage_build.build());
                }

            }
        }
    }

    public void HandlePublishesMsg(StreamObserver<GrpcMessage> responseObserver,GrpcMessage PublishesMsg){

        switch (PublishesMsg.getType()){
            case  init: {
                int pid = PublishesMsg.getPid();
                publish_list.put(pid,responseObserver);
                for ( int id :subscribe_list.keySet())
                {
                    subscribe_list.get(id).onNext(PublishesMsg);
                }
//                Log.e("rzx","HandlePublishesMsg init:"+pid);
                break;
            }
            case  file:{
                  // ����ͻ��˷��͵����� �����粻��������������ļ���Ϣ��ֻ�ᷢ����־��init
            }
            case  cmd: {
            }
            case  log: {
//                Log.e("rzx","HandlePublishesMsg log");
                //����־���͸�ÿ��������
                for ( int id :subscribe_list.keySet())
                {
                    try {
                        subscribe_list.get(id).onNext(PublishesMsg);
                    }catch (Exception e){
                        subscribe_list.remove(id);
                    }

                }
            }
        }
    }
}
