package com.thekim123.jobclient;


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
        while (true) {
            String line = br.readLine();
            if (line == null) break;
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.equalsIgnoreCase("q")) {
                observer.onCompleted();
                break;
            }

            if (line.equalsIgnoreCase("x")) {
                var call = observer.getCall().get();
//                call.cancel("client hard cancel", null);
                break;
            }

            if (line.equalsIgnoreCase("p")) {
                requestStream.onNext(Jobstream.ClientCommand.newBuilder()
                        .setJobId(observer.getJobIdRef() == null ? "" : observer.getJobIdRef().get())
                        .setPause(Jobstream.Pause.newBuilder().build())
                        .build());
                continue;
            }

            if (line.equals("r")) {
                requestStream.onNext(Jobstream.ClientCommand.newBuilder()
                        .setJobId(observer.getJobIdRef().get() == null ? "" : observer.getJobIdRef().get())
                        .setResume(Jobstream.Resume.newBuilder().build())
                        .build());
                continue;
            }

            if (line.startsWith("c")) {
                String reason = line.length() > 1 ? line.substring(1).trim() : "user requested";
                requestStream.onNext(Jobstream.ClientCommand.newBuilder()
                        .setJobId(observer.getJobIdRef().get() == null ? "" : observer.getJobIdRef().get())
                        .setCancel(Jobstream.Cancel.newBuilder().setReason(reason).build())
                        .build());
                continue;
            }

            if (line.startsWith("s ")) {
                int n = Integer.parseInt(line.substring(2).trim());
                requestStream.onNext(Jobstream.ClientCommand.newBuilder()
                        .setJobId(observer.getJobIdRef().get() == null ? "" : observer.getJobIdRef().get())
                        .setSetSpeed(Jobstream.SetSpeed.newBuilder().setEventsPerSec(n).build())
                        .build());
                continue;
            }

            if (line.startsWith("l ")) {
                int lv = Integer.parseInt(line.substring(2).trim());
                requestStream.onNext(Jobstream.ClientCommand.newBuilder()
                        .setJobId(observer.getJobIdRef().get() == null ? "" : observer.getJobIdRef().get())
                        .setSetLogLevel(Jobstream.SetLogLevel.newBuilder().setLevel(lv).build())
                        .build());
                continue;
            }

            if (line.equalsIgnoreCase("c")) {
                String jobId = observer.getJobIdRef().get();
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

        }

        Thread.sleep(1000);
        channel.shutdownNow();
    }

}
