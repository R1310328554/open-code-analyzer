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

/**
 * Deflate 压缩参数：{@link #compressionLevel()}、{@link #memLevel()} 与 {@link #windowBits()}。
 * 实现 {@link CompressionOptions}，可在多个编码器间共享。
 */
public class DeflateOptions implements CompressionOptions {

    private final int compressionLevel;
    private final int windowBits;
    private final int memLevel;

    /**
     * @see StandardCompressionOptions#deflate()
     */
    /** 默认 Deflate 选项：级别 6、窗口 15、内存 8。 */
    static final DeflateOptions DEFAULT = new DeflateOptions(
            6, 15, 8
    );

    /**
     * @see StandardCompressionOptions#deflate(int, int, int)
     */
    /** 构造 Deflate 选项并校验各参数合法范围。 */
    DeflateOptions(int compressionLevel, int windowBits, int memLevel) {
        this.compressionLevel = ObjectUtil.checkInRange(compressionLevel, 0, 9, "compressionLevel");
        this.windowBits = ObjectUtil.checkInRange(windowBits, 9, 15, "windowBits");
        this.memLevel = ObjectUtil.checkInRange(memLevel, 1, 9, "memLevel");
    }

    /** @return Deflate 压缩级别（0–9）。 */
    public int compressionLevel() {
        return compressionLevel;
    }

    /** @return 滑动窗口大小位数（9–15）。 */
    public int windowBits() {
        return windowBits;
    }

    /** @return 压缩内存级别（1–9）。 */
    public int memLevel() {
        return memLevel;
    }
}
