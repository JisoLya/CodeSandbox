package com.liu.soyaojcodesandbox.sandbox;


import com.liu.soyaojcodesandbox.model.ExecuteCodeRequest;
import com.liu.soyaojcodesandbox.model.ExecuteCodeResponse;

import java.util.concurrent.ExecutionException;

public interface CodeSandbox {
    /**
     * 执行代码
     * @param request 执行代码的请求
     * @return
     */
    ExecuteCodeResponse execute(ExecuteCodeRequest request) throws ExecutionException, InterruptedException;
}
