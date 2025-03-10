package com.liu.soyaojcodesandbox.model;

import lombok.Data;

/**
 * 题目判断信息
 */
@Data
public class JudgeInfo {
    /**
     * 代码是否执行完成
     */
    private boolean success;
    /**
     * 判题信息
     */
    private String message;
    /**
     * 判题所耗时长
     */
    private Long time;
    /**
     * 内存占用
     */
    private Long memory;
}
