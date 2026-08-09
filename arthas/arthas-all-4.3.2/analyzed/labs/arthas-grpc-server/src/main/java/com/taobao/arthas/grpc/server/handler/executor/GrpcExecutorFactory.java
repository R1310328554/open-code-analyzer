package com.taobao.arthas.grpc.server.handler.executor;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.grpc.server.handler.GrpcDispatcher;
import com.taobao.arthas.grpc.server.handler.constant.GrpcInvokeTypeEnum;
import com.taobao.arthas.grpc.server.utils.ReflectUtil;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * gRPC 执行器工厂，启动时扫描 executor 包并注册各调用类型的实现。
 * <p>
 * 通过反射实例化 {@link AbstractGrpcExecutor} 子类，按 {@link GrpcInvokeTypeEnum}
 * 建立类型到执行器的映射，供 HTTP/2 处理器按 RPC 语义路由。
 *
 * @author: FengYe
 * @date: 2024/10/24 01:56
 * @description: GrpcExecutorFactory
 */
public class GrpcExecutorFactory {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

    /** 执行器实现类所在包名，供 {@link ReflectUtil#findClasses(String)} 扫描 */
    public static final String DEFAULT_GRPC_EXECUTOR_PACKAGE_NAME = "com.taobao.arthas.grpc.server.handler.executor";

    /** 调用类型 -> 执行器实例 */
    private final Map<GrpcInvokeTypeEnum, GrpcExecutor> map = new HashMap<>();

    /**
     * 扫描并加载 executor 包下所有 {@link GrpcExecutor} 实现，注入 dispatcher 后注册。
     *
     * @param dispatcher 与业务服务绑定的 gRPC 调度器
     */
    public void loadExecutor(GrpcDispatcher dispatcher) {
        List<Class<?>> classes = ReflectUtil.findClasses(DEFAULT_GRPC_EXECUTOR_PACKAGE_NAME);
        for (Class<?> clazz : classes) {
            if (GrpcExecutor.class.isAssignableFrom(clazz)) {
                try {
                    // 跳过接口与抽象基类，只实例化具体执行器
                    if (AbstractGrpcExecutor.class.equals(clazz) || GrpcExecutor.class.equals(clazz)) {
                        continue;
                    }
                    if (AbstractGrpcExecutor.class.isAssignableFrom(clazz)) {
                        Constructor<?> constructor = clazz.getConstructor(GrpcDispatcher.class);
                        GrpcExecutor executor = (GrpcExecutor) constructor.newInstance(dispatcher);
                        map.put(executor.supportGrpcType(), executor);
                    } else {
                        Constructor<?> constructor = clazz.getConstructor();
                        GrpcExecutor executor = (GrpcExecutor) constructor.newInstance();
                        map.put(executor.supportGrpcType(), executor);
                    }
                } catch (Exception e) {
                    logger.error("GrpcExecutorFactory loadExecutor error", e);
                }
            }
        }
    }

    /**
     * 按 gRPC 调用类型获取已注册的执行器。
     *
     * @param grpcType 一元、服务端流、客户端流或双向流
     * @return 对应执行器，未注册时返回 null
     */
    public GrpcExecutor getExecutor(GrpcInvokeTypeEnum grpcType) {
        return map.get(grpcType);
    }
}
