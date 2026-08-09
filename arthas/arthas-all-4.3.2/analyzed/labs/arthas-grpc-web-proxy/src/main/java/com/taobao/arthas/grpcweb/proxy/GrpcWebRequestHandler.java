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

import com.taobao.arthas.common.Pair;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * gRPC-Web 请求处理器：解析 HTTP 请求、反序列化 protobuf 入参并调用本地 gRPC Stub。
 *
 * <p>URI 形如 {@code /包名.服务名/MethodName}，通过反射定位 {@code *Grpc} 类与 Stub 方法，
 * 响应经 {@link SendGrpcWebResponse} 以 chunked HTTP 返回浏览器。</p>
 */
public class GrpcWebRequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());
    /** 与后端 gRPC 服务通信的连接管理器 */
    private final GrpcServiceConnectionManager grpcServiceConnectionManager;

    public GrpcWebRequestHandler(GrpcServiceConnectionManager g) {
        grpcServiceConnectionManager = g;
    }

    /**
     * 处理一条完整的 gRPC-Web HTTP 请求。
     *
     * @param ctx Netty 通道上下文
     * @param req 聚合后的 HTTP 请求（含 body）
     */
    public void handle(ChannelHandlerContext ctx, FullHttpRequest req) {
        // 浏览器预检 OPTIONS 直接返回 CORS 头
        if (req.method().equals(HttpMethod.OPTIONS)) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            CorsUtils.updateCorsHeader(response.headers());
            ctx.writeAndFlush(response);
            return;
        }

        String contentTypeStr = req.headers().get(HttpHeaderNames.CONTENT_TYPE);

        MessageUtils.ContentType contentType = MessageUtils.validateContentType(contentTypeStr);
        SendGrpcWebResponse sendResponse = new SendGrpcWebResponse(ctx, req);

        try {
            // 从 URI 解析服务类名与 RPC 方法名，并加载对应 Grpc 类
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(req.uri());
            String pathInfo = queryStringDecoder.path();

            Pair<String, String> classAndMethodNames = getClassAndMethod(pathInfo);
            String className = classAndMethodNames.getFirst();
            String methodName = classAndMethodNames.getSecond();
            Class cls = getClassObject(className);
            if (cls == null) {
                logger.error("cannot find service impl in the request, className: " + className);
                // 请求中的类名无法解析为 *Grpc 实现
                sendResponse.returnUnimplementedStatusCode(className);
                return;
            }

            // 创建拦截器，把 gRPC 响应头/尾写回 HTTP
            CountDownLatch latch = new CountDownLatch(1);
            GrpcWebClientInterceptor interceptor = new GrpcWebClientInterceptor(latch, sendResponse);
            Channel channel = grpcServiceConnectionManager.getChannelWithClientInterceptor(interceptor);

            // 反射创建 AsyncStub 并绑定 HTTP 头中的 x-grpc-* Metadata
            io.grpc.stub.AbstractStub asyncStub = getRpcStub(channel, cls, "newStub");
            Metadata headers = MetadataUtil.getHtpHeaders(req.headers());
            if (!headers.keys().isEmpty()) {
                asyncStub = MetadataUtils.attachHeaders(asyncStub, headers);
            }
            Method asyncStubCall = getRpcMethod(asyncStub, methodName);
            // 从请求体解帧得到 protobuf 字节，再反序列化为入参对象
            ByteBuf content = req.content();
            InputStream in = new ByteBufInputStream(content);
            MessageDeframer deframer = new MessageDeframer();
            Object inObj = null;
            if (deframer.processInput(in, contentType)) {
                inObj = MessageUtils.getInputProtobufObj(asyncStubCall, deframer.getMessageBytes());
            }
            ManagedChannel managedChannel = grpcServiceConnectionManager.getChannel();
            // 发起 RPC；响应由 GrpcCallResponseReceiver 写回 HTTP chunk
            asyncStubCall.invoke(asyncStub, inObj, new GrpcCallResponseReceiver(sendResponse, latch,managedChannel));
            if (!latch.await( 1000, TimeUnit.MILLISECONDS)) {
                logger.warn("grpc call took too long!");
            }
        } catch (Exception e) {
            logger.error("try to invoke grpc serivce error, uri: {}", req.uri(), e);
            sendResponse.writeError(Status.UNAVAILABLE.withCause(e));
        }
    }

    /**
     * 从路径 {@code /RpcClass/RpcMethod} 解析类名与方法名（方法首字母转小写以匹配 Java Bean 命名）。
     *
     * @param pathInfo 不含 query 的路径
     * @return 类名与方法名对
     */
    private Pair<String, String> getClassAndMethod(String pathInfo) throws IllegalArgumentException {
        // pathInfo 以 "/" 开头，跳过首字符再按 "/" 分割
        String[] rpcClassAndMethodTokens = pathInfo.substring(1).split("/");
        if (rpcClassAndMethodTokens.length != 2) {
            throw new IllegalArgumentException("incorrect pathinfo: " + pathInfo);
        }

        String rpcClassName = rpcClassAndMethodTokens[0];
        String rpcMethodNameRecvd = rpcClassAndMethodTokens[1];
        String rpcMethodName = rpcMethodNameRecvd.substring(0, 1).toLowerCase() + rpcMethodNameRecvd.substring(1);
        return new Pair<>(rpcClassName, rpcMethodName);
    }

    /** 加载 {@code className + "Grpc"} 生成的 gRPC 服务类。 */
    private Class<?> getClassObject(String className) {
        Class rpcClass = null;
        try {
            rpcClass = Class.forName(className + "Grpc");
        } catch (ClassNotFoundException e) {
            logger.info("no such class " + className);
        }
        return rpcClass;
    }

    /** 反射调用 {@code stubName(Channel)} 静态工厂（如 {@code newStub}）创建 Stub。 */
    private io.grpc.stub.AbstractStub getRpcStub(Channel ch, Class cls, String stubName) {
        try {
            Method m = cls.getDeclaredMethod(stubName, io.grpc.Channel.class);
            return (io.grpc.stub.AbstractStub) m.invoke(null, ch);
        } catch (Exception e) {
            logger.warn("Error when fetching " + stubName + " for: " + cls.getName());
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 在 Stub 实例上按名称查找 RPC 方法。
     *
     * @param stub Stub 实例
     * @param rpcMethodName 方法名（已转为 camelCase）
     * @return 匹配的 {@link Method}
     */
    private Method getRpcMethod(Object stub, String rpcMethodName) {
        for (Method m : stub.getClass().getMethods()) {
            if (m.getName().equals(rpcMethodName)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Couldn't find rpcmethod: " + rpcMethodName);
    }

    /**
     * 将 gRPC {@link StreamObserver} 回调桥接到 gRPC-Web HTTP 写出逻辑。
     */
    private static class GrpcCallResponseReceiver<Object> implements StreamObserver {
        private final SendGrpcWebResponse sendResponse;
        private final CountDownLatch latch;

        /** 写出失败时需 shutdown 的后端 Channel */
        private final ManagedChannel channel;

        GrpcCallResponseReceiver(SendGrpcWebResponse s, CountDownLatch c, ManagedChannel channel) {
            sendResponse = s;
            latch = c;
            this.channel = channel;
        }

        @Override
        public void onNext(java.lang.Object resp) {
            // TODO: 校验 resp 类型是否与 RPC 返回类型一致
            byte[] outB = ((com.google.protobuf.GeneratedMessageV3) resp).toByteArray();
            if(!sendResponse.writeResponse(outB)){
                // 客户端已断开或写出失败，强制关闭后端 gRPC 连接
                this.channel.shutdownNow();
                logger.error("Grpc shutdown from grpc web proxy client");
            }
        }

        @Override
        public void onError(Throwable t) {
            Status s = Status.fromThrowable(t);
            sendResponse.writeError(s);
            latch.countDown();
        }

        @Override
        public void onCompleted() {
            sendResponse.writeTrailer(Status.OK, null);
            latch.countDown();
        }
    }
}
