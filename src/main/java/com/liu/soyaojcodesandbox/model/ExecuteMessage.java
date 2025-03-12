package com.liu.soyaojcodesandbox.model;

import lombok.Data;

/**
 * 进程的执行信息
 */
@Data
public class ExecuteMessage {
    /**
     * 程序的正常执行输出
     */
    private String message;

    /**
     * 程序的错误输出
     */
    private String errorMessage;

    /**
     * 程序的执行时间
     */
    private Long runTime;
}
