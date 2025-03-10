package com.liu.soyaojcodesandbox.process;

import com.liu.soyaojcodesandbox.model.ExecuteMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ProcessUtils {

    public static ExecuteMessage ExecuteCmd(String operationName, Process process) throws IOException, InterruptedException {
        ExecuteMessage executeMessage = new ExecuteMessage();
        // 等待程序执行，获取错误码
        int exitValue = process.waitFor();
        // 正常退出
        if (exitValue == 0) {
            System.out.printf("执行成功 operationName:%s\n", operationName);
            // 分批获取进程的正常输出
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(),StandardCharsets.UTF_8));
            StringBuilder compileOutputStringBuilder = new StringBuilder();
            // 逐行读取
            String compileOutputLine;
            while ((compileOutputLine = bufferedReader.readLine()) != null) {
                compileOutputStringBuilder.append(compileOutputLine);
            }
//            System.out.println(compileOutputStringBuilder);
            executeMessage.setMessage(compileOutputStringBuilder.toString());
        } else {
            // 异常退出
            System.out.printf("执行失败 operationName: %s错误码： %d\n", operationName, exitValue);
            // 分批获取进程的正常输出
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(),StandardCharsets.UTF_8));
            StringBuilder compileOutputStringBuilder = new StringBuilder();
            // 逐行读取
            String compileOutputLine;
            while ((compileOutputLine = bufferedReader.readLine()) != null) {
                compileOutputStringBuilder.append(compileOutputLine);
            }
            // 分批获取进程的错误输出
            BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder errorCompileOutputStringBuilder = new StringBuilder();

            // 逐行读取
            String errorCompileOutputLine;
            while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null) {
                errorCompileOutputStringBuilder.append(errorCompileOutputLine);
            }
            executeMessage.setErrorMessage(errorCompileOutputStringBuilder.toString());
        }
        return executeMessage;
    }
}
