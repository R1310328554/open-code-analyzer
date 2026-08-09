/*
 * Copyright 2016 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.compression;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.SystemPropertyUtil;

import java.nio.ByteBuffer;

/**
 * 压缩/解压模块内部工具：校验和比对、安全 NIO 缓冲访问及默认转发字节数配置。
 */
final class CompressionUtil {

    /** 解压时允许向前扫描的最大字节数，可通过系统属性 {@code io.netty.compression.defaultMaxForwardBytes} 覆盖。 */
    static final int DEFAULT_MAX_FORWARD_BYTES = SystemPropertyUtil.getInt(
            "io.netty.compression.defaultMaxForwardBytes", 64 * 1024);

    private CompressionUtil() { }

    /** 重算解压数据校验和并与期望值比对，不一致时抛出 {@link DecompressionException}。 */
    static void checkChecksum(ByteBufChecksum checksum, ByteBuf uncompressed, int currentChecksum) {
        checksum.reset();
        checksum.update(uncompressed,
                uncompressed.readerIndex(), uncompressed.readableBytes());

        final int checksumResult = (int) checksum.getValue();
        if (checksumResult != currentChecksum) {
            throw new DecompressionException(String.format(
                    "stream corrupted: mismatching checksum: %d (expected: %d)",
                    checksumResult, currentChecksum));
        }
    }

    /** 从 {@link ByteBuf} 可读区域获取安全的 {@link ByteBuffer} 视图。 */
    static ByteBuffer safeReadableNioBuffer(ByteBuf buffer) {
        return safeNioBuffer(buffer, buffer.readerIndex(), buffer.readableBytes());
    }

    /** 在指定区间获取 NIO 缓冲；单段缓冲时用 {@code internalNioBuffer} 避免拷贝。 */
    static ByteBuffer safeNioBuffer(ByteBuf buffer, int index, int length) {
        return buffer.nioBufferCount() == 1 ? buffer.internalNioBuffer(index, length)
                : buffer.nioBuffer(index, length);
    }
}
