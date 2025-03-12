package com.liu.soyaojcodesandbox.sandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.liu.soyaojcodesandbox.checker.DictionaryTreeChecker;
import com.liu.soyaojcodesandbox.constant.FileConstant;
import com.liu.soyaojcodesandbox.model.ExecuteCodeRequest;
import com.liu.soyaojcodesandbox.model.ExecuteCodeResponse;
import com.liu.soyaojcodesandbox.model.ExecuteMessage;
import com.liu.soyaojcodesandbox.model.JudgeInfo;
import com.liu.soyaojcodesandbox.process.ProcessUtils;
import com.liu.soyaojcodesandbox.process.TaskGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
public class JavaNativeCodeSandBox implements CodeSandbox {
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4,
            8, 1000L * 3,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<>(8));

    @Override
    public ExecuteCodeResponse execute(ExecuteCodeRequest request) throws ExecutionException, InterruptedException {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        JudgeInfo judgeInfo = new JudgeInfo();

        String code = request.getCode();
        //todo code权限校验
        // 不允许执行阻塞done 占用内存不释放done 读文件 写文件 运行其他程序 执行高危命令
        //校验代码，布隆过滤器
        //字典树
        if (DictionaryTreeChecker.checkExist(code)) {
            //有违禁词
            judgeInfo.setSuccess(false);
            judgeInfo.setMessage("vulnerable code detected!");
            executeCodeResponse.setJudgeInfo(judgeInfo);
            return executeCodeResponse;
        }
        //容器化技术
        List<String> inputList = request.getInputList();
        //todo 这里需要根据语言类型选择执行命令
        String language = request.getLanguage();
        inputList.add("1 2");
        inputList.add("3 4");
        inputList.add("5 6");
        inputList.add("7 8");
        inputList.add("9 10");
        inputList.add("11 12");
        inputList.add("13 14");
        inputList.add("15 16");
        inputList.add("16 17");

        UUID uuid = UUID.randomUUID();
        //这个大概率是不存在的
        String userCodeDir = FileConstant.OUT_PUT_DIR + File.separator + uuid;
        log.info("创建用户代码目录");
        FileUtil.mkdir(userCodeDir);

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
            log.debug(e.getMessage());
            judgeInfo.setMessage("SystemError");
            executeCodeResponse.setJudgeInfo(judgeInfo);
            return executeCodeResponse;
        }

        if (!StrUtil.isEmpty(compileMsg.getErrorMessage())) {
            log.info("编译失败");
            judgeInfo.setMessage(compileMsg.getErrorMessage());
            executeCodeResponse.setJudgeInfo(judgeInfo);
            return executeCodeResponse;
        }

        //用于记录程序的执行时间
        StopWatch stopWatch = new StopWatch();
        long maxTime = 0;
        //执行运行命令并获取输出
        File usrCodeDir = new File(userCodeDir);
        for (String inputArgs : inputList) {
            //设置最大的JVM堆内存
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=utf8 -cp %s Main %s", usrCodeDir.getAbsolutePath(), inputArgs);
            ExecuteMessage runMsg;
            stopWatch.start();
            Future<ExecuteMessage> submitted = threadPoolExecutor.submit(new TaskGenerator(runCmd, inputArgs));
            runMsg = submitted.get();
            if (!StrUtil.isEmpty(runMsg.getErrorMessage())) {
                //运行错误，需要特殊处理
                //出现错误直接封装返回
                judgeInfo.setMessage(runMsg.getErrorMessage());
                executeCodeResponse.setJudgeInfo(judgeInfo);
                return executeCodeResponse;
            }
            stopWatch.stop();
            long millis = stopWatch.getLastTaskTimeMillis();
            maxTime = Math.max(maxTime, millis);
            executeCodeResponse.getOutput().add(runMsg.getMessage());
        }

        judgeInfo.setSuccess(true);
        judgeInfo.setTime(maxTime);
        judgeInfo.setMessage("ok");
        executeCodeResponse.setJudgeInfo(judgeInfo);

        //文件清理,先判断一下这个文件是否存在
        if (FileUtil.exist(userCodeDir)) {
            FileUtil.del(userCodeDir);
        }
        //根据运行或者编译的结果封装ExecuteCodeResponse返回
        return executeCodeResponse;
    }
}
