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
 * Bzip2 按位读取器：支持单 bit 布尔值、最长 32 bit 位串及 32 bit 对齐整数。
 * 位缓冲不足时从 {@link ByteBuf} 逐字节补充。
 */
class Bzip2BitReader {
    /**
     * 可检查的最大可读字节数上限。
     */
    private static final int MAX_COUNT_OF_READABLE_BYTES = Integer.MAX_VALUE >>> 3;

    /**
     * 输入数据源 {@link ByteBuf}。
     */
    private ByteBuf in;

    /**
     * 尚未消费的预读位缓冲（高位在左）。
     */
    private long bitBuffer;

    /**
     * {@link #bitBuffer} 中当前有效位数。
     */
    private int bitCount;

    /**
     * 绑定待读取的 {@link ByteBuf}。
     */
    void setByteBuf(ByteBuf in) {
        this.in = in;
    }

    /**
     * 从 {@link ByteBuf} 读取最多 32 位，结果在 int 中右对齐。
     * @param count The number of bits to read (maximum {@code 32} as a size of {@code int})
     * @return The bits requested, right-aligned within the integer
     */
    int readBits(final int count) {
        if (count < 0 || count > 32) {
            throw new IllegalArgumentException("count: " + count + " (expected: 0-32 )");
        }
        int bitCount = this.bitCount;
        long bitBuffer = this.bitBuffer;

        if (bitCount < count) {
            long readData;
            int offset;
            switch (in.readableBytes()) {
                case 1: {
                    readData = in.readUnsignedByte();
                    offset = 8;
                    break;
                }
                case 2: {
                    readData = in.readUnsignedShort();
                    offset = 16;
                    break;
                }
                case 3: {
                    readData = in.readUnsignedMedium();
                    offset = 24;
                    break;
                }
                default: {
                    readData = in.readUnsignedInt();
                    offset = 32;
                    break;
                }
            }

            bitBuffer = bitBuffer << offset | readData;
            bitCount += offset;
            this.bitBuffer = bitBuffer;
        }

        this.bitCount = bitCount -= count;
        return (int) (bitBuffer >>> bitCount & (count != 32 ? (1 << count) - 1 : 0xFFFFFFFFL));
    }

    /**
     * 读取单个位；为 1 时返回 {@code true}。
     * @return {@code true} if the bit read was {@code 1}, otherwise {@code false}
     */
    boolean readBoolean() {
        return readBits(1) != 0;
    }

    /**
     * 读取 32 位组成一个 int。
     * @return The integer read
     */
    int readInt() {
        return readBits(32);
    }

    /**
     * 从 {@link ByteBuf} 再读一字节补充位缓冲。
     */
    void refill() {
        int readData = in.readUnsignedByte();
        bitBuffer = bitBuffer << 8 | readData;
        bitCount += 8;
    }

    /**
     * 是否至少还有 1 位可读。
     * @return {@code true} if one bit is available for reading, otherwise {@code false}
     */
    boolean isReadable() {
        return bitCount > 0 || in.isReadable();
    }

    /**
     * 检查是否尚有 {@code count} 位可读。
     * @param count The number of bits to check
     * @return {@code true} if {@code count} bits are available for reading, otherwise {@code false}
     */
    boolean hasReadableBits(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count: " + count + " (expected value greater than 0)");
        }
        return bitCount >= count || (in.readableBytes() << 3 & Integer.MAX_VALUE) >= count - bitCount;
    }

    /**
     * 检查是否尚有 {@code count} 字节（换算为 bit）可读。
     * @param count The number of bytes to check
     * @return {@code true} if {@code count} bytes are available for reading, otherwise {@code false}
     */
    boolean hasReadableBytes(int count) {
        if (count < 0 || count > MAX_COUNT_OF_READABLE_BYTES) {
            throw new IllegalArgumentException("count: " + count
                    + " (expected: 0-" + MAX_COUNT_OF_READABLE_BYTES + ')');
        }
        return hasReadableBits(count << 3);
    }
}
