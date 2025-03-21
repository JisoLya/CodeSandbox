/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liu.soyaojcodesandbox.demos.web;

import com.liu.soyaojcodesandbox.model.ExecuteCodeRequest;
import com.liu.soyaojcodesandbox.model.ExecuteCodeResponse;
import com.liu.soyaojcodesandbox.sandbox.JavaDockerCodeSandBox;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Controller
public class BasicController {
    // 定义鉴权请求头和密钥
    private static final String AUTH_REQUEST_HEADER = "auth";

    private static final String AUTH_REQUEST_SECRET = "secretKey";


    // http://127.0.0.1:8080/hello?name=lisi
    @RequestMapping("/hello")
    @ResponseBody
    public String hello(@RequestParam(name = "name", defaultValue = "unknown user") String name) {
        return "Hello " + name;
    }

    @PostMapping("/executeCode")
    @ResponseBody
    ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if (request == null) {
            throw new RuntimeException("request is null");
        }
        String authHeader = httpServletRequest.getHeader(AUTH_REQUEST_HEADER);
        if (authHeader == null || !AUTH_REQUEST_SECRET.equals(authHeader)) {
            httpServletResponse.setStatus(403);
            return null;
        }
        return JavaDockerCodeSandBox.getInstance().execute(request);
    }
}
