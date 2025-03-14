package com.liu.soyaojcodesandbox.sandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.liu.soyaojcodesandbox.checker.BloomFilter;
import com.liu.soyaojcodesandbox.checker.DictionaryTreeFilter;
import com.liu.soyaojcodesandbox.constant.FileConstant;
import com.liu.soyaojcodesandbox.model.ExecuteCodeRequest;
import com.liu.soyaojcodesandbox.model.ExecuteCodeResponse;
import com.liu.soyaojcodesandbox.model.ExecuteMessage;
import com.liu.soyaojcodesandbox.model.JudgeInfo;
import com.liu.soyaojcodesandbox.process.ProcessUtils;
import com.liu.soyaojcodesandbox.process.TaskGenerator;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
public class JavaNativeCodeSandBox implements CodeSandbox {

    private final BloomFilter bloomFilter = new BloomFilter();

    private final DictionaryTreeFilter dictionaryTreeFilter = new DictionaryTreeFilter();

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4,
            8, 1000L * 3,
            TimeUnit.MILLISECONDS,
            //无上限的任务队列
            new LinkedBlockingDeque<>(8),
            //不能采用Abort策略
            new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
    public ExecuteCodeResponse execute(ExecuteCodeRequest request) throws ExecutionException, InterruptedException {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        JudgeInfo judgeInfo = new JudgeInfo();

        String code = request.getCode();
        //todo code权限校验
        // 不允许执行阻塞done 占用内存不释放done 读文件 写文件 运行其他程序 执行高危命令
        //校验代码，布隆过滤器

        //其实这里设计的不太好，因为布隆过滤器存在误判，不能因为误判从而拒绝用户的代码提交.
        if (bloomFilter.checkExist(code)) {
            judgeInfo.setSuccess(false);
            judgeInfo.setMessage("vulnerable code detected!");
            executeCodeResponse.setJudgeInfo(judgeInfo);
            return executeCodeResponse;
        }
        //字典树done
//        if (dictionaryTreeFilter.checkExist(code)) {
//            //有违禁词
//            judgeInfo.setSuccess(false);
//            judgeInfo.setMessage("vulnerable code detected!");
//            executeCodeResponse.setJudgeInfo(judgeInfo);
//            return executeCodeResponse;
//        }
        //容器化技术
        List<String> inputList = request.getInputList();
        //todo 这里需要根据语言类型选择执行命令
        String language = request.getLanguage();

        UUID uuid = UUID.randomUUID();
        //这个大概率是不存在的
        String userCodeDir = FileConstant.OUT_PUT_DIR + File.separator + uuid;
        log.info("创建用户代码目录");
        FileUtil.mkdir(userCodeDir);

        //将用户代码写入文件
        File userCodeFile = new File(userCodeDir + File.separator + FileConstant.JAVA_CLASS_NAME);
        FileUtil.writeString(code, userCodeFile, StandardCharsets.UTF_8);
        //执行编译命令
        //指定编译的错误输出为英文,避免出现乱码问题
        String compileCmd = String.format("javac -encoding utf8 -J-Duser.language=en %s", userCodeFile.getAbsolutePath());
        ExecuteMessage compileMsg;
        try {
            compileMsg = ProcessUtils.ExecuteCmd("compile", Runtime.getRuntime().exec(compileCmd));
        } catch (Exception e) {
            log.debug(e.getMessage());
            judgeInfo.setMessage("SystemError");
            executeCodeResponse.setJudgeInfo(judgeInfo);
            return executeCodeResponse;
        } finally {
            clearUserCodeDir(userCodeDir);
        }

        if (!StrUtil.isEmpty(compileMsg.getErrorMessage())) {
            log.info("编译失败");
            judgeInfo.setMessage(compileMsg.getErrorMessage());
            executeCodeResponse.setJudgeInfo(judgeInfo);
            clearUserCodeDir(userCodeDir);
            return executeCodeResponse;
        }

        //用于记录程序的执行时间

        //执行运行命令并获取输出
        File usrCodeDir = new File(userCodeDir);
        List<Future<ExecuteMessage>> futures = new ArrayList<>();
        for (String inputArgs : inputList) {
            //设置最大的JVM堆内存
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=utf8 -cp %s Main %s", usrCodeDir.getAbsolutePath(), inputArgs);
            futures.add(threadPoolExecutor.submit(new TaskGenerator(runCmd)));
        }

        long maxTime = 0;
        for (Future<ExecuteMessage> future : futures) {
            try {
                ExecuteMessage runMsg = future.get(); // 按任务完成顺序获取结果,捕获线程抛出的异常
                if (!StrUtil.isEmpty(runMsg.getErrorMessage())) {
                    judgeInfo.setMessage(runMsg.getErrorMessage());
                    executeCodeResponse.setJudgeInfo(judgeInfo);
                    clearUserCodeDir(userCodeDir);
                    return executeCodeResponse;
                }
                maxTime = Math.max(maxTime, runMsg.getRunTime());
                executeCodeResponse.getOutput().add(runMsg.getMessage());
            } catch (Exception e) {
                //系统错误
                log.debug(e.getMessage());
                judgeInfo.setMessage("SystemError");
                executeCodeResponse.setJudgeInfo(judgeInfo);
                return executeCodeResponse;
            } finally {
                clearUserCodeDir(userCodeDir);
            }
        }
        judgeInfo.setSuccess(true);
        judgeInfo.setTime(maxTime);
        judgeInfo.setMessage("ok");
        executeCodeResponse.setJudgeInfo(judgeInfo);

        //文件清理,先判断一下这个文件是否存在
        clearUserCodeDir(userCodeDir);
        //根据运行或者编译的结果封装ExecuteCodeResponse返回
        return executeCodeResponse;
    }

    private void clearUserCodeDir(String userCodeDir) {
        if (FileUtil.exist(userCodeDir)) {
            FileUtil.del(userCodeDir);
        }
    }
}
