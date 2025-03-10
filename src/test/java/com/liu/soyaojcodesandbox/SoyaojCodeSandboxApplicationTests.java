package com.liu.soyaojcodesandbox;

import com.liu.soyaojcodesandbox.model.ExecuteCodeRequest;
import com.liu.soyaojcodesandbox.model.ExecuteCodeResponse;
import com.liu.soyaojcodesandbox.sandbox.JavaNativeCodeSandBox;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;

@SpringBootTest
class SoyaojCodeSandboxApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void test() {
        ExecuteCodeRequest request = new ExecuteCodeRequest();
        request.setInputList(new ArrayList<>());
        request.setCode("public class Main{\n" +
                "    public static void main(String[] args){\n" +
                "        int a = Integer.parseInt(args[0]);\n" +
                "        int b = Integer.parseInt(args[1]);\n" +
                "        int c = a + b;\n" +
                "        System.out.println(c);\n" +
                "    }\n" +
                "}");
        ExecuteCodeResponse executeCodeResponse = new JavaNativeCodeSandBox().execute(request);
        System.out.println(executeCodeResponse);
    }
}
