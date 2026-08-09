package com.taobao.arthas.grpcweb.grpc.server;

import arthas.VmTool;
import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.common.SocketUtils;
import com.taobao.arthas.core.advisor.TransformerManager;
import com.taobao.arthas.grpcweb.grpc.service.*;
import com.taobao.arthas.grpcweb.grpc.view.GrpcResultViewResolver;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandles;

/**
 * Arthas gRPC 服务启动器，注册 Object、Pwd、SystemProperty、Watch 等命令服务。
 * <p>
 * 使用 attach 传入的 {@link Instrumentation} 与 {@link TransformerManager} 支撑字节码增强；
 * 端口为 0 时自动选取可用 TCP 端口。
 */
public class GrpcServer {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

    /** 监听端口，0 表示自动分配 */
    private int port;

    private Server grpcServer;

    private Instrumentation instrumentation;

    private TransformerManager transformerManager;

    /**
     * @param port                监听端口，0 则调用 {@link SocketUtils#findAvailableTcpPort()}
     * @param instrumentation     JVM 插桩接口
     * @param transformerManager  ClassFileTransformer 生命周期管理
     */
    public GrpcServer(int port, Instrumentation instrumentation, TransformerManager transformerManager) {
        if (port == 0) {
            this.port = SocketUtils.findAvailableTcpPort();
        } else {
            this.port = port;
        }
        this.instrumentation = instrumentation;
        this.transformerManager = transformerManager;
    }

    /**
     * 构建并启动 gRPC Server，注册各命令 Service 并添加 JVM 关闭钩子。
     */
    public void start() {
        GrpcResultViewResolver grpcResultViewResolver = new GrpcResultViewResolver();
        GrpcJobController grpcJobController = new GrpcJobController(this.instrumentation, this.transformerManager, grpcResultViewResolver);
        // VmTool 本地库目录与 arthas-core jar 同级
        File path = new File(VmTool.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
        String libPath = path.getAbsolutePath();

        try {
            grpcServer = ServerBuilder.forPort(port)
                    .addService(new ObjectService(grpcJobController,libPath))
                    .addService(new PwdCommandService(grpcJobController))
                    .addService(new SystemPropertyCommandService(grpcJobController))
                    .addService(new WatchCommandService(grpcJobController))
                    .build()
                    .start();
            logger.info("Server started, listening on " + port);
            Runtime.getRuntime().addShutdownHook(new Thread("grpc-server-shutdown") {
                @Override
                public void run() {
                    if (grpcServer != null) {
                        grpcServer.shutdown();
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
