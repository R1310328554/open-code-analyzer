/*
 * Copyright 2019 The Netty Project
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
import net.jpountz.xxhash.StreamingXXHash32;
import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;

import java.nio.ByteBuffer;
import java.util.zip.Checksum;

/**
 * 供 {@link Lz4FrameEncoder} 与 {@link Lz4FrameDecoder} 使用的专用 {@link ByteBufChecksum}。
 *
 * {@link StreamingXXHash32#asChecksum()} 的 {@link Checksum#update(int)} 每次分配单字节数组，
 * 且无 {@link ByteBuffer} 重载，无法配合 Reflective/Slow 包装高效处理直接缓冲。
 *
 * 块级 {@link XXHash32#hash} 一次计算整段数据，用法为 reset → 单次 update → getValue，
 * 与 LZ4 帧编解码的块校验模式一致。
 */
public final class Lz4XXHash32 extends ByteBufChecksum {

    private static final XXHash32 XXHASH32 = XXHashFactory.fastestInstance().hash32();

    private final int seed;
    private boolean used;
    private int value;

    @SuppressWarnings("WeakerAccess")
    public Lz4XXHash32(int seed) {
        this.seed = seed;
    }

    @Override
    public void update(int b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(byte[] b, int off, int len) {
        if (used) {
            throw new IllegalStateException();
        }
        value = XXHASH32.hash(b, off, len, seed);
        used = true;
    }

    @Override
    public void update(ByteBuf b, int off, int len) {
        if (used) {
            throw new IllegalStateException();
        }
        if (b.hasArray()) {
            value = XXHASH32.hash(b.array(), b.arrayOffset() + off, len, seed);
        } else {
            value = XXHASH32.hash(CompressionUtil.safeNioBuffer(b, off, len), seed);
        }
        used = true;
    }

    @Override
    public long getValue() {
        if (!used) {
            throw new IllegalStateException();
        }
        /*
         * 最高 4 位被丢弃（疑似 bug），但须与 StreamingXXHash32#asChecksum() 行为保持一致。
         */
        return value & 0xFFFFFFFL;
    }

    @Override
    public void reset() {
        used = false;
    }
}
