package com.taobao.arthas.grpc.server.handler;

import com.taobao.arthas.grpc.server.handler.constant.GrpcInvokeTypeEnum;
import com.taobao.arthas.grpc.server.utils.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http2.Http2Headers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * 单条 gRPC 流上的请求上下文与消息缓冲。
 * <p>
 * 累积 HTTP/2 DATA 帧中的 gRPC 消息（5 字节前缀：压缩标志 + 长度），
 * 支持 gzip 解压并按帧读取 Protobuf 字节。
 *
 * @author: FengYe
 * @date: 2024/9/4 23:07
 * @description: GrpcRequest grpc 请求体
 */
public class GrpcRequest<T> {

    /**
     * 请求对应的 streamId
     */
    private Integer streamId;

    /**
     * 请求的 service
     */
    private String service;

    /**
     * 请求的 method
     */
    private String method;

    /**
     * 二进制数据，可能包含多个 grpc body，每个 body 都带有 5 个 byte 的前缀，分别是 boolean compressed - int length
     */
    private ByteBuf byteData;

    /**
     * 二进制数据的长度
     */
    private int length;

    /**
     * 请求class
     */
    private Class<?> clazz;

    /**
     * 是否是 grpc 流式请求
     */
    private boolean stream;

    /**
     * 是否是 grpc 流式请求的第一个data
     */
    private boolean streamFirstData;

    /**
     * http2 headers
     */
    private Http2Headers headers;

    /**
     * grpc 调用类型
     */
    private GrpcInvokeTypeEnum grpcType;


    /**
     * @param streamId HTTP/2 流 ID
     * @param path gRPC 服务名（来自 :path 第一段）
     * @param method 方法名（:path 第二段）
     */
    public GrpcRequest(Integer streamId, String path, String method) {
        this.streamId = streamId;
        this.service = path;
        this.method = method;
        this.byteData = ByteUtil.newByteBuf();
    }

    /** 追加 DATA 帧负载，必要时 gzip 解压后写入内部缓冲。 */
    public void writeData(ByteBuf byteBuf) {
        byte[] bytes = ByteUtil.getBytes(byteBuf);
        if (bytes.length == 0) {
            return;
        }
        byte[] decompressedData = decompressGzip(bytes);
        if (decompressedData == null) {
            return;
        }
        byteData.writeBytes(ByteUtil.newByteBuf(decompressedData));
    }

    /**
     * 读取部分数据
     *
     * @return 下一条 gRPC 消息体字节，无数据时返回 null
     */
    public synchronized byte[] readData() {
        if (byteData.readableBytes() == 0) {
            return null;
        }
        boolean compressed = byteData.readBoolean();
        int length = byteData.readInt();
        byte[] bytes = new byte[length];
        byteData.readBytes(bytes);
        return bytes;
    }

    /** 清空内部 ByteBuf 缓冲。 */
    public void clearData() {
        byteData.clear();
    }

    /** 检测 gzip 魔数并解压，非 gzip 则原样返回。 */
    private byte[] decompressGzip(byte[] compressedData) {
        boolean isGzip = (compressedData.length > 2 && (compressedData[0] & 0xff) == 0x1f && (compressedData[1] & 0xff) == 0x8b);
        if (isGzip) {
            try (InputStream byteStream = new ByteArrayInputStream(compressedData);
                 GZIPInputStream gzipStream = new GZIPInputStream(byteStream);
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzipStream.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                return out.toByteArray();
            } catch (IOException e) {
                System.err.println("Failed to decompress GZIP data: " + e.getMessage());
                // Optionally rethrow the exception or return an Optional<byte[]>
                return null; // or throw new RuntimeException(e);
            }
        } else {
            return compressedData;
        }
    }

    /** 返回 {@code service.method} 路由键。 */
    public String getGrpcMethodKey() {
        return service + "." + method;
    }

    public Integer getStreamId() {
        return streamId;
    }

    public String getService() {
        return service;
    }

    public String getMethod() {
        return method;
    }

    public ByteBuf getByteData() {
        return byteData;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public boolean isStreamFirstData() {
        return streamFirstData;
    }

    public void setStreamFirstData(boolean streamFirstData) {
        this.streamFirstData = streamFirstData;
    }

    public Http2Headers getHeaders() {
        return headers;
    }

    public void setHeaders(Http2Headers headers) {
        this.headers = headers;
    }

    public GrpcInvokeTypeEnum getGrpcType() {
        return grpcType;
    }

    public void setGrpcType(GrpcInvokeTypeEnum grpcType) {
        this.grpcType = grpcType;
    }
}
