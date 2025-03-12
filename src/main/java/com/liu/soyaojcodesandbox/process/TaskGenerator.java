package com.liu.soyaojcodesandbox.process;

import com.liu.soyaojcodesandbox.model.ExecuteMessage;
import org.springframework.util.StopWatch;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


public class TaskGenerator implements Callable<ExecuteMessage> {
    public String cmd;

    public TaskGenerator(String cmd) {
        this.cmd = cmd;
    }

    @Override
    public ExecuteMessage call() {
        ExecuteMessage runMsg;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        boolean timeout = false;
        try {
            Process exec = Runtime.getRuntime().exec(this.cmd);
            //判断是否出现死循环等问题
            if (!exec.waitFor(4L, TimeUnit.SECONDS)) {
                //执行超时
                System.out.println("执行超时");
                exec.destroyForcibly();
                timeout = true;
            }
            stopWatch.stop();
            runMsg = ProcessUtils.ExecuteCmd("run", exec);
            if (!timeout) {
                runMsg.setRunTime(stopWatch.getTotalTimeMillis());
            } else {
                //超时了
                runMsg.setErrorMessage("运行超时");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return runMsg;
    }
}
