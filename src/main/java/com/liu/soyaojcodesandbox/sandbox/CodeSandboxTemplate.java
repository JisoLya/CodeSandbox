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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
public abstract class CodeSandboxTemplate implements CodeSandbox {
    public String CreateUserCodeDir() {
        UUID uuid = UUID.randomUUID();
        //这个大概率是不存在的
        String userCodeDir = FileConstant.OUT_PUT_DIR + File.separator + uuid;
        File file = FileUtil.mkdir(userCodeDir);
        log.info("创建用户代码目录 : {}", file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    public String WriteCodeToFile(String code, String fileName) {
        File file = new File(fileName);
        FileUtil.writeString(code, file, StandardCharsets.UTF_8);
        return file.getAbsolutePath();
    }

    public ExecuteMessage CompileCode(File codeFile, JudgeInfo judgeInfo) {
        String compileCmd = String.format("javac -encoding utf8 -J-Duser.language=en %s", codeFile.getAbsolutePath());
        ExecuteMessage compileMsg = null;
        try {
            compileMsg = ProcessUtils.ExecuteCmd("Compile", Runtime.getRuntime().exec(compileCmd));
        } catch (Exception e) {
            log.error("error : {}", e.getMessage());
            log.debug("System compile err");
            judgeInfo.setMessage("SystemError");
            return null;
        }
        return compileMsg;
    }

    public List<ExecuteMessage> RunCode(String classDir, List<String> inputList) throws ExecutionException, InterruptedException {
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=utf8 -cp %s Main %s", classDir, inputArgs);
            try {
                long startTime = System.currentTimeMillis();
                ExecuteMessage runCodeMsg = ProcessUtils.ExecuteCmd("Run", Runtime.getRuntime().exec(runCmd));
                long endTime = System.currentTimeMillis();
                runCodeMsg.setRunTime(endTime - startTime);
                executeMessageList.add(runCodeMsg);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return executeMessageList;
    }

    @Override
    public ExecuteCodeResponse execute(ExecuteCodeRequest request) {
        String code = request.getCode();
        List<String> inputList = request.getInputList();
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        JudgeInfo judgeInfo = new JudgeInfo();

        String userCodeDir = CreateUserCodeDir();
        String codeFile = userCodeDir + File.separator + FileConstant.JAVA_CLASS_NAME;
        codeFile = WriteCodeToFile(code, codeFile);

        ExecuteMessage compileMsg = CompileCode(new File(codeFile), judgeInfo);
        if (compileMsg == null) {
            executeCodeResponse.setJudgeInfo(judgeInfo);
            return executeCodeResponse;
        }
        if (StrUtil.isNotBlank(compileMsg.getErrorMessage())) {
            log.info("编译失败: {}", compileMsg.getErrorMessage());
            judgeInfo.setMessage(compileMsg.getErrorMessage());
            executeCodeResponse.setJudgeInfo(judgeInfo);
            ClearUserCodeDir(userCodeDir);
            return executeCodeResponse;
        }
        log.info("编译成功");
        //执行代码
        long maxTime = 0;
        ArrayList<String> outputList = new ArrayList<>();
        try {
            List<ExecuteMessage> runCodeMessages = RunCode(userCodeDir, inputList);
            for (ExecuteMessage msg : runCodeMessages) {
                if (StrUtil.isNotBlank(msg.getErrorMessage())) {
                    judgeInfo.setMessage(msg.getErrorMessage());
                    executeCodeResponse.setJudgeInfo(judgeInfo);
                    return executeCodeResponse;
                }
                outputList.add(msg.getMessage());
                maxTime = Math.max(maxTime, msg.getRunTime());
            }
        } catch (Exception e) {
            judgeInfo.setMessage("SystemError");
            executeCodeResponse.setJudgeInfo(judgeInfo);
            return executeCodeResponse;
        }
        //执行成功了
        judgeInfo.setMessage("ok");
        judgeInfo.setTimeLimit(maxTime);
        judgeInfo.setSuccess(true);
        executeCodeResponse.setOutput(outputList);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        executeCodeResponse.setId(request.getId());
        executeCodeResponse.setQuestionSubmitId(request.getQuestionSubmitId());
        ClearUserCodeDir(userCodeDir);
        return executeCodeResponse;
    }

    public void ClearUserCodeDir(String userCodeDir) {
        if (FileUtil.exist(userCodeDir)) {
            FileUtil.del(userCodeDir);
        }
    }
}
