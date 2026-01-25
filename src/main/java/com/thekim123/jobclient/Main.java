package com.thekim123.jobclient;


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.ClientCallStreamObserver;
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
        Jobstream.StartJobRequest req = Jobstream.StartJobRequest.newBuilder()
                .setJobType("demo")
                .setPayload("hello")
                .build();

        System.out.println("StartJob streaming... (press ENTER to cancel stream client-side)");
        System.out.println("Tip: 서버 CancelJob은 jobId를 알아야 해서, 먼저 이벤트로 jobId 받은 뒤에 호출 가능");

        // ✅ 클라이언트 스트림 cancel(방법 B)을 체험: callStreamObserver.cancel()
        CustomClientResponseObserver observer = new CustomClientResponseObserver();
        stub.startJob(req, observer);

        // ENTER로 스트림 자체를 cancel (방법 B)
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String line = br.readLine();
        while (line != null) {
            if (line.equalsIgnoreCase("c")) {
                String jobId = observer.getJobIdRef();
                if (jobId == null) {
                    System.out.println("jobId not received yet");
                    continue;
                }

                Jobstream.CancelJobRequest cancelRequest = Jobstream.CancelJobRequest.newBuilder()
                        .setJobId(jobId)
                        .build();
                JobServiceGrpc.JobServiceBlockingStub blockingStub =
                        JobServiceGrpc.newBlockingStub(channel);
                Jobstream.CancelJobResponse response = blockingStub.cancelJob(cancelRequest);
                System.out.println("CancelJob Accepted: " + response.getAccepted());
            }

            if (line.equalsIgnoreCase("q")) {
                break;
            }

            line = br.readLine();
        }

        Thread.sleep(1000);
        channel.shutdownNow();
    }

}
