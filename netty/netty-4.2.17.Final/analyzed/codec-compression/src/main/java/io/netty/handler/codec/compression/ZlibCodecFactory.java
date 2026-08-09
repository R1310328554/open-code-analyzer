/*
 * Copyright 2012 The Netty Project
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

import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * Zlib 编解码器工厂：按 classpath 与系统属性在 JDK 与 JZlib 实现间选择。
 */
public final class ZlibCodecFactory {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ZlibCodecFactory.class);

    private static final int DEFAULT_JDK_WINDOW_SIZE = 15;
    private static final int DEFAULT_JDK_MEM_LEVEL = 8;

    private static final boolean noJdkZlibDecoder;
    private static final boolean noJdkZlibEncoder;

    private static final boolean JZLIB_AVAILABLE;

    static {
        noJdkZlibDecoder = SystemPropertyUtil.getBoolean("io.netty.noJdkZlibDecoder", false);
        logger.debug("-Dio.netty.noJdkZlibDecoder: {}", noJdkZlibDecoder);

        noJdkZlibEncoder = SystemPropertyUtil.getBoolean("io.netty.noJdkZlibEncoder", false);
        logger.debug("-Dio.netty.noJdkZlibEncoder: {}", noJdkZlibEncoder);

        boolean jzlibAvailable;
        try {
            Class.forName("com.jcraft.jzlib.JZlib", false,
                PlatformDependent.getClassLoader(ZlibCodecFactory.class));
            jzlibAvailable = true;
        } catch (ClassNotFoundException t) {
            jzlibAvailable = false;
            logger.debug(
                "JZlib not in the classpath; the only window bits supported value will be " +
                    DEFAULT_JDK_WINDOW_SIZE);
        }
        JZLIB_AVAILABLE = jzlibAvailable;
    }

    /** @return  classpath 含 JZlib 时为 {@code true}，可自定义窗口与内存级别。 */
    public static boolean isSupportingWindowSizeAndMemLevel() {
        return JZLIB_AVAILABLE;
    }

    public static ZlibEncoder newZlibEncoder(int compressionLevel) {
        if (noJdkZlibEncoder) {
            return new JZlibEncoder(compressionLevel);
        } else {
            return new JdkZlibEncoder(compressionLevel);
        }
    }

    public static ZlibEncoder newZlibEncoder(ZlibWrapper wrapper) {
        if (noJdkZlibEncoder) {
            return new JZlibEncoder(wrapper);
        } else {
            return new JdkZlibEncoder(wrapper);
        }
    }

    public static ZlibEncoder newZlibEncoder(ZlibWrapper wrapper, int compressionLevel) {
        if (noJdkZlibEncoder) {
            return new JZlibEncoder(wrapper, compressionLevel);
        } else {
            return new JdkZlibEncoder(wrapper, compressionLevel);
        }
    }

    public static ZlibEncoder newZlibEncoder(ZlibWrapper wrapper, int compressionLevel, int windowBits, int memLevel) {
        if (noJdkZlibEncoder ||
                windowBits != DEFAULT_JDK_WINDOW_SIZE || memLevel != DEFAULT_JDK_MEM_LEVEL) {
            return new JZlibEncoder(wrapper, compressionLevel, windowBits, memLevel);
        } else {
            return new JdkZlibEncoder(wrapper, compressionLevel);
        }
    }

    public static ZlibEncoder newZlibEncoder(byte[] dictionary) {
        if (noJdkZlibEncoder) {
            return new JZlibEncoder(dictionary);
        } else {
            return new JdkZlibEncoder(dictionary);
        }
    }

    public static ZlibEncoder newZlibEncoder(int compressionLevel, byte[] dictionary) {
        if (noJdkZlibEncoder) {
            return new JZlibEncoder(compressionLevel, dictionary);
        } else {
            return new JdkZlibEncoder(compressionLevel, dictionary);
        }
    }

    public static ZlibEncoder newZlibEncoder(int compressionLevel, int windowBits, int memLevel, byte[] dictionary) {
        if (noJdkZlibEncoder ||
                windowBits != DEFAULT_JDK_WINDOW_SIZE || memLevel != DEFAULT_JDK_MEM_LEVEL) {
            return new JZlibEncoder(compressionLevel, windowBits, memLevel, dictionary);
        } else {
            return new JdkZlibEncoder(compressionLevel, dictionary);
        }
    }

    /**
     * 创建 Zlib 解码器（无分配上限）。
     *
     * @deprecated Use {@link ZlibCodecFactory#newZlibDecoder(int)}.
     */
    @Deprecated
    public static ZlibDecoder newZlibDecoder() {
        return newZlibDecoder(0);
    }

    /**
     * 创建带解压缓冲上限的 Zlib 解码器。
     *
     * @param maxAllocation
     *           Maximum size of the decompression buffer. Must be &gt;= 0.
     *           If zero, maximum size is not limited by decoder.
     */
    public static ZlibDecoder newZlibDecoder(int maxAllocation) {
        if (noJdkZlibDecoder) {
            return new JZlibDecoder(maxAllocation);
        } else {
            return new JdkZlibDecoder(true, maxAllocation);
        }
    }

    /**
     * 按封装格式创建 Zlib 解码器（无分配上限）。
     *
     * @deprecated Use {@link ZlibCodecFactory#newZlibDecoder(ZlibWrapper, int)}.
     */
    @Deprecated
    public static ZlibDecoder newZlibDecoder(ZlibWrapper wrapper) {
        return newZlibDecoder(wrapper, 0);
    }

    /**
     * 按封装格式与缓冲上限创建 Zlib 解码器。
     *
     * @param maxAllocation
     *          Maximum size of the decompression buffer. Must be &gt;= 0.
     *          If zero, maximum size is not limited by decoder.
     */
    public static ZlibDecoder newZlibDecoder(ZlibWrapper wrapper, int maxAllocation) {
        if (noJdkZlibDecoder) {
            return new JZlibDecoder(wrapper, maxAllocation);
        } else {
            return new JdkZlibDecoder(wrapper, true, maxAllocation);
        }
    }

    /**
     * 使用预设字典创建 Zlib 解码器（仅 {@link ZlibWrapper#ZLIB} 支持字典）。
     *
     * @deprecated Use {@link ZlibCodecFactory#newZlibDecoder(byte[], int)}.
     */
    @Deprecated
    public static ZlibDecoder newZlibDecoder(byte[] dictionary) {
        return newZlibDecoder(dictionary, 0);
    }

    /**
     * 使用预设字典与缓冲上限创建 Zlib 解码器。
     * 封装格式固定为 {@link ZlibWrapper#ZLIB}（唯一支持预设字典的格式）。
     *
     * @param maxAllocation
     *          Maximum size of the decompression buffer. Must be &gt;= 0.
     *          If zero, maximum size is not limited by decoder.
     */
    public static ZlibDecoder newZlibDecoder(byte[] dictionary, int maxAllocation) {
        if (noJdkZlibDecoder) {
            return new JZlibDecoder(dictionary, maxAllocation);
        } else {
            return new JdkZlibDecoder(dictionary, maxAllocation);
        }
    }

    private ZlibCodecFactory() {
        // 工具类，禁止实例化
    }
}
