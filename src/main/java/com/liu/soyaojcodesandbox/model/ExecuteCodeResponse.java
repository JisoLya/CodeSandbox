package com.liu.soyaojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ExecuteCodeResponse {
    public ExecuteCodeResponse() {
    }

    private JudgeInfo judgeInfo;
    /**
     * 程序输出
     */
    private List<String> output;

    /**
     * 执行的堆栈输出
     */
    private String stackInfo;
}
