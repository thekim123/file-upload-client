package com.thekim123.jobclient.router;

import com.thekim123.jobclient.control.JobControl;

import java.util.HashMap;
import java.util.Map;

public class CommandRouter {
    @FunctionalInterface
    public interface Handler {
        boolean handle(JobControl ctx, String args) throws Exception;
    }

    private final Map<String, Handler> handlers = new HashMap<>();

    public void register(String key, Handler h) {
        handlers.put(key.toLowerCase(), h);
    }

    public boolean dispatch(JobControl jobControl, String line) throws Exception {
        line = line.trim();
        if (line.isEmpty()) return true;

        String[] parts = line.split("\\s+", 2);
        String key = parts[0].toLowerCase();
        String args = (parts.length > 1) ? parts[1].trim() : "";

        Handler h = handlers.get(key);
        if (h == null) {
            System.out.println("unknown command: " + key);
            return true;
        }

        return h.handle(jobControl, args);
    }
}
