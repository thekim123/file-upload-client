package com.thekim123.jobclient.router;

import com.thekim123.jobclient.control.JobControl;

public class Router {

    public static CommandRouter buildRouter() {
        CommandRouter router = new CommandRouter();
        router.register("p", (ctx, args) -> ctx.pause());
        router.register("c", JobControl::cancel);
        router.register("r", (ctx, args) -> ctx.resume());
        router.register("q", (ctx, args) -> ctx.quit());
        router.register("x", (ctx, args) -> ctx.hardCancel());
        router.register("s", (ctx, args) -> ctx.setSpeed(Integer.parseInt(args)));
        router.register("l", (ctx, args) -> ctx.setLogLevel(Integer.parseInt(args)));
        return router;
    }


}
