package com.liu.soyaojcodesandbox.process;

import com.liu.soyaojcodesandbox.model.ExecuteMessage;

import java.util.concurrent.Callable;


public class TaskGenerator implements Callable<ExecuteMessage> {
    public String cmd;

    public TaskGenerator(String cmd, String args) {
        this.cmd = cmd;
    }

    @Override
    public ExecuteMessage call() {
        ExecuteMessage runMsg;
        System.out.println(Thread.currentThread().getName() + "run");
        try {
            Process exec = Runtime.getRuntime().exec(this.cmd);
            new Thread(() -> {
                try {
                    Thread.sleep(1000L * 3);
                    System.out.println("执行超时了");
                    exec.destroy();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            runMsg = ProcessUtils.ExecuteCmd("run", exec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return runMsg;
    }
}
