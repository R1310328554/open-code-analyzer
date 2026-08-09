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

/**
 * Gzip 压缩参数，继承 {@link DeflateOptions} 的级别、窗口与内存配置。
 * 用于 Gzip 封装层的 Deflate 引擎选项。
 */
public final class GzipOptions extends DeflateOptions {

    /**
     * @see StandardCompressionOptions#gzip()
     */
    /** 默认 Gzip 选项：级别 6、窗口 15、内存 8。 */
    static final GzipOptions DEFAULT = new GzipOptions(
            6, 15, 8
    );

    /**
     * @see StandardCompressionOptions#gzip(int, int, int)
     */
    /** 构造 Gzip 选项并委托 {@link DeflateOptions} 校验参数范围。 */
    GzipOptions(int compressionLevel, int windowBits, int memLevel) {
        super(compressionLevel, windowBits, memLevel);
    }
}
