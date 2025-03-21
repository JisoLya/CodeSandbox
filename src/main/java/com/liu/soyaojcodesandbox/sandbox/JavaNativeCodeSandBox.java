package com.liu.soyaojcodesandbox.sandbox;

import com.liu.soyaojcodesandbox.checker.BloomFilter;
import com.liu.soyaojcodesandbox.checker.DictionaryTreeFilter;
import com.liu.soyaojcodesandbox.model.ExecuteCodeRequest;
import com.liu.soyaojcodesandbox.model.ExecuteCodeResponse;
import com.liu.soyaojcodesandbox.model.ExecuteMessage;
import com.liu.soyaojcodesandbox.process.TaskGenerator;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class JavaNativeCodeSandBox extends CodeSandboxTemplate implements CodeSandbox {

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
    public ExecuteCodeResponse execute(ExecuteCodeRequest request) {
        return super.execute(request);
    }

    @Override
    public List<ExecuteMessage> RunCode(String classDir, List<String> inputList) throws ExecutionException, InterruptedException {
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        List<Future<ExecuteMessage>> futures = new ArrayList<>();
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=utf8 -cp %s Main %s", classDir, inputArgs);
            futures.add(threadPoolExecutor.submit(new TaskGenerator(runCmd)));
        }
        for (Future<ExecuteMessage> future : futures) {
            ExecuteMessage executeMessage = future.get();
            executeMessageList.add(executeMessage);
        }
        return executeMessageList;
    }
}
