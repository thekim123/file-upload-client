package com.thekim123.jobclient.control;

import com.thekim123.jobclient.CustomClientResponseObserver;
import io.grpc.stub.StreamObserver;
import jobstream.v1.Jobstream;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GrpcJobControl implements JobControl {
    private final StreamObserver<Jobstream.ClientCommand> requestStream;
    private final CustomClientResponseObserver observer;

    @Override
    public boolean quit() {
        observer.onCompleted();
        return false;
    }

    @Override
    public boolean hardCancel() {
        var call = observer.getCall().get();
//                call.cancel("client hard cancel", null);
        return false;
    }

    @Override
    public boolean pause() {
        requestStream.onNext(Jobstream.ClientCommand.newBuilder()
                .setPause(Jobstream.Pause.newBuilder().build())
                .build());
        return true;
    }

    @Override
    public boolean resume() {
        requestStream.onNext(Jobstream.ClientCommand.newBuilder()
                .setJobId(observer.getJobIdRef().get() == null ? "" : observer.getJobIdRef().get())
                .setResume(Jobstream.Resume.newBuilder().build())
                .build());
        return true;
    }

    @Override
    public boolean cancel(String reason) {
        requestStream.onNext(Jobstream.ClientCommand.newBuilder()
                .setJobId(observer.getJobIdRef().get() == null ? "" : observer.getJobIdRef().get())
                .setCancel(Jobstream.Cancel.newBuilder().setReason(reason).build())
                .build());
        return false;
    }

    @Override
    public boolean setSpeed(int n) {
        requestStream.onNext(Jobstream.ClientCommand.newBuilder()
                .setJobId(observer.getJobIdRef().get() == null ? "" : observer.getJobIdRef().get())
                .setSetSpeed(Jobstream.SetSpeed.newBuilder().setEventsPerSec(n).build())
                .build());
        return true;
    }

    @Override
    public boolean setLogLevel(int lv) {
        requestStream.onNext(Jobstream.ClientCommand.newBuilder()
                .setJobId(observer.getJobIdRef().get() == null ? "" : observer.getJobIdRef().get())
                .setSetLogLevel(Jobstream.SetLogLevel.newBuilder().setLevel(lv).build())
                .build());
        return true;
    }
}
