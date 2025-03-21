package com.liu.soyaojcodesandbox.sandbox;

import cn.hutool.core.io.FileUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.liu.soyaojcodesandbox.checker.DictionaryTreeFilter;
import com.liu.soyaojcodesandbox.constant.FileConstant;
import com.liu.soyaojcodesandbox.model.ExecuteCodeRequest;
import com.liu.soyaojcodesandbox.model.ExecuteCodeResponse;
import com.liu.soyaojcodesandbox.model.ExecuteMessage;
import com.liu.soyaojcodesandbox.process.TaskGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class JavaDockerCodeSandBox extends CodeSandboxTemplate implements CodeSandbox {

    private static final Logger log = LoggerFactory.getLogger(JavaDockerCodeSandBox.class);
    public final DockerClient dockerClient;

    /**
     * 一个dockerSandbox的实例
     */
    private volatile static JavaDockerCodeSandBox javaDockerCodeSandBox = null;

    private final ThreadPoolExecutor threadPoolExecutor;

    private final DictionaryTreeFilter dictionaryTreeFilter;
    /**
     * 默认的容器名称
     */
    public static final String CONTAINER_NAME = "java8";
    /**
     * 利用DockerClient获取到的容器名称会有/，在判断容器是否存在的时候需要加一个
     */
    public static final String CONTAINER_NAME_WITH_SLASH = "/java8";
    /**
     * java镜像名称
     */
    public static final String JAVA_IMAGE_NAME = "dockette/jdk8";

    private JavaDockerCodeSandBox() {
        //单例模式需要私有化构造方法
        this.threadPoolExecutor = new ThreadPoolExecutor(4,
                8, 1000L * 3,
                TimeUnit.MILLISECONDS,
                //无上限的任务队列
                new LinkedBlockingDeque<>(8),
                //不能采用Abort策略
                new ThreadPoolExecutor.CallerRunsPolicy());
        this.dockerClient = DockerClientBuilder.getInstance().build();
        this.dictionaryTreeFilter = new DictionaryTreeFilter();
        if (checkExistImage(JAVA_IMAGE_NAME) == null) {
            pullImage(JAVA_IMAGE_NAME, "latest");
        }

        Container container = checkExistContainer();
        if (container == null) {
            //挂载目录
            String userHome = System.getProperty("user.home");
            String hostPath = userHome + File.separator + "CodeSandbox" + File.separator + "tempCode";
            createContainer(JAVA_IMAGE_NAME, hostPath, "/app/data");
        } else {
            log.info("容器已存在");
            //如果容器存在，但是没有启动...
            if (!"running".equals(container.getState())) {
                log.info("容器未启动");
                dockerClient.startContainerCmd(container.getId()).exec();
            }
            return;
        }
        log.info("docker 启动成功");
    }

    //单例双检
    public static JavaDockerCodeSandBox getInstance() {
        if (javaDockerCodeSandBox == null) {
            synchronized (JavaDockerCodeSandBox.class) {
                if (javaDockerCodeSandBox == null) {
                    javaDockerCodeSandBox = new JavaDockerCodeSandBox();
                }
            }
        }
        return javaDockerCodeSandBox;
    }

    /**
     * docker执行代码，返回结果
     *
     * @param request 执行代码的请求
     * @return
     */
    @Override
    public ExecuteCodeResponse execute(ExecuteCodeRequest request) {
        return super.execute(request);
    }

    @Override
    public List<ExecuteMessage> RunCode(String classDir, List<String> inputList) throws ExecutionException, InterruptedException {
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        List<Future<ExecuteMessage>> futures = new ArrayList<>();
        System.out.println(classDir);
        String[] strings = classDir.split("/");
        classDir = strings[strings.length - 1];
        for (String args : inputList) {
            //这里读取命令的时候会把整个字符串作为一个输入
            String runCmd = String.format("docker exec %s java -Xmx256m -Dfile.encoding=utf8 -cp %s %s %s", CONTAINER_NAME, classDir, FileConstant.MAIN_CLASS_NAME, args);
            futures.add(threadPoolExecutor.submit(new TaskGenerator(runCmd)));
        }
        for(Future<ExecuteMessage> f : futures) {
            ExecuteMessage executeMessage = f.get();
            executeMessageList.add(executeMessage);
        }
        return executeMessageList;
    }

    /**
     * 拉取镜像
     *
     * @param imageName 镜像名称
     * @param tag       name:tag   e.g. latest
     */
    private void pullImage(String imageName, String tag) {
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(imageName).withTag(tag);
        try {
            pullImageCmd.exec(new ResultCallback.Adapter() {
                public void onNext(PullResponseItem item) {
                    System.out.println(item.getStatus());
                }
            }).awaitCompletion(100, TimeUnit.SECONDS);
            System.out.println("镜像拉取成功");
        } catch (Exception e) {
            throw new RuntimeException("镜像拉取失败!", e);
        }
    }

    /**
     * 如果存在那么返回该容器，否则，返回null
     *
     * @return 查找到的容器
     */
    private Container checkExistContainer() {
        List<Container> containers = dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec();
        for (Container container : containers) {
            if (CONTAINER_NAME_WITH_SLASH.equals(container.getNames()[0])) {
                return container;
            }
        }
        return null;
    }

    /**
     * 检查镜像是否存在
     *
     * @param imageName 镜像的名字
     * @return 如果有那么返回该镜像
     */
    public Image checkExistImage(String imageName) {
        List<Image> images = dockerClient.listImagesCmd().withShowAll(true).exec();
        for (Image image : images) {
            String curImageName = image.getRepoTags()[0].split(":")[0];
            if (imageName.equals(curImageName)) {
                return image;
            }
        }
        return null;
    }

    /**
     * 创建容器
     *
     * @return CreateContainerResponse
     */
    private CreateContainerResponse createContainer(String imageName, String hostPath, String containerPath) {

        //将用户的代码目录和容器内目录进行挂载
        Bind bind = new Bind(hostPath, new com.github.dockerjava.api.model.Volume(containerPath));
        HostConfig hostConfig = new HostConfig().withBinds(bind);

        CreateContainerResponse containerResponse = dockerClient.createContainerCmd(imageName)
                .withHostConfig(hostConfig)
                .withName("java8")
                .withWorkingDir("/app/data")
                .withCmd("tail", "-f", "/dev/null")
                .exec();
        dockerClient.startContainerCmd(containerResponse.getId()).exec();
        return containerResponse;
    }

    private void clearUserCodeDir(String userCodeDir) {
        if (FileUtil.exist(userCodeDir)) {
            FileUtil.del(userCodeDir);
        }
    }
}
