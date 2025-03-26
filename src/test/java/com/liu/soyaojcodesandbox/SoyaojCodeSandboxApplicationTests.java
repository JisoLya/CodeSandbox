package com.liu.soyaojcodesandbox;

import cn.hutool.core.io.FileUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmd;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.liu.soyaojcodesandbox.checker.DictionaryTreeFilter;
import com.liu.soyaojcodesandbox.constant.FileConstant;
import com.liu.soyaojcodesandbox.message.RabbitMQTemplateCreator;
import com.liu.soyaojcodesandbox.model.ExecuteCodeRequest;
import com.liu.soyaojcodesandbox.model.ExecuteCodeResponse;
import com.liu.soyaojcodesandbox.model.ExecuteMessage;
import com.liu.soyaojcodesandbox.process.ProcessUtils;
import com.liu.soyaojcodesandbox.sandbox.JavaDockerCodeSandBox;
import com.liu.soyaojcodesandbox.sandbox.JavaNativeCodeSandBox;
import org.apache.http.util.Asserts;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
class SoyaojCodeSandboxApplicationTests {


    private static final Logger log = LoggerFactory.getLogger(SoyaojCodeSandboxApplicationTests.class);

    @Test
    void contextLoads() {
    }

    @Test
    void test() throws ExecutionException, InterruptedException {
        ExecuteCodeRequest request = new ExecuteCodeRequest();
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("1 1");
        request.setInputList(arrayList);
        request.setCode("public class Main{\n" +
                "    public static void main(String[] args) throws InterruptedException{\n" +
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
        long startTime = System.currentTimeMillis();
        ExecuteCodeResponse executeCodeResponse = new JavaNativeCodeSandBox().execute(request);
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
        System.out.println(executeCodeResponse);
    }

    @Test
    void TestInfiniteLoop() {
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

    @Test
    void TestCreateUserFile() {
        String code = "";
        UUID uuid = UUID.randomUUID();
        String userCodeDir = FileConstant.OUT_PUT_DIR + File.separator + uuid;
        log.info("创建用户代码目录");
        FileUtil.mkdir(userCodeDir);
        File userCodeFile = new File(userCodeDir + File.separator + FileConstant.JAVA_CLASS_NAME);
        FileUtil.writeString(code, userCodeFile, StandardCharsets.UTF_8);
    }

    @Test
    void TestCompile() {
        ExecuteCodeRequest request = new ExecuteCodeRequest();
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("1 1");
        request.setInputList(arrayList);
        request.setCode("public class Main{\n" +
                "    public static void main(String[] args) throws InterruptedException{\n" +
//                "        Thread.sleep(5*1000L);\n" +
                "        int a = Integer.parseInt(args[0]);\n" +
                "        int b = Integer.parseInt(args[1]);\n" +
                "        int c = a + b;\n" +
                "        System.out.println(c);\n" +
                "    }\n" +
                "}");
        String code = request.getCode();
        //容器化技术
        UUID uuid = UUID.randomUUID();
        //这个大概率是不存在的
        String userCodeDir = FileConstant.OUT_PUT_DIR + File.separator + uuid;
        log.info("创建用户代码目录");
        FileUtil.mkdir(userCodeDir);

        //将用户代码写入文件
        File userCodeFile = new File(userCodeDir + File.separator + FileConstant.JAVA_CLASS_NAME);
        FileUtil.writeString(code, userCodeFile, StandardCharsets.UTF_8);
        //执行编译命令
        //指定编译的错误输出为英文,避免出现乱码问题
        String compileCmd = String.format("javac -encoding utf8 -J-Duser.language=en %s", userCodeFile.getAbsolutePath());
        String[] cmds = {"/bin/bash", "-c", compileCmd};
        ExecuteMessage compileMsg = null;
        try {
            compileMsg = ProcessUtils.ExecuteCmd("compile", Runtime.getRuntime().exec(compileCmd));
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
        System.out.println(compileMsg);
    }

    @Test
    void TestCompileExisted() {
//        String cmd = "javac ~/CodeSandbox/tempCode/d8a76b1d-b0e6-444d-a5e1-cf1b2fef3ab1/Main.java";
        String cmd = "javac -encoding utf8 -J-Duser.language=en ./tempCode/d8a76b1d-b0e6-444d-a5e1-cf1b2fef3ab1/Main.java";
        System.out.println("当前工作目录: " + new File("").getAbsolutePath());
        ExecuteMessage compileMsg = null;
        try {
            compileMsg = ProcessUtils.ExecuteCmd("compile", Runtime.getRuntime().exec(cmd));
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
        System.out.println(compileMsg);
    }

    @Test
    void TestCreateContainer() {
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        CreateContainerResponse containerResponse = dockerClient.createContainerCmd("dockette/jdk8")
                .withName("java8")
                .withCmd("tail", "-f", "/dev/null")
                .exec();
        dockerClient.startContainerCmd(containerResponse.getId()).exec();
    }

    @Test
    void TestGetImageName() {
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        List<Image> images = dockerClient.listImagesCmd().withShowAll(true).exec();
        for (Image image : images) {
            String curImageName = image.getRepoTags()[0].split(":")[0];
            System.out.println(curImageName);
        }
    }

    @Test
    void TestGetInstanceAndRunCmd() {
        JavaDockerCodeSandBox instance = JavaDockerCodeSandBox.getInstance();
//        String[] command = {"/bin/echo", "hello"};

        ExecCreateCmd execCreateCmd = instance.dockerClient.execCreateCmd("java8")
                .withCmd("java", "-version")
                .withAttachStderr(true)
                .withAttachStdin(true);
        // 获取执行ID
        ExecCreateCmdResponse execCreateCmdResponse = execCreateCmd.exec();
        String execId = execCreateCmdResponse.getId();

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        // 执行命令并获取结果

        try {
            instance.dockerClient.execStartCmd(execId)
                    .exec(new ExecStartResultCallback(stdout, stderr))
                    .awaitCompletion();

            // 打印结果
            System.out.println("标准输出: " + stdout);
            System.out.println("标准错误: " + stderr);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void TestIfVolume() {
        //测试一下在tempCode中创建了用户文件之后，会不会在容器的/app/data中也创建出来
        JavaDockerCodeSandBox instance = JavaDockerCodeSandBox.getInstance();
        UUID uuid = UUID.randomUUID();
        //这个大概率是不存在的
        String userCodeDir = FileConstant.OUT_PUT_DIR + File.separator + uuid;
        log.info("创建用户代码目录");
        FileUtil.mkdir(userCodeDir);
        String code = "public class Main{\n" +
                "    public static void main(String[] args) throws InterruptedException{\n" +
                "        int a = Integer.parseInt(args[0]);\n" +
                "        int b = Integer.parseInt(args[1]);\n" +
                "        int c = a + b;\n" +
                "        System.out.println(c);\n" +
                "    }\n" +
                "}";
        //将用户代码写入文件
        File userCodeFile = new File(userCodeDir + File.separator + FileConstant.JAVA_CLASS_NAME);
        FileUtil.writeString(code, userCodeFile, StandardCharsets.UTF_8);
    }

    @Test
    void executeDocker() {
        JavaDockerCodeSandBox instance = JavaDockerCodeSandBox.getInstance();
        ExecuteCodeRequest request = new ExecuteCodeRequest();
        String code = "public class Main{\n" +
                "    public static void main(String[] args) throws InterruptedException{\n" +
                "        int a = Integer.parseInt(args[0]);\n" +
                "        int b = Integer.parseInt(args[1]);\n" +
                "        int c = a + b;\n" +
                "        System.out.println(c);\n" +
                "    }\n" +
                "}";
        ArrayList<String> inputList = new ArrayList<>();
        inputList.add("1 2");
        inputList.add("3 4");
        request.setInputList(inputList);
        request.setCode(code);
        ExecuteCodeResponse executeCodeResponse = instance.execute(request);

        System.out.println(executeCodeResponse);
    }

    @Test
    void executeDockerGetOutput() throws InterruptedException {
        JavaDockerCodeSandBox instance = JavaDockerCodeSandBox.getInstance();
        String[] runCmd = {"java", "-cp", "666482b1-c08b-4875-845c-2b305ef658e3", "Main", "1", "2"};
        ExecCreateCmd execCreateCmd = instance.dockerClient.execCreateCmd("java8")
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withWorkingDir("/app/data")
                .withCmd(runCmd);
        String execId = execCreateCmd.exec().getId();

// Capture output
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        instance.dockerClient.execStartCmd(execId)
                .exec(new ExecStartResultCallback(stdout, stderr))
                .awaitCompletion();

// Print results
        System.out.println("Output: " + stdout.toString()); // Should print "3"
        System.out.println("Errors (if any): " + stderr.toString());
    }

    @Test
    public void testQueue() {
        RabbitTemplate template = RabbitMQTemplateCreator.createTemplateForExisting("10.195.102.74", 5672, "soya", "soya");
        template.convertAndSend("submit_exchange", "submit_routKey", "test");
    }
}
