package com.thekim123.jobclient;


import com.thekim123.jobclient.control.GrpcJobControl;
import com.thekim123.jobclient.control.JobControl;
import com.thekim123.jobclient.router.CommandRouter;
import com.thekim123.jobclient.router.Router;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import jobstream.v1.JobServiceGrpc;
import jobstream.v1.Jobstream;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws Exception {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        JobServiceGrpc.JobServiceStub stub = JobServiceGrpc.newStub(channel);


        System.out.println("StartJob streaming... (press ENTER to cancel stream client-side)");
        System.out.println("Tip: 서버 CancelJob은 jobId를 알아야 해서, 먼저 이벤트로 jobId 받은 뒤에 호출 가능");

        // 클라이언트 스트림 cancel(방법 B)을 체험: callStreamObserver.cancel()

        CustomClientResponseObserver observer = new CustomClientResponseObserver();
        StreamObserver<Jobstream.ClientCommand> requestStream = stub.runJob(observer);

        Jobstream.ClientCommand request = Jobstream.ClientCommand.newBuilder()
                .setStart(Jobstream.Start.newBuilder().setJobType("demo").setPayload("hello").build())
                .build();
        requestStream.onNext(request);

        System.out.println("""
                Commands:
                  p            -> PAUSE
                  r            -> RESUME
                  c [reason]   -> CANCEL (server-side)
                  s <n>        -> SET_SPEED (events/sec)
                  l <0|1|2>    -> SET_LOG_LEVEL
                  x            -> client hard cancel (transport)
                  q            -> close send (onCompleted)
                """);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        CommandRouter router = Router.buildRouter();
        JobControl jobControl = new GrpcJobControl(requestStream, observer);
        while (true) {
            String line = br.readLine();
            if (line == null) break;
            router.dispatch(jobControl, line);
        }

        Thread.sleep(1000);
        channel.shutdownNow();
    }

}
