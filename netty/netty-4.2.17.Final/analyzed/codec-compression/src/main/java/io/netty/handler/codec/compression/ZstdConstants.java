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

import com.github.luben.zstd.Zstd;

/** Zstd 编解码默认常量（压缩级别、块大小、编码上限）。 */
final class ZstdConstants {

    /** 默认压缩级别。 */
    static final int DEFAULT_COMPRESSION_LEVEL = Zstd.defaultCompressionLevel();

    /** 最小压缩级别。 */
    static final int MIN_COMPRESSION_LEVEL = Zstd.minCompressionLevel();

    /** 最大压缩级别。 */
    static final int MAX_COMPRESSION_LEVEL = Zstd.maxCompressionLevel();

    /** 单次编码允许的最大尺寸。 */
    static final int DEFAULT_MAX_ENCODE_SIZE = Integer.MAX_VALUE;
    /** 默认块大小（64 KB）。 */
    static final int DEFAULT_BLOCK_SIZE = 1 << 16;  // 64 KB

    private ZstdConstants() { }
}
