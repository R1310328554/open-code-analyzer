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

import com.taobao.arthas.grpcweb.proxy.MessageUtils.ContentType;
import io.grpc.Metadata;
import io.grpc.Status;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedStream;
import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Base64;
import java.util.Map;

/**
 * <pre>
 * * https://github.com/grpc/grpc/blob/master/doc/PROTOCOL-WEB.md
 * * https://github.com/grpc/grpc/blob/master/doc/PROTOCOL-HTTP2.md
 *
 * 据协议与抓包分析，gRPC-Web 响应需用 HTTP/1.1 chunked 包装 gRPC 帧数据。
 *
 * gRPC-Web 的 HTTP/1.1 响应由三部分组成：
 * 1. headers — HTTP 状态码恒为 200
 * 2. data chunk — 可有多块 DATA 帧
 * 3. trailer chunk — grpc-status、grpc-message 等在此
 *
 * </pre>
 *
 * @author hengyunabc 2023-09-06
 *
 */
class SendGrpcWebResponse {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

    /** 与请求一致的 Content-Type，决定二进制或 Base64 文本模式 */
    private final String contentType;

    /**
     * HTTP/1.1 响应头（非 chunk 体）是否已发送
     */
    private boolean isHeaderSent = false;

    /**
     * 所有 gRPC 消息 chunk 发送完毕后，是否已发送空的结束 chunk
     */
    private boolean isEndChunkSent = false;

    /**
     * gRPC trailer 帧是否已通过 HTTP chunk 写出
     */
    private boolean isTrailerSent = false;

    /**
     * 向客户端写 DATA chunk 是否仍成功；失败时上层应关闭后端 gRPC 连接
     */
    private Boolean isSuccessSendData = true;

    private ChannelHandlerContext ctx;

    SendGrpcWebResponse(ChannelHandlerContext ctx, FullHttpRequest req) {
        HttpHeaders headers = req.headers();
        contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
        this.ctx = ctx;
    }

    /**
     * 写出 HTTP 响应头（Transfer-Encoding: chunked）及 gRPC 初始 Metadata。
     *
     * @param headers 来自 gRPC 的响应头 Metadata，可为 null
     */
    synchronized void writeHeaders(Metadata headers) {
        if (isHeaderSent) {
            return;
        }
        // 发送 HTTP/1.1 起始行与响应头
        DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType).set(HttpHeaderNames.TRANSFER_ENCODING,
                "chunked");

        CorsUtils.updateCorsHeader(response.headers());

        if (headers != null) {
            Map<String, String> ht = MetadataUtil.getHttpHeadersFromMetadata(headers);
            for (String key : ht.keySet()) {
                response.headers().set(key, ht.get(key));
            }
        }

        logger.debug("write headers: {}", response);

        ctx.writeAndFlush(response);

        isHeaderSent = true;
    }

    /** 服务实现类未找到时，写出 UNIMPLEMENTED 状态的 trailer。 */
    synchronized void returnUnimplementedStatusCode(String className) {
        writeHeaders(null);
        writeTrailer(
                Status.UNIMPLEMENTED.withDescription("Can not find service impl, check dep, service: " + className),
                null);
    }

    /** 发送 HTTP chunked 传输的最后一个空块，标记响应结束。 */
    private void writeEndChunk() {
        if (isEndChunkSent) {
            return;
        }
        LastHttpContent end = new DefaultLastHttpContent();
        ctx.writeAndFlush(end);
        isEndChunkSent = true;
    }

    /** 出错路径：先写头再写带 grpc-status 的 trailer。 */
    synchronized void writeError(Status s) {
        writeHeaders(null);
        writeTrailer(s, null);
    }

    /**
     * 将 gRPC {@link Status} 与 trailer Metadata 编码为 TRAILER 帧并写出，随后发送结束 chunk。
     */
    synchronized void writeTrailer(Status status, Metadata trailer) {
        if (isTrailerSent) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        if (trailer != null) {
            Map<String, String> ht = MetadataUtil.getHttpHeadersFromMetadata(trailer);
            for (String key : ht.keySet()) {
                sb.append(String.format("%s:%s\r\n", key, ht.get(key)));
            }
        }
        sb.append(String.format("grpc-status:%d\r\n", status.getCode().value()));
        if (status.getDescription() != null && !status.getDescription().isEmpty()) {
            sb.append(String.format("grpc-message:%s\r\n", status.getDescription()));
        }

        writeResponse(sb.toString().getBytes(), MessageFramer.Type.TRAILER);

        isTrailerSent = true;

        writeEndChunk();
    }

    /** 写出一条 DATA 帧（protobuf 响应体）。 */
    synchronized boolean writeResponse(byte[] out) {
        return writeResponse(out, MessageFramer.Type.DATA);
    }

    /**
     * 组帧并写出 DATA 或 TRAILER chunk。
     *
     * @return 写出是否仍视为成功（监听 ChannelFuture 更新 {@link #isSuccessSendData}）
     */
    private boolean writeResponse(byte[] out, MessageFramer.Type type) {
        if (isTrailerSent) {
            logger.error("grpcweb trailer sented, writeResponse can not be called, framer type: {}", type);
            return false;
        }

        try {
            // 当前未实现单条消息拆成多帧
            byte[] prefix = new MessageFramer().getPrefix(out, type);
            ByteArrayOutputStream oStream = new ByteArrayOutputStream();
            // grpc-web-text 模式：帧头+payload 整体 Base64 编码
            if (MessageUtils.getContentType(contentType) == ContentType.GRPC_WEB_TEXT) {
                byte[] concated = new byte[out.length + 5];
                System.arraycopy(prefix, 0, concated, 0, 5);
                System.arraycopy(out, 0, concated, 5, out.length);
                oStream.write(Base64.getEncoder().encode(concated));
            } else {
                oStream.write(prefix);
                oStream.write(out);
            }

            byte[] byteArray = oStream.toByteArray();

            InputStream dataStream = new ByteArrayInputStream(byteArray);
            ChunkedStream chunkedStream = new ChunkedStream(dataStream);
            SingleHttpChunkedInput httpChunkedInput = new SingleHttpChunkedInput(chunkedStream);
            ChannelFuture channelFuture = ctx.writeAndFlush(httpChunkedInput);
            ChannelFutureListener channelFutureListener = new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (!future.isSuccess()) {
                        // 客户端断开或网络错误导致写出失败
                        isSuccessSendData = false;
                    }
                }
            };
            channelFuture.addListener(channelFutureListener);
            return isSuccessSendData;

        } catch (IOException e) {
            logger.error("write grpcweb response error, framer type: {}", type, e);
            return false;
        }
    }

}
