package com.taobao.arthas.grpc.server.handler;


import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.grpc.server.handler.annotation.GrpcMethod;
import com.taobao.arthas.grpc.server.handler.annotation.GrpcService;
import com.taobao.arthas.grpc.server.handler.constant.GrpcInvokeTypeEnum;
import com.taobao.arthas.grpc.server.utils.ReflectUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * gRPC 方法路由与反射调用调度器。
 * <p>
 * 启动时扫描 {@code @GrpcService} 类，为每个 {@code @GrpcMethod} 绑定
 * {@link MethodHandle} 及 Protobuf parseFrom/toByteArray 句柄，
 * 运行时按 service.method 键分发 unary/流式调用。
 *
 * @author: FengYe
 * @date: 2024/9/6 01:12
 * @description: GrpcDelegrate
 */
public class GrpcDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

    /** 未指定包名时的默认服务实现扫描路径。 */
    public static final String DEFAULT_GRPC_SERVICE_PACKAGE_NAME = "com.taobao.arthas.grpc.server.service.impl";

    /** service.method -> 已绑定实例的 gRPC 方法句柄。 */
    public static Map<String, MethodHandle> grpcInvokeMap = new HashMap<>();

//    public static Map<String, StreamObserver> clientStreamInvokeMap = new HashMap<>();

    public static Map<String, MethodHandle> requestParseFromMap = new HashMap<>();

    public static Map<String, MethodHandle> requestToByteArrayMap = new HashMap<>();

    public static Map<String, MethodHandle> responseParseFromMap = new HashMap<>();

    public static Map<String, MethodHandle> responseToByteArrayMap = new HashMap<>();

    public static Map<String, GrpcInvokeTypeEnum> grpcInvokeTypeMap = new HashMap<>();

    /**
     * 扫描包内 {@code @GrpcService} 类，注册方法句柄与 Protobuf 序列化句柄。
     *
     * @param grpcServicePackageName 扫描包名，null 时用 {@link #DEFAULT_GRPC_SERVICE_PACKAGE_NAME}
     */
    public void loadGrpcService(String grpcServicePackageName) {
        List<Class<?>> classes = ReflectUtil.findClasses(Optional.ofNullable(grpcServicePackageName).orElse(DEFAULT_GRPC_SERVICE_PACKAGE_NAME));
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(GrpcService.class)) {
                try {
                    // 处理 service
                    GrpcService grpcService = clazz.getAnnotation(GrpcService.class);
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    // 处理 method
                    MethodHandles.Lookup lookup = MethodHandles.lookup();
                    Method[] declaredMethods = clazz.getDeclaredMethods();
                    for (Method method : declaredMethods) {
                        if (method.isAnnotationPresent(GrpcMethod.class)) {
                            GrpcMethod grpcMethod = method.getAnnotation(GrpcMethod.class);
                            MethodHandle grpcInvoke = lookup.unreflect(method);
                            String grpcMethodKey = generateGrpcMethodKey(grpcService.value(), grpcMethod.value());
                            grpcInvokeTypeMap.put(grpcMethodKey, grpcMethod.grpcType());
                            grpcInvokeMap.put(grpcMethodKey, grpcInvoke.bindTo(instance));


                            Class<?> requestClass = null;
                            Class<?> responseClass = null;
                            if (GrpcInvokeTypeEnum.UNARY.equals(grpcMethod.grpcType())) {
                                requestClass = grpcInvoke.type().parameterType(1);
                                responseClass = grpcInvoke.type().returnType();
                            } else if (GrpcInvokeTypeEnum.CLIENT_STREAM.equals(grpcMethod.grpcType()) || GrpcInvokeTypeEnum.BI_STREAM.equals(grpcMethod.grpcType())) {
                                responseClass = getInnerGenericClass(method.getGenericParameterTypes()[0]);
                                requestClass = getInnerGenericClass(method.getGenericReturnType());
                            } else if (GrpcInvokeTypeEnum.SERVER_STREAM.equals(grpcMethod.grpcType())) {
                                requestClass = getInnerGenericClass(method.getGenericParameterTypes()[0]);
                                responseClass = getInnerGenericClass(method.getGenericParameterTypes()[1]);
                            }
                            MethodHandle requestParseFrom = lookup.findStatic(requestClass, "parseFrom", MethodType.methodType(requestClass, byte[].class));
                            MethodHandle responseParseFrom = lookup.findStatic(responseClass, "parseFrom", MethodType.methodType(responseClass, byte[].class));
                            MethodHandle requestToByteArray = lookup.findVirtual(requestClass, "toByteArray", MethodType.methodType(byte[].class));
                            MethodHandle responseToByteArray = lookup.findVirtual(responseClass, "toByteArray", MethodType.methodType(byte[].class));
                            requestParseFromMap.put(grpcMethodKey, requestParseFrom);
                            responseParseFromMap.put(grpcMethodKey, responseParseFrom);
                            requestToByteArrayMap.put(grpcMethodKey, requestToByteArray);
                            responseToByteArrayMap.put(grpcMethodKey, responseToByteArray);


//                            switch (grpcMethod.grpcType()) {
//                                case UNARY:
//                                    unaryInvokeMap.put(grpcMethodKey, grpcInvoke.bindTo(instance));
//                                    return;
//                                case CLIENT_STREAM:
//                                    Object invoke = grpcInvoke.bindTo(instance).invoke();
//                                    if (!(invoke instanceof StreamObserver)) {
//                                        throw new RuntimeException(grpcMethodKey + " return class is not StreamObserver!");
//                                    }
//                                    clientStreamInvokeMap.put(grpcMethodKey, (StreamObserver) invoke);
//                                    return;
//                                case SERVER_STREAM:
//                                    return;
//                                case BI_STREAM:
//                                    return;
//                            }
                        }
                    }
                } catch (Throwable e) {
                    logger.error("GrpcDispatcher loadGrpcService error.", e);
                }
            }
        }
    }

    /** 按 service/method 名执行 unary 调用（原始字节入参）。 */
    public GrpcResponse doUnaryExecute(String service, String method, byte[] arg) throws Throwable {
        MethodHandle methodHandle = grpcInvokeMap.get(generateGrpcMethodKey(service, method));
        MethodType type = grpcInvokeMap.get(generateGrpcMethodKey(service, method)).type();
        Object req = requestParseFromMap.get(generateGrpcMethodKey(service, method)).invoke(arg);
        Object execute = methodHandle.invoke(req);
        GrpcResponse grpcResponse = new GrpcResponse();
        grpcResponse.setClazz(type.returnType());
        grpcResponse.setService(service);
        grpcResponse.setMethod(method);
        grpcResponse.writeResponseData(execute);
        return grpcResponse;
    }

    /** 对 {@link GrpcRequest} 执行 unary 调用并封装 {@link GrpcResponse}。 */
    public GrpcResponse unaryExecute(GrpcRequest request) throws Throwable {
        MethodHandle methodHandle = grpcInvokeMap.get(request.getGrpcMethodKey());
        MethodType type = grpcInvokeMap.get(request.getGrpcMethodKey()).type();
        Object req = requestParseFromMap.get(request.getGrpcMethodKey()).invoke(request.readData());
        Object execute = methodHandle.invoke(req);
        GrpcResponse grpcResponse = new GrpcResponse();
        grpcResponse.setClazz(type.returnType());
        grpcResponse.setService(request.getService());
        grpcResponse.setMethod(request.getMethod());
        grpcResponse.writeResponseData(execute);
        return grpcResponse;
    }

    /** 客户端流式：返回用于接收后续请求的 {@link StreamObserver}。 */
    public StreamObserver<GrpcRequest> clientStreamExecute(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) throws Throwable {
        MethodHandle methodHandle = grpcInvokeMap.get(request.getGrpcMethodKey());
        return (StreamObserver<GrpcRequest>) methodHandle.invoke(responseObserver);
    }

    /** 服务端流式：解析首包请求后通过 responseObserver 推送多条响应。 */
    public void serverStreamExecute(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) throws Throwable {
        MethodHandle methodHandle = grpcInvokeMap.get(request.getGrpcMethodKey());
        Object req = requestParseFromMap.get(request.getGrpcMethodKey()).invoke(request.readData());
        methodHandle.invoke(req, responseObserver);
    }

    /** 双向流式：返回客户端请求侧 StreamObserver。 */
    public StreamObserver<GrpcRequest> biStreamExecute(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) throws Throwable {
        MethodHandle methodHandle = grpcInvokeMap.get(request.getGrpcMethodKey());
        return (StreamObserver<GrpcRequest>) methodHandle.invoke(responseObserver);
    }

    /**
     * 获取指定 service method 对应的入参类型
     *
     * @param serviceName gRPC 服务名
     * @param methodName 方法名
     * @return 请求消息 Protobuf 类型
     */
    public static Class<?> getRequestClass(String serviceName, String methodName) {
        //protobuf 规范只能有单入参
        return Optional.ofNullable(grpcInvokeMap.get(generateGrpcMethodKey(serviceName, methodName))).orElseThrow(() -> new RuntimeException("The specified grpc method does not exist")).type().parameterArray()[0];
    }

    /** 生成 {@code serviceName.methodName} 路由键。 */
    public static String generateGrpcMethodKey(String serviceName, String methodName) {
        return serviceName + "." + methodName;
    }

    /** 根据注册表填充请求的 {@link GrpcInvokeTypeEnum} 并标记为首包数据。 */
    public static void checkGrpcType(GrpcRequest request) {
        request.setGrpcType(
                Optional.ofNullable(grpcInvokeTypeMap.get(generateGrpcMethodKey(request.getService(), request.getMethod())))
                        .orElse(GrpcInvokeTypeEnum.UNARY)
        );
        request.setStreamFirstData(true);
    }

    /** 递归解析泛型参数，获取 StreamObserver 内层 Protobuf 消息类型。 */
    public static Class<?> getInnerGenericClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            Type[] actualTypeArguments = paramType.getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                Type innerType = actualTypeArguments[0]; // 获取第一个实际类型参数
                if (innerType instanceof ParameterizedType) {
                    return getInnerGenericClass(innerType); // 递归调用获取最内层类型
                } else if (innerType instanceof Class) {
                    return (Class<?>) innerType; // 直接返回 Class 类型
                }
            }
        }
        return null; // 如果没有找到对应的类型
    }
}
