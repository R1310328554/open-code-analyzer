/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.remote.client.grpc;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.grpc.auto.Metadata;
import com.alibaba.nacos.api.grpc.auto.Payload;
import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.api.utils.NetUtils;
import com.alibaba.nacos.common.remote.PayloadRegistry;
import com.alibaba.nacos.common.remote.exception.RemoteException;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.UnsafeByteOperations;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * gRPC 载荷工具：在 {@link Request}/{@link Response} 与 Protobuf {@link Payload} 间转换，请求体经 Jackson 序列化为 JSON 写入 {@link Any}，类型名写入 {@link Metadata}。
 * gRPC utils, use to parse request and response.
 *
 * @author liuzunfei
 * @version $Id: GrpcUtils.java, v 0.1 2020年08月09日 1:43 PM liuzunfei Exp $
 */
public class GrpcUtils {
    
    /**
     * convert request to payload.
     *
     * @param request request.
     * @param meta    request meta.
     * @return payload.
      * <p>gRPC 载荷编解码工具；详见类级说明。</p>
     */
    public static Payload convert(Request request, RequestMeta meta) {
        // 组装元数据：类型名、客户端 IP、自定义 Header
        Payload.Builder payloadBuilder = Payload.newBuilder();
        Metadata.Builder metaBuilder = Metadata.newBuilder();
        if (meta != null) {
            metaBuilder.putAllHeaders(request.getHeaders())
                .setType(request.getClass().getSimpleName());
        }
        metaBuilder.setClientIp(NetUtils.localIp());
        payloadBuilder.setMetadata(metaBuilder.build());
        
        // 请求体 JSON 序列化后写入 Payload.body
        byte[] jsonBytes = convertRequestToByte(request);
        return payloadBuilder
            .setBody(Any.newBuilder().setValue(UnsafeByteOperations.unsafeWrap(jsonBytes))).build();
        
    }
    
    /**
     * convert request to payload.
     *
     * @param request request.
     * @return payload.
      * <p>gRPC 载荷编解码工具；详见类级说明。</p>
     */
    public static Payload convert(Request request) {
        
        Metadata newMeta = Metadata.newBuilder().setType(request.getClass().getSimpleName())
            .setClientIp(NetUtils.localIp()).putAllHeaders(request.getHeaders()).build();
        
        byte[] jsonBytes = convertRequestToByte(request);
        
        Payload.Builder builder = Payload.newBuilder();
        
        return builder
            .setBody(Any.newBuilder().setValue(UnsafeByteOperations.unsafeWrap(jsonBytes)))
            .setMetadata(newMeta).build();
        
    }
    
    /**
     * convert response to payload.
     *
     * @param response response.
     * @return payload.
      * <p>gRPC 载荷编解码工具；详见类级说明。</p>
     */
    public static Payload convert(Response response) {
        byte[] jsonBytes = JacksonUtils.toJsonBytes(response);
        
        Metadata.Builder metaBuilder =
            Metadata.newBuilder().setType(response.getClass().getSimpleName());
        return Payload.newBuilder()
            .setBody(Any.newBuilder().setValue(UnsafeByteOperations.unsafeWrap(jsonBytes)))
            .setMetadata(metaBuilder.build()).build();
    }
    
    private static byte[] convertRequestToByte(Request request) {
        Map<String, String> requestHeaders = new HashMap<>(request.getHeaders());
        request.clearHeaders();
        byte[] jsonBytes = JacksonUtils.toJsonBytes(request);
        request.putAllHeader(requestHeaders);
        return jsonBytes;
    }
    
    /**
     * 按 {@link Metadata#getType()} 从 {@link PayloadRegistry} 反查类并反序列化；未知类型抛出 {@link RemoteException}。
     * parse payload to request/response model.
     *
     * @param payload payload to be parsed.
     * @return payload
     */
    public static Object parse(Payload payload) {
        Class classType = PayloadRegistry.getClassByType(payload.getMetadata().getType());
        if (classType != null) {
            ByteString byteString = payload.getBody().getValue();
            ByteBuffer byteBuffer = byteString.asReadOnlyByteBuffer();
            Object obj = JacksonUtils.toObj(new ByteBufferBackedInputStream(byteBuffer), classType);
            if (obj instanceof Request) {
                ((Request) obj).putAllHeader(payload.getMetadata().getHeadersMap());
            }
            return obj;
        } else {
            throw new RemoteException(NacosException.SERVER_ERROR,
                "Unknown payload type:" + payload.getMetadata().getType());
        }
    }
}
