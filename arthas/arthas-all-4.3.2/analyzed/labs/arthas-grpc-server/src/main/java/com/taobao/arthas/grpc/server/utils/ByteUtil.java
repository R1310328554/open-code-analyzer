package com.taobao.arthas.grpc.server.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

/**
 * Netty {@link ByteBuf} 与 Java {@code byte[]} 之间的转换工具。
 * <p>
 * 统一使用 {@link PooledByteBufAllocator#DEFAULT} 分配缓冲，减少堆外内存拷贝开销。
 *
 * @author: FengYe
 * @date: 2024/9/5 00:51
 * @description: ByteUtil
 */
public class ByteUtil {

    /** 分配空的可写 ByteBuf */
    public static ByteBuf newByteBuf() {
        return PooledByteBufAllocator.DEFAULT.buffer();
    }

    /** 将已有字节数组包装为可写 ByteBuf */
    public static ByteBuf newByteBuf(byte[] bytes) {
        return PooledByteBufAllocator.DEFAULT.buffer(bytes.length).writeBytes(bytes);
    }

    /**
     * 将 ByteBuf 可读区域转为 byte 数组。
     * <p>
     * 若底层已暴露数组则零拷贝返回，否则复制可读字节到新数组。
     *
     * @param buf 待读取的 Netty 缓冲
     * @return 包含全部可读字节的数组
     */
    public static byte[] getBytes(ByteBuf buf) {
        if (buf.hasArray()) {
            // 如果 ByteBuf 是一个支持底层数组的实现，直接获取数组
            return buf.array();
        } else {
            // 创建一个新的 byte 数组
            byte[] bytes = new byte[buf.readableBytes()];
            // 将 ByteBuf 的内容复制到 byte 数组中
            buf.getBytes(buf.readerIndex(), bytes);
            return bytes;
        }
    }
}
