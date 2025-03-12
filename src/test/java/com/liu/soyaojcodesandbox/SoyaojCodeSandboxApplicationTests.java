package com.liu.soyaojcodesandbox;

import com.liu.soyaojcodesandbox.checker.DictionaryTreeChecker;
import com.liu.soyaojcodesandbox.model.ExecuteCodeRequest;
import com.liu.soyaojcodesandbox.model.ExecuteCodeResponse;
import com.liu.soyaojcodesandbox.sandbox.JavaNativeCodeSandBox;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@SpringBootTest
class SoyaojCodeSandboxApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void test() throws ExecutionException, InterruptedException {
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

    @Test
    void TestManyData() throws ExecutionException, InterruptedException {
        ArrayList<String> inputList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            inputList.add("1 " + i);
        }
        ExecuteCodeRequest request = new ExecuteCodeRequest();
        request.setInputList(inputList);
        request.setCode("public class Main{\n" +
                "    public static void main(String[] args) throws InterruptedException{\n" +
                "        int a = Integer.parseInt(args[0]);\n" +
                "        int b = Integer.parseInt(args[1]);\n" +
                "        int c = a + b;\n" +
                "        System.out.println(c);\n" +
                "    }\n" +
                "}"
        );
        ExecuteCodeResponse executeCodeResponse = new JavaNativeCodeSandBox().execute(request);
        System.out.println(executeCodeResponse);
    }

    @Test
    void TestInfiniteLoop() throws ExecutionException, InterruptedException {
        ExecuteCodeRequest request = new ExecuteCodeRequest();
        ArrayList<String> inputList = new ArrayList<>();
        inputList.add("1 2");
        inputList.add("1 3");
        request.setInputList(inputList);
        request.setCode(
                "public class Main{" +
                        "public static void main(String[] args) {" +
                        "while(true){" +
                        "}}" +
                        "}"
        );
        ExecuteCodeResponse executeCodeResponse = new JavaNativeCodeSandBox().execute(request);
        System.out.println(executeCodeResponse);
    }
}
