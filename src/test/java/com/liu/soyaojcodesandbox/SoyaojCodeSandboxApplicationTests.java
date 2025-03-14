package com.liu.soyaojcodesandbox;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.liu.soyaojcodesandbox.checker.DictionaryTreeFilter;
import com.liu.soyaojcodesandbox.model.ExecuteCodeRequest;
import com.liu.soyaojcodesandbox.model.ExecuteCodeResponse;
import com.liu.soyaojcodesandbox.sandbox.JavaNativeCodeSandBox;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        DictionaryTreeFilter dictionaryTreeFilter = new DictionaryTreeFilter();
        boolean b = dictionaryTreeFilter.checkExist(code);
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

    @Test
    void TestParser() {
        String code = "public class Main{\n" +
                "    public static void main(String[] args) throws InterruptedException{\n" +
                "        int a = Integer.parseInt(args[0]);\n" +
                "        int b = Integer.parseInt(args[1]);\n" +
                "        int c = a + b;\n" +
                "        System.out.println(c);\n" +
                "    }\n" +
                "}";

        String[] words = code.split("\\s+");
        ArrayList<String> tokens = new ArrayList<>();
        // 正则表达式：匹配单词或单个非单词字符
        Pattern pattern = Pattern.compile("([a-zA-Z0-9_]+|[^\\w\\s])");
        for (String word : words) {
            Matcher matcher = pattern.matcher(word);
            while (matcher.find()) {
                tokens.add(matcher.group());
            }
        }
        System.out.println(tokens);
    }

    @Test
    void checkBloomFilter() throws ExecutionException, InterruptedException {
        ExecuteCodeRequest request = new ExecuteCodeRequest();
        ArrayList<String> inputList = new ArrayList<>();
        inputList.add("1 2");
        inputList.add("1 3");
        request.setInputList(inputList);
        request.setCode("import java.io.IOException;\n" +
                "\n" +
                "public class Main {\n" +
                "    public static void main(String[] args) throws IOException {\n" +
                "        String cmd = \"echo hello\";\n" +
                "        Runtime.getRuntime().exec(cmd);\n" +
                "    }\n" +
                "}");
        ExecuteCodeResponse executeCodeResponse = new JavaNativeCodeSandBox().execute(request);
        System.out.println(executeCodeResponse);
    }

    @Test
    void TestJavaParser() {
        String code = "import java.io.IOException;\n" +
                "\n" +
                "public class Main {\n" +
                "    public static void main(String[] args) throws IOException {\n" +
                "        String cmd = \"echo hello\";\n" +
                "        Runtime.getRuntime().exec(cmd);\n" +
                "    }\n" +
                "}";
        List<String> tokens = new ArrayList<>();
        CompilationUnit unit = StaticJavaParser.parse(code);
        unit.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(SimpleName n, Void arg) {
                tokens.add(n.getIdentifier());
                System.out.println("identifier = " + n.getIdentifier());
                super.visit(n, arg);
            }

//            @Override
//            public void visit(Name n, Void arg) {
//                tokens.add(n.asString());
//                System.out.println("n.asString = " + n.asString());
//                super.visit(n, arg);
//            }

//            @Override
//            public void visit(StringLiteralExpr n, Void arg) {
//                tokens.add(n.getValue());
//                System.out.println("value = " + n.getValue());
//                super.visit(n, arg);
//            }
        }, null);

        System.out.println("通过 AST 提取的 token:");
        for (int i = 0; i < tokens.size(); i++) {
            System.out.println(i + ": " + tokens.get(i));
        }
    }
}
