/*
 * Copyright 2020  Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.taobao.arthas.grpcweb.proxy;

import com.google.common.annotations.VisibleForTesting;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * gRPC-Web 消息与 Content-Type 工具类。
 *
 * <p>校验 HTTP {@code Content-Type}、映射 gRPC-Web 变体，并将请求体字节反序列化为 RPC 入参 protobuf。</p>
 */
public class MessageUtils {
    /** gRPC-Web 二进制或文本（Base64）传输模式 */
    @VisibleForTesting
    public
    enum ContentType {
        GRPC_WEB_BINARY, GRPC_WEB_TEXT;
    }

    /** 标准 gRPC-Web MIME 类型到内部枚举的映射 */
    private static Map<String, ContentType> GRPC_GCP_CONTENT_TYPES = new HashMap<String, ContentType>() {
        {
            put("application/grpc-web", ContentType.GRPC_WEB_BINARY);
            put("application/grpc-web+proto", ContentType.GRPC_WEB_BINARY);
            put("application/grpc-web-text", ContentType.GRPC_WEB_TEXT);
            put("application/grpc-web-text+proto", ContentType.GRPC_WEB_TEXT);
        }
    };

    /**
     * 校验请求 Content-Type 是否为支持的 gRPC-Web 类型。
     *
     * @param contentType HTTP Content-Type 头值
     * @return 对应的 {@link ContentType}
     * @throws IllegalArgumentException 类型缺失或不支持时
     */
    public static ContentType validateContentType(String contentType) throws IllegalArgumentException {
        if (contentType == null || !GRPC_GCP_CONTENT_TYPES.containsKey(contentType)) {
            throw new IllegalArgumentException("This content type is not used for grpc-web: " + contentType);
        }
        return getContentType(contentType);
    }

    /** 按 MIME 字符串查找 ContentType，调用方需保证 key 已注册。 */
    static ContentType getContentType(String type) {
        return GRPC_GCP_CONTENT_TYPES.get(type);
    }

    /**
     * 根据 RPC 方法第一个参数类型，将字节数组反序列化为 protobuf 入参对象。
     *
     * @param rpcMethod Stub 上的 RPC 方法（反射）
     * @param in 解帧后的 protobuf 字节
     * @return {@code parseFrom(byte[])} 得到的入参实例
     */
    static Object getInputProtobufObj(Method rpcMethod, byte[] in) {
        Class[] inputArgs = rpcMethod.getParameterTypes();
        Class inputArgClass = inputArgs[0];

        // 通过 generated message 的 parseFrom(byte[]) 反序列化
        Method parseFromObj;
        try {
            parseFromObj = inputArgClass.getMethod("parseFrom", byte[].class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Couldn't find method in 'parseFrom' in " + inputArgClass.getName());
        }

        Object inputObj;
        try {
            inputObj = parseFromObj.invoke(null, in);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }

        if (inputObj == null || !inputArgClass.isInstance(inputObj)) {
            throw new IllegalArgumentException("Input obj is **not** instance of the correct input class type");
        }
        return inputObj;
    }
}
