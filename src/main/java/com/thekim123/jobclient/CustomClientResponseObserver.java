package com.thekim123.jobclient;

import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import jobstream.v1.Jobstream;

import java.util.concurrent.atomic.AtomicReference;

public class CustomClientResponseObserver implements ClientResponseObserver<Jobstream.StartJobRequest, Jobstream.JobEvent> {

    private volatile ClientCallStreamObserver<Jobstream.StartJobRequest> requestStreamRef;
    private final AtomicReference<String> jobIdRef = new AtomicReference<>(null);

    @Override
    public void beforeStart(ClientCallStreamObserver<Jobstream.StartJobRequest> requestStream) {
        requestStreamRef = requestStream;
//        requestStream.disableAutoInboundFlowControl(); // (선택) 흐름제어 직접 하고 싶으면
//        requestStream.request(3);
    }

    @Override
    public void onNext(Jobstream.JobEvent ev) {
        if (jobIdRef.get() == null && !ev.getJobId().isEmpty()) {
            jobIdRef.set(ev.getJobId());
        }

        if (ev.hasProgress()) {
            System.out.println("[" + ev.getJobId() + "] progress=" + ev.getProgress().getPercent() + "%");
        } else if (ev.hasLog()) {
            System.out.println("[" + ev.getJobId() + "] log: " + ev.getLog().getMessage());
        } else if (ev.hasCompleted()) {
            System.out.println("[" + ev.getJobId() + "] DONE: " + ev.getCompleted().getResult());
        } else if (ev.hasCanceled()) {
            System.out.println("[" + ev.getJobId() + "] CANCELED: " + ev.getCanceled().getReason());
        } else if (ev.hasFailed()) {
            System.out.println("[" + ev.getJobId() + "] FAILED: " + ev.getFailed().getError());
        }

        // 다음 이벤트 요청(흐름제어)
//        if (this.requestStreamRef != null) {
//            this.requestStreamRef.request(3);
//        }
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("stream error: " + t);
    }

    @Override
    public void onCompleted() {
        System.out.println("stream completed");
    }

    public ClientCallStreamObserver<Jobstream.StartJobRequest> getRequestStreamRef() {
        return requestStreamRef;
    }

    public String getJobIdRef() {
        return this.jobIdRef.get();
    }
}
