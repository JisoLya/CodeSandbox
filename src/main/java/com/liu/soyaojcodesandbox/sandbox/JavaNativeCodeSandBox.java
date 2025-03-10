package com.liu.soyaojcodesandbox.sandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.liu.soyaojcodesandbox.constant.FileConstant;
import com.liu.soyaojcodesandbox.model.ExecuteCodeRequest;
import com.liu.soyaojcodesandbox.model.ExecuteCodeResponse;
import com.liu.soyaojcodesandbox.model.ExecuteMessage;
import com.liu.soyaojcodesandbox.model.JudgeInfo;
import com.liu.soyaojcodesandbox.process.ProcessUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
public class JavaNativeCodeSandBox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse execute(ExecuteCodeRequest request) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        String code = request.getCode();
        List<String> inputList = request.getInputList();
        String language = request.getLanguage();
        inputList.add("1 2");
        inputList.add("3 4");
        UUID uuid = UUID.randomUUID();
        String userCodeDir = FileConstant.OUT_PUT_DIR + File.separator + uuid;
        if (!FileUtil.exist(userCodeDir)) {
            log.info("创建用户代码目录");
            FileUtil.mkdir(userCodeDir);
        }
        //将用户代码写入文件
        File userCodeFile = new File(userCodeDir + File.separator + FileConstant.JAVA_CLASS_NAME);
        FileUtil.writeString(code, userCodeFile, StandardCharsets.UTF_8);
        //执行编译命令
        //指定编译的错误输出为英文,直接避免出现乱码问题
        String compileCmd = String.format("javac -encoding utf8 -J-Duser.language=en %s", userCodeFile.getAbsolutePath());
        ExecuteMessage compileMsg;
        try {
            compileMsg = ProcessUtils.ExecuteCmd("compile", Runtime.getRuntime().exec(compileCmd));
        } catch (Exception e) {
            //todo 全局异常处理： 这里应该返回系统错误
            String errorMsg = e.getMessage();
            executeCodeResponse.getOutput().add(errorMsg);
            return executeCodeResponse;
        }
        if (compileMsg == null || !StrUtil.isEmpty(compileMsg.getErrorMessage())) {
            //todo 编译错误
            // 需要封装返回给response
            System.out.println("编译失败");
            executeCodeResponse.getOutput().add(compileMsg.getErrorMessage());
            return executeCodeResponse;
        }
        //用于记录程序的执行时间
        StopWatch stopWatch = new StopWatch();
        long maxTime = 0;
        //执行运行命令并获取输出
        File usrCodeDir = new File(userCodeDir);
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -Dfile.encoding=utf8 -cp %s Main %s", usrCodeDir.getAbsolutePath(), inputArgs);
            ExecuteMessage runMsg;
            stopWatch.start();
            try {
                runMsg = ProcessUtils.ExecuteCmd("run", Runtime.getRuntime().exec(runCmd));
            } catch (Exception e) {
                String errorMsg = e.getMessage();
                executeCodeResponse.getOutput().add(errorMsg);
                return executeCodeResponse;
            }
            if (!StrUtil.isEmpty(runMsg.getErrorMessage())) {
                //运行错误，需要特殊处理
                //出现错误直接封装返回
                executeCodeResponse.getOutput().add(runMsg.getErrorMessage());
                JudgeInfo errorJudgeInfo = new JudgeInfo();
                errorJudgeInfo.setMessage("failed");
                executeCodeResponse.setJudgeInfo(errorJudgeInfo);
                return executeCodeResponse;
            }
            stopWatch.stop();
            long millis = stopWatch.getLastTaskTimeMillis();
            maxTime = Math.max(maxTime, millis);
            executeCodeResponse.getOutput().add(runMsg.getMessage());
        }
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        judgeInfo.setMessage("ok");
        executeCodeResponse.setJudgeInfo(judgeInfo);
        //根据运行或者编译的结果封装ExecuteCodeResponse返回
        return executeCodeResponse;
    }
}
