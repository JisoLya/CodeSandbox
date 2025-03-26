package com.liu.soyaojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteCodeRequest {
    /**
     * 题目提交的ID
     */
    private long questionSubmitId;
    /**
     * 题目的id
     */
    private Long id;

    /**
     * 代码语言
     */
    private String language;

    /**
     * 用户
     */
    private String code;

    /**
     * 输入用例
     */
    private List<String> inputList;

    /**
     * 时间限制
     */
    private Long timeLimit;
}
