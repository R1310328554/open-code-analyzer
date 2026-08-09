package com.taobao.arthas.grpcweb.grpc;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.common.SocketUtils;
import com.taobao.arthas.core.advisor.TransformerManager;
import com.taobao.arthas.grpcweb.grpc.objectUtils.ComplexObject;
import com.taobao.arthas.grpcweb.grpc.server.GrpcServer;
import com.taobao.arthas.grpcweb.grpc.server.httpServer.NettyHttpServer;
import com.taobao.arthas.grpcweb.proxy.server.GrpcWebProxyServer;
import demo.MathGame;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandles;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

/**
 * gRPC-Web 演示程序启动入口，串联 MathGame 靶场、Arthas 探针与三类网络服务。
 * <p>
 * 启动顺序：ByteBuddy 注入 spy jar → 启动 gRPC 服务 → gRPC-Web 代理 → 静态 HTTP 页面，
 * 供浏览器通过 gRPC-Web 调用 Arthas 命令并观察增强效果。
 */
public class DemoBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

    /** gRPC-Web 代理监听端口，浏览器经此端口访问后端 gRPC */
    private int GRPC_WEB_PROXY_PORT = 8567;

    /** 原生 gRPC 服务端口，由代理转发 */
    private int GRPC_PORT = SocketUtils.findAvailableTcpPort();

    /** 静态资源 HTTP 服务端口，提供 index.html 等前端页面 */
    private int HTTP_PORT = SocketUtils.findAvailableTcpPort();

    /** Java Agent 插桩句柄，用于加载 spy 与注册 Transformer */
    private Instrumentation instrumentation;

    /** Arthas 字节码增强管理器 */
    private TransformerManager transformerManager;

    /** 后台任务线程池，执行 gRPC 侧异步逻辑 */
    private ScheduledExecutorService executorService;


    private static DemoBootstrap demoBootstrap;


    private DemoBootstrap() throws InterruptedException, IOException {
        ComplexObject ccc = createComplexObject();

        // 0. 启动 MathGame 靶场，持续产生可观测的业务调用
        Thread mathDemo = new Thread(() ->{
            MathGame game = new MathGame();
            while (true) {
                try {
                    game.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        mathDemo.start();

        // 1. 初始化 Agent：安装 ByteBuddy、挂载 spy jar、创建增强管理器与线程池
        instrumentation = ByteBuddyAgent.install();
        appendSpyJar(instrumentation);
        this.transformerManager = new TransformerManager(instrumentation);
        executorService = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                final Thread t = new Thread(r, "grpc-service-execute");
                t.setDaemon(true);
                return t;
            }
        });

        // 2. 在独立线程中启动 gRPC、gRPC-Web 代理与 HTTP 静态服务
        Thread allServerStartThread = new Thread("grpc-server-start"){
            @Override
            public void run(){
                try {
                    serverStart();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        allServerStartThread.start();
    }

    /**
     * 依次启动 gRPC 服务端、gRPC-Web 代理与 Netty HTTP 静态文件服务。
     */
    public void serverStart() throws IOException, InterruptedException {

        // 预构造复杂对象，供后续 gRPC 序列化/反序列化演示
        ComplexObject complexObject = createComplexObject();
        // 1. 启动原生 gRPC 服务（阻塞在 System.in.read 以保持进程）
        Thread grpcStartThread = new Thread(() -> {
            GrpcServer grpcServer = new GrpcServer(GRPC_PORT, instrumentation, transformerManager);
            grpcServer.start();
            try {
                System.in.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        grpcStartThread.start();

        // 2. 启动 gRPC-Web 代理，将浏览器 HTTP/1.1 请求转为 gRPC
        //this.GRPC_WEB_PROXY_PORT = SocketUtils.findAvailableTcpPort();
        Thread grpcWebProxyStartThread = new Thread(() -> {
            GrpcWebProxyServer grpcWebProxyServer = new GrpcWebProxyServer(GRPC_WEB_PROXY_PORT,GRPC_PORT);
            grpcWebProxyServer.start();
        });
        grpcWebProxyStartThread.start();

        // 3. 启动 HTTP 静态资源服务，托管前端 demo 页面
        String currentDir = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getPath();
        String STATIC_LOCATION = Paths.get(currentDir, "static").toString();
        NettyHttpServer nettyHttpServer = new NettyHttpServer(HTTP_PORT,STATIC_LOCATION);
        logger.info("start grpc server on port: {}, grpc web proxy server on port: {}, " +
                "http server server on port: {}", GRPC_PORT,GRPC_WEB_PROXY_PORT,HTTP_PORT);
        System.out.println("Open your web browser and navigate to " + "http" + "://127.0.0.1:" + HTTP_PORT + '/' + "index.html");
        nettyHttpServer.start();
    }

    /** 懒加载单例，首次调用时完成 Agent 与服务初始化 */
    public synchronized static DemoBootstrap getInstance() throws Throwable {
        if (demoBootstrap == null) {
            demoBootstrap = new DemoBootstrap();
        }
        return demoBootstrap;
    }

    /** 获取已初始化的实例，未启动时抛出 {@link IllegalStateException} */
    public static DemoBootstrap getRunningInstance() {
        if (demoBootstrap == null) {
            throw new IllegalStateException("AllServerStart must be initialized before!");
        }
        return demoBootstrap;
    }

    /** 在 gRPC 专用线程池中执行异步任务 */
    public void execute(Runnable command) {
        executorService.execute(command);
    }



    /**
     * 将 spy 模块的 class 文件打包为 jar 并加入 Bootstrap ClassLoader，
     * 使 Arthas 探针类对目标 JVM 全局可见。
     */
    public static void appendSpyJar(Instrumentation instrumentation) throws IOException {
        // 定位 spy 模块编译输出目录
        String file = DemoBootstrap.class.getProtectionDomain().getCodeSource().getLocation().getFile();

        File spyClassDir = new File(file, "../../../spy/target/classes").getAbsoluteFile();

        File destJarFile = new File(file, "../../../spy/target/test-spy.jar").getAbsoluteFile();

        ZipUtil.pack(spyClassDir, destJarFile);

        instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(destJarFile));

    }

    /**
     * 构造包含多种 Java 类型的 {@link ComplexObject} 样例，用于 gRPC 类型转换与 watch 演示。
     */
    public static ComplexObject createComplexObject() {
        // 创建一个 ComplexObject 对象
        ComplexObject complexObject = new ComplexObject();

        // 设置基本类型的值
        complexObject.setId(1);
        complexObject.setName("Complex Object");
        complexObject.setValue(3.14);

        // 设置基本类型的数组
        int[] numbers = { 1, 2, 3, 4, 5 };
        complexObject.setNumbers(numbers);

        Long[] longNumbers = {10086l,10087l,10088l,10089l,10090l,10091l};
        complexObject.setLongNumbers(longNumbers);

        // 创建并设置嵌套对象
        ComplexObject.NestedObject nestedObject = new ComplexObject.NestedObject();
        nestedObject.setNestedId(10);
        nestedObject.setNestedName("Nested Object");
        nestedObject.setFlag(true);
        complexObject.setNestedObject(nestedObject);


        List<String> stringList = new ArrayList<>();
        stringList.add("foo");
        stringList.add("bar");
        stringList.add("baz");
        complexObject.setStringList(stringList);

        Map<String, Integer> stringIntegerMap = new HashMap<>();
        stringIntegerMap.put("one", 1);
        stringIntegerMap.put("two", 2);
        complexObject.setStringIntegerMap(stringIntegerMap);

        complexObject.setDoubleArray(new Double[] { 1.0, 2.0, 3.0 });

        complexObject.setComplexArray(null);

        complexObject.setCollection(Arrays.asList("element1", "element2"));


        // 创建并设置复杂对象数组
        ComplexObject[] complexArray = new ComplexObject[2];

        ComplexObject complexObject1 = new ComplexObject();
        complexObject1.setId(2);
        complexObject1.setName("Complex Object 1");
        complexObject1.setValue(2.71);

        ComplexObject complexObject2 = new ComplexObject();
        complexObject2.setId(3);
        complexObject2.setName("Complex Object 2");
        complexObject2.setValue(1.618);

        complexArray[0] = complexObject1;
        complexArray[1] = complexObject2;

        complexObject.setComplexArray(complexArray);

        // 创建并设置多维数组
        int[][] multiDimensionalArray = { { 1, 2, 3 }, { 4, 5, 6 } };
        complexObject.setMultiDimensionalArray(multiDimensionalArray);

        // 设置数组中的基本元素数组
        String[] stringArray = { "Hello", "World" };
        complexObject.setStringArray(stringArray);

        // 输出 ComplexObject 对象的信息
        System.out.println(complexObject);

        return complexObject;
    }

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public TransformerManager getTransformerManager() {
        return transformerManager;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return this.executorService;
    }
    public static void main(String[] args) throws Throwable {
        DemoBootstrap.getInstance();
    }
}
