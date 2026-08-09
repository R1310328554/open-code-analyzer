package com.taobao.arthas.grpcweb.grpc.service;

import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandles;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.taobao.arthas.core.command.express.Express;
import com.taobao.arthas.core.command.express.ExpressException;
import com.taobao.arthas.core.command.express.ExpressFactory;
import com.taobao.arthas.core.util.Constants;
import com.taobao.arthas.grpcweb.grpc.objectUtils.JavaObjectConverter;
import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.common.VmToolUtils;

import arthas.VmTool;
import com.taobao.arthas.grpcweb.grpc.observer.ArthasStreamObserver;
import com.taobao.arthas.grpcweb.grpc.observer.impl.ArthasStreamObserverImpl;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.arthas.api.ObjectServiceGrpc.ObjectServiceImplBase;
import io.arthas.api.ArthasServices.JavaObject;
import io.arthas.api.ArthasServices.ObjectQuery;
import io.arthas.api.ArthasServices.ObjectQueryResult;
import io.arthas.api.ArthasServices.ObjectQueryResult.Builder;

/**
 * gRPC 堆对象查询服务：通过 {@link VmTool} 获取类实例，可选 OGNL 表达式过滤，
 * 再经 {@link JavaObjectConverter} 序列化为 {@link JavaObject} 返回。
 */
public class ObjectService extends ObjectServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

    private VmTool vmTool;
    private Instrumentation inst;

    private GrpcJobController grpcJobController;

    /**
     * 初始化 VmTool 本地库并保存 Instrumentation 引用。
     *
     * @param grpcJobController 任务控制器，提供 Instrumentation
     * @param libDir            arthas 本地库目录（与 core jar 同级）
     */
    public ObjectService(GrpcJobController grpcJobController, String libDir) {
        this.inst = grpcJobController.getInstrumentation();
        this.grpcJobController = grpcJobController;

        try {
            String detectLibName = VmToolUtils.detectLibName();
            String vmToolLibPath = Paths.get(libDir, detectLibName).toString();

            vmTool = VmTool.getInstance(vmToolLibPath);
        } catch (Throwable e) {
            logger.error("init vmtool error", e);
        }
    }

    /**
     * 按类名（及可选 ClassLoader 条件）查找实例，执行 express/resultExpress 后返回序列化对象。
     */
    @Override
    public void query(ObjectQuery query, StreamObserver<ObjectQueryResult> responseObserver) {
        if (vmTool == null) {
            throw Status.UNAVAILABLE.withDescription("vmtool can not work").asRuntimeException();
        }
        ArthasStreamObserver<ObjectQueryResult> arthasStreamObserver = new ArthasStreamObserverImpl<>(responseObserver, null,grpcJobController);
        String className = query.getClassName();
        String classLoaderHash = query.getClassLoaderHash();
        String classLoaderClass = query.getClassLoaderClass();
        int limit = query.getLimit();
        int depth = query.getDepth();
        String express = query.getExpress();
        String resultExpress = query.getResultExpress();

        // 仅指定类名时，JVM 中可能存在多个同名类，需全部扫描
        if (isEmpty(classLoaderHash) && isEmpty(classLoaderClass)) {
            List<Class<?>> foundClassList = new ArrayList<>();
            for (Class<?> clazz : inst.getAllLoadedClasses()) {
                if (clazz.getName().equals(className)) {
                    foundClassList.add(clazz);
                }
            }

            if (foundClassList.size() == 0) {
                arthasStreamObserver.onNext(ObjectQueryResult.newBuilder().setSuccess(false)
                        .setMessage("can not find class: " + className).build());
                arthasStreamObserver.onCompleted();
                return;
            } else if (foundClassList.size() > 1) {
                String message = "found more than one class: " + className;
                arthasStreamObserver.onNext(ObjectQueryResult.newBuilder().setSuccess(false).setMessage(message).build());
                arthasStreamObserver.onCompleted();
                return;
            } else {
                Object[] instances = vmTool.getInstances(foundClassList.get(0), limit);
                Builder builder = ObjectQueryResult.newBuilder().setSuccess(true);
                Object value = null;
                if (!isEmpty(express)) {
                    Express unpooledExpress = ExpressFactory.unpooledExpress(foundClassList.get(0).getClassLoader());
                    try {
                        value = unpooledExpress.bind(new InstancesWrapper(instances)).get(express);
                    } catch (ExpressException e) {
                        logger.warn("ognl: failed execute express: " + express, e);
                    }
                }
                if(value != null && !isEmpty(resultExpress)){
                    try {
                        value = ExpressFactory.threadLocalExpress(value).bind(Constants.COST_VARIABLE, 0.0).get(resultExpress);
                    } catch (ExpressException e) {
                        logger.warn("ognl: failed execute result express: " + express, e);
                    }
                }
                JavaObject javaObject = JavaObjectConverter.toJavaObjectWithExpand(value, depth);
                builder.addObjects(javaObject);
                arthasStreamObserver.onNext(builder.build());
                arthasStreamObserver.onCompleted();
                return;
            }
        }

        // 指定了 classLoader hash 或 classLoader 类名，精确匹配唯一 Class
        Class<?> foundClass = null;

        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (!clazz.getName().equals(className)) {
                continue;
            }

            ClassLoader classLoader = clazz.getClassLoader();

            if (classLoader == null) {
                continue;
            }

            if (!isEmpty(classLoaderHash)) {
                String hex = Integer.toHexString(classLoader.hashCode());
                if (classLoaderHash.equals(hex)) {
                    foundClass = clazz;
                    break;
                }
            }

            if (!isEmpty(classLoaderClass) && classLoaderClass.equals(classLoader.getClass().getName())) {
                foundClass = clazz;
                break;
            }
        }
        if (foundClass == null) {
            arthasStreamObserver.onNext(ObjectQueryResult.newBuilder().setSuccess(false)
                    .setMessage("can not find class: " + className).build());
            arthasStreamObserver.onCompleted();
            return;
        }

        Object[] instances = vmTool.getInstances(foundClass, limit);
        Builder builder = ObjectQueryResult.newBuilder().setSuccess(true);
