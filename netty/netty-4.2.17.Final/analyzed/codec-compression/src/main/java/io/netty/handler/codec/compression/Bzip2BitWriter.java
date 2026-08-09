/*
 * Copyright 2014 The Netty Project
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

/**
 * Bzip2 位级写入器：支持单比特布尔值、一元编码数字、最长 32 位的位串以及按位对齐的 32 位整数。
 * 累积满 32 位后向 {@link ByteBuf} 写入一个字（4 字节）。
 */
final class Bzip2BitWriter {
    /** 待写入输出流的位缓冲（高 64 位有效区）。 */
    private long bitBuffer;

    /** {@link #bitBuffer} 中当前已缓冲的位数。 */
    private int bitCount;

    /**
     * Writes up to 32 bits to the output {@link ByteBuf}.
     * @param count 要写入的位数（最多 {@code 32}，受 {@code int} 宽度限制）
     * @param value 要写入的位值
     */
    void writeBits(ByteBuf out, final int count, final long value) {
        if (count < 0 || count > 32) {
            throw new IllegalArgumentException("count: " + count + " (expected: 0-32)");
        }
        int bitCount = this.bitCount;
        long bitBuffer = this.bitBuffer | value << 64 - count >>> bitCount;
        bitCount += count;

        if (bitCount >= 32) {
            out.writeInt((int) (bitBuffer >>> 32));
            bitBuffer <<= 32;
            bitCount -= 32;
        }
        this.bitBuffer = bitBuffer;
        this.bitCount = bitCount;
    }

    /**
     * Writes a single bit to the output {@link ByteBuf}.
     * @param value 要写入的单个比特
     */
    void writeBoolean(ByteBuf out, final boolean value) {
        int bitCount = this.bitCount + 1;
        long bitBuffer = this.bitBuffer | (value ? 1L << 64 - bitCount : 0L);

        if (bitCount == 32) {
            out.writeInt((int) (bitBuffer >>> 32));
            bitBuffer = 0;
            bitCount = 0;
        }
        this.bitBuffer = bitBuffer;
        this.bitCount = bitCount;
    }

    /**
     * Writes a zero-terminated unary number to the output {@link ByteBuf}.
     * 示例：value = 6 时输出 {@code 1111110}（6 个 1 后接 0）
     * @param value 要写入的连续 {@code 1} 的个数
     */
    void writeUnary(ByteBuf out, int value) {
        if (value < 0) {
            throw new IllegalArgumentException("value: " + value + " (expected 0 or more)");
        }
        while (value-- > 0) {
            writeBoolean(out, true);
        }
        writeBoolean(out, false);
    }

    /**
     * Writes an integer as 32 bits to the output {@link ByteBuf}.
     * @param value 要写入的 32 位整数
     */
    void writeInt(ByteBuf out, final int value) {
        writeBits(out, 32, value);
    }

    /**
     * Writes any remaining bits to the output {@link ByteBuf},
     * 必要时以零填充至整字节边界。
     */
    void flush(ByteBuf out) {
        final int bitCount = this.bitCount;

        if (bitCount > 0) {
            final long bitBuffer = this.bitBuffer;
            final int shiftToRight = 64 - bitCount;

            if (bitCount <= 8) {
                out.writeByte((int) (bitBuffer >>> shiftToRight << 8 - bitCount));
            } else if (bitCount <= 16) {
                out.writeShort((int) (bitBuffer >>> shiftToRight << 16 - bitCount));
            } else if (bitCount <= 24) {
                out.writeMedium((int) (bitBuffer >>> shiftToRight << 24 - bitCount));
            } else {
                out.writeInt((int) (bitBuffer >>> shiftToRight << 32 - bitCount));
            }
        }
    }
}
