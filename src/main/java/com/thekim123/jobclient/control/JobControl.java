package com.thekim123.jobclient.control;

public interface JobControl {
    boolean pause();

    boolean resume();

    boolean cancel(String reason);

    boolean setSpeed(int n);

    boolean setLogLevel(int lv);

    boolean hardCancel();

    boolean quit();
}
