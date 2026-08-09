/*
 * Copyright 2021 The Netty Project
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

import com.aayushatharva.brotli4j.encoder.Encoder;
import io.netty.util.internal.ObjectUtil;

/**
 * 标准压缩选项工厂，提供 Brotli、Gzip、Deflate、Zstd、Snappy 等默认与自定义配置。
 */
public final class StandardCompressionOptions {

    private StandardCompressionOptions() {
        // 工具类，禁止实例化
    }

    /** 默认 Brotli 选项：质量 4、模式 TEXT。 */
    public static BrotliOptions brotli() {
        return BrotliOptions.DEFAULT;
    }

    /**
     * 由 {@link Encoder.Parameters} 构造 Brotli 选项。
     *
     * @param parameters {@link Encoder.Parameters} Instance
     * @throws NullPointerException If {@link Encoder.Parameters} is {@code null}
     * @deprecated Use {@link #brotli(int, int, BrotliMode)}
     */
    @Deprecated
    public static BrotliOptions brotli(Encoder.Parameters parameters) {
        return new BrotliOptions(parameters);
    }

    /**
     * 按质量、窗口与模式创建 Brotli 选项。
     *
     * @param quality Specifies the compression level.
     * @param window  Specifies the size of the sliding window when compressing.
     * @param mode    optimizes the compression algorithm based on the type of input data.
     * @throws NullPointerException If {@link BrotliMode} is {@code null}
     */
    public static BrotliOptions brotli(int quality, int window, BrotliMode mode) {
        ObjectUtil.checkInRange(quality, 0, 11, "quality");
        ObjectUtil.checkInRange(window, 10, 24, "window");
        ObjectUtil.checkNotNull(mode, "mode");

        Encoder.Parameters parameters = new Encoder.Parameters()
                .setQuality(quality)
                .setWindow(window)
                .setMode(mode.adapt());
        return new BrotliOptions(parameters);
    }

    /** 默认 Zstd 选项：默认压缩级别、64KB 块大小、最大编码尺寸。 */
    public static ZstdOptions zstd() {
        return ZstdOptions.DEFAULT;
    }

    /**
     * 创建自定义 Zstd 压缩选项。
     *
     * @param blockSize        is used to calculate the compressionLevel
     * @param maxEncodeSize    specifies the size of the largest compressed object
     * @param compressionLevel specifies the level of the compression
     */
    public static ZstdOptions zstd(int compressionLevel, int blockSize, int maxEncodeSize) {
        return new ZstdOptions(compressionLevel, blockSize, maxEncodeSize);
    }

    /** 创建 Snappy 压缩选项（当前无额外参数）。 */
    public static SnappyOptions snappy() {
        return new SnappyOptions();
    }

    /** 默认 Gzip 选项：级别 6、窗口 15、内存 8。 */
    public static GzipOptions gzip() {
        return GzipOptions.DEFAULT;
    }

    /**
     * 创建自定义 Gzip 压缩选项。
     *
     * @param compressionLevel {@code 1} yields the fastest compression and {@code 9} yields the
     *                         best compression.  {@code 0} means no compression.  The default
     *                         compression level is {@code 6}.
     * @param windowBits       The base two logarithm of the size of the history buffer.  The
     *                         value should be in the range {@code 9} to {@code 15} inclusive.
     *                         Larger values result in better compression at the expense of
     *                         memory usage.  The default value is {@code 15}.
     * @param memLevel         How much memory should be allocated for the internal compression
     *                         state.  {@code 1} uses minimum memory and {@code 9} uses maximum
     *                         memory.  Larger values result in better and faster compression
     *                         at the expense of memory usage.  The default value is {@code 8}
     */
    public static GzipOptions gzip(int compressionLevel, int windowBits, int memLevel) {
        return new GzipOptions(compressionLevel, windowBits, memLevel);
    }

    /** 默认 Deflate 选项：级别 6、窗口 15、内存 8。 */
    public static DeflateOptions deflate() {
        return DeflateOptions.DEFAULT;
    }

    /**
     * 创建自定义 Deflate 压缩选项。
     *
     * @param compressionLevel {@code 1} yields the fastest compression and {@code 9} yields the
     *                         best compression.  {@code 0} means no compression.  The default
     *                         compression level is {@code 6}.
     * @param windowBits       The base two logarithm of the size of the history buffer.  The
     *                         value should be in the range {@code 9} to {@code 15} inclusive.
     *                         Larger values result in better compression at the expense of
     *                         memory usage.  The default value is {@code 15}.
     * @param memLevel         How much memory should be allocated for the internal compression
     *                         state.  {@code 1} uses minimum memory and {@code 9} uses maximum
     *                         memory.  Larger values result in better and faster compression
     *                         at the expense of memory usage.  The default value is {@code 8}
     */
    public static DeflateOptions deflate(int compressionLevel, int windowBits, int memLevel) {
        return new DeflateOptions(compressionLevel, windowBits, memLevel);
    }
}
