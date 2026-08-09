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

import io.netty.util.internal.ObjectUtil;

import static io.netty.handler.codec.compression.ZstdConstants.DEFAULT_COMPRESSION_LEVEL;
import static io.netty.handler.codec.compression.ZstdConstants.MIN_COMPRESSION_LEVEL;
import static io.netty.handler.codec.compression.ZstdConstants.MAX_COMPRESSION_LEVEL;
import static io.netty.handler.codec.compression.ZstdConstants.DEFAULT_BLOCK_SIZE;
import static io.netty.handler.codec.compression.ZstdConstants.DEFAULT_MAX_ENCODE_SIZE;

/**
 * {@link ZstdOptions} 保存 Zstd 压缩的级别、块大小与最大编码尺寸。
 */
public class ZstdOptions implements CompressionOptions {

    private final int blockSize;
    private final int compressionLevel;
    private final int maxEncodeSize;

    /** 默认 Zstd 选项：默认压缩级别、块大小与最大编码尺寸。 */
    static final ZstdOptions DEFAULT = new ZstdOptions(DEFAULT_COMPRESSION_LEVEL, DEFAULT_BLOCK_SIZE,
            DEFAULT_MAX_ENCODE_SIZE);

    /**
     * 创建自定义 {@link ZstdOptions}。
     *
     * @param  blockSize
     *           分块压缩的块大小
     * @param  maxEncodeSize
     *           单块压缩结果允许的最大字节数
     * @param  compressionLevel
     *           压缩级别
     */
    ZstdOptions(int compressionLevel, int blockSize, int maxEncodeSize) {
        if (!Zstd.isAvailable()) {
            throw new IllegalStateException("zstd-jni is not available", Zstd.cause());
        }

        this.compressionLevel = ObjectUtil.checkInRange(compressionLevel,
                MIN_COMPRESSION_LEVEL, MAX_COMPRESSION_LEVEL, "compressionLevel");
        this.blockSize = ObjectUtil.checkPositive(blockSize, "blockSize");
        this.maxEncodeSize = ObjectUtil.checkPositive(maxEncodeSize, "maxEncodeSize");
    }

    public int compressionLevel() {
        return compressionLevel;
    }

    public int blockSize() {
        return blockSize;
    }

    public int maxEncodeSize() {
        return maxEncodeSize;
    }
}
