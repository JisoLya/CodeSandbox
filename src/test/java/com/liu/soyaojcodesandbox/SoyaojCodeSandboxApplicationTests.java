package com.liu.soyaojcodesandbox;

import com.liu.soyaojcodesandbox.checker.DicTreeChecker;
import com.liu.soyaojcodesandbox.checker.DictionaryTreeChecker;
import com.liu.soyaojcodesandbox.model.ExecuteCodeRequest;
import com.liu.soyaojcodesandbox.model.ExecuteCodeResponse;
import com.liu.soyaojcodesandbox.sandbox.JavaNativeCodeSandBox;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
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
                "    public static void main(String[] args) throws InterruptedException{\n" +
//                "        Thread.sleep(5*1000L);\n" +
                "        int a = Integer.parseInt(args[0]);\n" +
                "        int b = Integer.parseInt(args[1]);\n" +
                "        int c = a + b;\n" +
                "        System.out.println(c);\n" +
                "    }\n" +
                "}");
        ExecuteCodeResponse executeCodeResponse = new JavaNativeCodeSandBox().execute(request);
        System.out.println(executeCodeResponse);
    }

    @Test
    void TestAlter() {
        String code = "public class Main{\n" +
                "    public static void main(String[] args) throws InterruptedException{\n" +
//                "        Thread.sleep(5*1000L);\n" +
                "        int a = Integer.parseInt(args[0]);\n" +
                "        Files" +
                "        int b = Integer.parseInt(args[1]);\n" +
                "        int c = a + b;\n" +
                "        System.out.println(c);\n" +
                "    }\n" +
                "}";

        boolean b = DictionaryTreeChecker.checkExist(code);
        System.out.println(b);
    }
}