//        for (Object obj : instances) {
//            JavaObject javaObject = JavaObjectConverter.toJavaObjectWithExpand(obj, depth);
//            builder.addObjects(javaObject);
//        }

        Object value = null;
        if (!isEmpty(express)) {
            Express unpooledExpress = ExpressFactory.unpooledExpress(foundClass.getClassLoader());
            try {
                value = unpooledExpress.bind(new InstancesWrapper(instances)).get(express);
            } catch (ExpressException e) {
                logger.warn("ognl: failed execute express: " + express, e);
            }
        }
        if(value != null && !isEmpty(resultExpress)){
            try {
                value = ExpressFactory.threadLocalExpress(value).bind(Constants.COST_VARIABLE, 0.0).get(resultExpress);
            } catch (ExpressException e) {
                logger.warn("ognl: failed execute result express: " + express, e);
            }
        }
        JavaObject javaObject = JavaObjectConverter.toJavaObjectWithExpand(value, depth);
        builder.addObjects(javaObject);
        arthasStreamObserver.onNext(builder.build());
        arthasStreamObserver.onCompleted();
    }

    /** 判断字符串是否为 null 或空串 */
    public static boolean isEmpty(Object str) {
        return str == null || "".equals(str);
    }

    /** OGNL 绑定用包装类，将实例数组暴露为 {@code instances} 变量 */
    static class InstancesWrapper {
        Object instances;

        public InstancesWrapper(Object instances) {
            this.instances = instances;
        }

        public Object getInstances() {
            return instances;
        }

        public void setInstances(Object instances) {
            this.instances = instances;
        }
    }

}
