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

import io.netty.buffer.ByteBuf;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.MathUtil;
import io.netty.util.internal.SystemPropertyUtil;

import java.util.Arrays;

/**
 * Snappy 压缩/解压缩核心实现，在输入/输出 {@link ByteBuf} 间编解码。
 * <p>
 * 格式见 <a href="https://github.com/google/snappy/blob/master/format_description.txt">Snappy 规范</a>。
 */
public final class Snappy {

    private static final int MAX_HT_SIZE = 1 << 14;
    private static final int MIN_COMPRESSIBLE_BYTES = 15;

    // 表示序言（长度 varint）尚未读完整
    private static final int PREAMBLE_NOT_FULL = -1;
    private static final int NOT_ENOUGH_INPUT = -1;

    // 标签类型常量
    private static final int LITERAL = 0;
    private static final int COPY_1_BYTE_OFFSET = 1;
    private static final int COPY_2_BYTE_OFFSET = 2;
    private static final int COPY_4_BYTE_OFFSET = 3;

    // 压缩用哈希表，可在多次 encode 间复用
    private static final FastThreadLocal<short[]> HASH_TABLE = new FastThreadLocal<short[]>();

    private static final boolean DEFAULT_REUSE_HASHTABLE =
            SystemPropertyUtil.getBoolean("io.netty.handler.codec.compression.snappy.reuseHashTable", false);

    public Snappy() {
        this(DEFAULT_REUSE_HASHTABLE);
    }

    Snappy(boolean reuseHashtable) {
        this.reuseHashtable = reuseHashtable;
    }

    public static Snappy withHashTableReuse() {
        return new Snappy(true);
    }

    private final boolean reuseHashtable;
    private State state = State.READING_PREAMBLE;
    private byte tag;
    private int written;

    private enum State {
        READING_PREAMBLE,
        READING_TAG,
        READING_LITERAL,
        READING_COPY
    }

    public void reset() {
        state = State.READING_PREAMBLE;
        tag = 0;
        written = 0;
    }

    public void encode(final ByteBuf in, final ByteBuf out, final int length) {
        // 将未压缩长度 varint 写入输出
        for (int i = 0;; i ++) {
            int b = length >>> i * 7;
            if ((b & 0xFFFFFF80) != 0) {
                out.writeByte(b & 0x7f | 0x80);
            } else {
                out.writeByte(b);
                break;
            }
        }

        int inIndex = in.readerIndex();
        final int baseIndex = inIndex;

        int hashTableSize = MathUtil.findNextPositivePowerOfTwo(length);
        hashTableSize = Math.min(hashTableSize, MAX_HT_SIZE);
        final short[] table = getHashTable(hashTableSize);
        final int shift = Integer.numberOfLeadingZeros(hashTableSize) + 1;

        int nextEmit = inIndex;

        if (length - inIndex >= MIN_COMPRESSIBLE_BYTES) {
            int nextHash = hash(in, ++inIndex, shift);
            outer: while (true) {
                int skip = 32;

                int candidate;
                int nextIndex = inIndex;
                do {
                    inIndex = nextIndex;
                    int hash = nextHash;
                    int bytesBetweenHashLookups = skip++ >> 5;
                    nextIndex = inIndex + bytesBetweenHashLookups;

                    // 至少需 4 字节才能计算哈希
                    if (nextIndex > length - 4) {
                        break outer;
                    }

                    nextHash = hash(in, nextIndex, shift);

                    // 等价于无符号 short；避免大偏移时精度丢失
                    candidate = baseIndex + (table[hash] & 0xffff);

                    table[hash] = (short) (inIndex - baseIndex);
                }
                while (in.getInt(inIndex) != in.getInt(candidate));

                encodeLiteral(in, out, inIndex - nextEmit);

                int insertTail;
                do {
                    int base = inIndex;
                    int matched = 4 + findMatchingLength(in, candidate + 4, inIndex + 4, length);
                    inIndex += matched;
                    int offset = base - candidate;
                    encodeCopy(out, offset, matched);
                    in.readerIndex(in.readerIndex() + matched);
                    insertTail = inIndex - 1;
                    nextEmit = inIndex;
                    if (inIndex >= length - 4) {
                        break outer;
                    }

                    int prevHash = hash(in, insertTail, shift);
                    table[prevHash] = (short) (inIndex - baseIndex - 1);
                    int currentHash = hash(in, insertTail + 1, shift);
                    candidate = baseIndex + (table[currentHash] & 0xFFFF);
                    table[currentHash] = (short) (inIndex - baseIndex);
                }
                while (in.getInt(insertTail + 1) == in.getInt(candidate));

                nextHash = hash(in, insertTail + 2, shift);
                ++inIndex;
            }
        }

        // 剩余字节作为 literal 写出
        if (nextEmit < length) {
            encodeLiteral(in, out, length - nextEmit);
        }
    }

    /**
     * 对 index 处 4 字节求哈希并映射到哈希表范围。
     *
     * @param in 输入缓冲
     * @param index 起始索引
     * @param shift 右移量，使结果落在表大小内
     * @return 32 位哈希值
     */
    private static int hash(ByteBuf in, int index, int shift) {
        return in.getInt(index) * 0x1e35a7bd >>> shift;
    }

    /**
     * 获取用于压缩的 short[] 哈希表。
     *
     * @param hashTableSize 表大小
     * @return 适当大小的空表
     */
    private short[] getHashTable(int hashTableSize) {
        if (reuseHashtable) {
            return getHashTableFastThreadLocalArrayFill(hashTableSize);
        }
        return new short[hashTableSize];
    }

    /**
     * 从 FastThreadLocal 获取哈希表，必要时扩容并清零。
     *
     * @param hashTableSize 所需表大小
     * @return 空哈希表
     */
    public static short[] getHashTableFastThreadLocalArrayFill(int hashTableSize) {
        short[] hashTable = HASH_TABLE.get();
        if (hashTable == null || hashTable.length < hashTableSize) {
            hashTable = new short[hashTableSize];
            HASH_TABLE.set(hashTable);
            return hashTable;
        }

        Arrays.fill(hashTable, 0, hashTableSize, (short) 0);
        return hashTable;
    }

    /**
     * 在输入缓冲中扫描，计算与已写入数据的匹配重复长度。
     *
     * @param in 输入缓冲
     * @param minIndex 候选匹配起点
     * @param inIndex 当前输入位置
     * @param maxIndex 输入上界
     * @return 匹配字节数
     */
    private static int findMatchingLength(ByteBuf in, int minIndex, int inIndex, int maxIndex) {
        int matched = 0;

        while (inIndex <= maxIndex - 4 &&
                in.getInt(inIndex) == in.getInt(minIndex + matched)) {
            inIndex += 4;
            matched += 4;
        }

        while (inIndex < maxIndex && in.getByte(minIndex + matched) == in.getByte(inIndex)) {
            ++inIndex;
            ++matched;
        }

        return matched;
    }

    /**
     * 计算编码某值所需的最小比特数，用于确定长度字段字节数。
     *
     * @param value 待编码数值
     * @return 所需最小比特数
     */
    private static int bitsToEncode(int value) {
        int highestOneBit = Integer.highestOneBit(value);
        int bitLength = 0;
        while ((highestOneBit >>= 1) != 0) {
            bitLength++;
        }

        return bitLength;
    }

    /**
     * 将输入中一段原始字节作为 literal 写入输出。
     *
     * @param in 源缓冲
     * @param out 目标缓冲
     * @param length literal 长度
     */
    static void encodeLiteral(ByteBuf in, ByteBuf out, int length) {
        if (length < 61) {
            out.writeByte(length - 1 << 2);
        } else {
            int bitLength = bitsToEncode(length - 1);
            int bytesToEncode = 1 + bitLength / 8;
            out.writeByte(59 + bytesToEncode << 2);
            for (int i = 0; i < bytesToEncode; i++) {
                out.writeByte(length - 1 >> i * 8 & 0x0ff);
            }
        }

        out.writeBytes(in, length);
    }

    private static void encodeCopyWithOffset(ByteBuf out, int offset, int length) {
        if (length < 12 && offset < 2048) {
            out.writeByte(COPY_1_BYTE_OFFSET | length - 4 << 2 | offset >> 8 << 5);
            out.writeByte(offset & 0x0ff);
        } else {
            out.writeByte(COPY_2_BYTE_OFFSET | length - 1 << 2);
            out.writeByte(offset & 0x0ff);
            out.writeByte(offset >> 8 & 0x0ff);
        }
    }

    /**
     * 编码一系列 backward copy，每段最多 64 字节。
     *
     * @param out 输出缓冲
     * @param offset 回指偏移
     * @param length 复制长度
     */
    private static void encodeCopy(ByteBuf out, int offset, int length) {
        while (length >= 68) {
            encodeCopyWithOffset(out, offset, 64);
            length -= 64;
        }

        if (length > 64) {
            encodeCopyWithOffset(out, offset, 60);
            length -= 60;
        }

        encodeCopyWithOffset(out, offset, length);
    }

    public void decode(ByteBuf in, ByteBuf out) {
        while (in.isReadable()) {
            switch (state) {
            case READING_PREAMBLE:
                int uncompressedLength = readPreamble(in);
                if (uncompressedLength == PREAMBLE_NOT_FULL) {
                    // 序言未读完，等待更多输入
                    return;
                }
                if (uncompressedLength == 0) {
                    // 不应出现；表示无后续数据
                    return;
                }
                out.ensureWritable(uncompressedLength);
                state = State.READING_TAG;
                // fall through
            case READING_TAG:
                if (!in.isReadable()) {
                    return;
                }
                tag = in.readByte();
                switch (tag & 0x03) {
                case LITERAL:
                    state = State.READING_LITERAL;
                    break;
                case COPY_1_BYTE_OFFSET:
                case COPY_2_BYTE_OFFSET:
                case COPY_4_BYTE_OFFSET:
                    state = State.READING_COPY;
                    break;
                }
                break;
            case READING_LITERAL:
                int literalWritten = decodeLiteral(tag, in, out);
                if (literalWritten != NOT_ENOUGH_INPUT) {
                    state = State.READING_TAG;
                    written += literalWritten;
                } else {
                    // 等待更多输入数据
                    return;
                }
                break;
            case READING_COPY:
                int decodeWritten;
                switch (tag & 0x03) {
                case COPY_1_BYTE_OFFSET:
                    decodeWritten = decodeCopyWith1ByteOffset(tag, in, out, written);
                    if (decodeWritten != NOT_ENOUGH_INPUT) {
                        state = State.READING_TAG;
                        written += decodeWritten;
                    } else {
                        // Need to wait for more data
                        return;
                    }
                    break;
                case COPY_2_BYTE_OFFSET:
                    decodeWritten = decodeCopyWith2ByteOffset(tag, in, out, written);
                    if (decodeWritten != NOT_ENOUGH_INPUT) {
                        state = State.READING_TAG;
                        written += decodeWritten;
                    } else {
                        // Need to wait for more data
                        return;
                    }
                    break;
                case COPY_4_BYTE_OFFSET:
                    decodeWritten = decodeCopyWith4ByteOffset(tag, in, out, written);
                    if (decodeWritten != NOT_ENOUGH_INPUT) {
                        state = State.READING_TAG;
                        written += decodeWritten;
                    } else {
                        // Need to wait for more data
                        return;
                    }
                    break;
                }
            }
        }
    }

    /**
     * 读取长度 varint（低 7 位为数据，最高位表示后续还有字节）。
     *
     * @param in 输入缓冲
     * @return 解析出的长度；数据不足时返回 0
     */
    private static int readPreamble(ByteBuf in) {
        int length = 0;
        int byteIndex = 0;
        while (in.isReadable()) {
            int current = in.readUnsignedByte();
            length |= (current & 0x7f) << byteIndex++ * 7;
            if ((current & 0x80) == 0) {
                return length;
            }

            if (byteIndex >= 4) {
                throw new DecompressionException("Preamble is greater than 4 bytes");
            }
        }

        return 0;
    }

    /**
     * 在不推进 readerIndex 的情况下预览序言长度 varint。
     *
     * @param in 输入缓冲
     * @return 解析长度；无法完整读取时返回 0
     */
    int getPreamble(ByteBuf in) {
        if (state == State.READING_PREAMBLE) {
            int readerIndex = in.readerIndex();
            try {
                return readPreamble(in);
            } finally {
                in.readerIndex(readerIndex);
            }
        }
        return 0;
    }

    /**
     * 从输入读取 literal（未压缩原始段）并写入输出。
     *
     * @param tag 标识 literal 的标签，亦编码部分长度
     * @param in 输入缓冲
     * @param out 输出缓冲
     * @return 写入字节数，或 -1 表示需更多输入
     */
    static int decodeLiteral(byte tag, ByteBuf in, ByteBuf out) {
        in.markReaderIndex();
        int length;
        switch(tag >> 2 & 0x3F) {
        case 60:
            if (!in.isReadable()) {
                return NOT_ENOUGH_INPUT;
            }
            length = in.readUnsignedByte();
            break;
        case 61:
            if (in.readableBytes() < 2) {
                return NOT_ENOUGH_INPUT;
            }
            length = in.readUnsignedShortLE();
            break;
        case 62:
            if (in.readableBytes() < 3) {
                return NOT_ENOUGH_INPUT;
            }
            length = in.readUnsignedMediumLE();
            break;
        case 63:
            if (in.readableBytes() < 4) {
                return NOT_ENOUGH_INPUT;
            }
            length = in.readIntLE();
            break;
        default:
            length = tag >> 2 & 0x3F;
        }
        length += 1;

        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return NOT_ENOUGH_INPUT;
        }

        out.writeBytes(in, length);
        return length;
    }

    /**
     * 解码 1 字节偏移的 copy 指令，回指复制到输出。
     *
     * @param tag 标签（含部分长度与偏移）
     * @param in 输入
     * @param out 输出
     * @return 写入字节数或 -1
     * @throws DecompressionException 偏移非法时
     */
    private static int decodeCopyWith1ByteOffset(byte tag, ByteBuf in, ByteBuf out, int writtenSoFar) {
        if (!in.isReadable()) {
            return NOT_ENOUGH_INPUT;
        }

        int initialIndex = out.writerIndex();
        int length = 4 + ((tag & 0x01c) >> 2);
        int offset = (tag & 0x0e0) << 8 >> 5 | in.readUnsignedByte();

        validateOffset(offset, writtenSoFar);

        out.markReaderIndex();
        if (offset < length) {
            int copies = length / offset;
            for (; copies > 0; copies--) {
                out.readerIndex(initialIndex - offset);
                out.readBytes(out, offset);
            }
            if (length % offset != 0) {
                out.readerIndex(initialIndex - offset);
                out.readBytes(out, length % offset);
            }
        } else {
            out.readerIndex(initialIndex - offset);
            out.readBytes(out, length);
        }
        out.resetReaderIndex();

        return length;
    }

    /**
     * Reads a compressed reference offset and length from the supplied input
     * buffer, seeks back to the appropriate place in the input buffer and
     * writes the found data to the supplied output stream.
     *
     * @param tag The tag used to identify this as a copy is also used to encode
     *     the length and part of the offset
     * @param in The input buffer to read from
     * @param out The output buffer to write to
     * @throws DecompressionException 偏移非法时
     * @return 写入字节数或 -1
     */
    private static int decodeCopyWith2ByteOffset(byte tag, ByteBuf in, ByteBuf out, int writtenSoFar) {
        if (in.readableBytes() < 2) {
            return NOT_ENOUGH_INPUT;
        }

        int initialIndex = out.writerIndex();
        int length = 1 + (tag >> 2 & 0x03f);
        int offset = in.readUnsignedShortLE();

        validateOffset(offset, writtenSoFar);

        out.markReaderIndex();
        if (offset < length) {
            int copies = length / offset;
            for (; copies > 0; copies--) {
                out.readerIndex(initialIndex - offset);
                out.readBytes(out, offset);
            }
            if (length % offset != 0) {
                out.readerIndex(initialIndex - offset);
                out.readBytes(out, length % offset);
            }
        } else {
            out.readerIndex(initialIndex - offset);
            out.readBytes(out, length);
        }
        out.resetReaderIndex();

        return length;
    }

    /**
     * Reads a compressed reference offset and length from the supplied input
     * buffer, seeks back to the appropriate place in the input buffer and
     * writes the found data to the supplied output stream.
     *
     * @param tag The tag used to identify this as a copy is also used to encode
     *     the length and part of the offset
     * @param in The input buffer to read from
     * @param out The output buffer to write to
     * @return The number of bytes appended to the output buffer, or -1 to indicate
     *     "try again later"
     * @throws DecompressionException If the read offset is invalid
     */
    private static int decodeCopyWith4ByteOffset(byte tag, ByteBuf in, ByteBuf out, int writtenSoFar) {
        if (in.readableBytes() < 4) {
            return NOT_ENOUGH_INPUT;
        }

        int initialIndex = out.writerIndex();
        int length = 1 + (tag >> 2 & 0x03F);
        int offset = in.readIntLE();

        validateOffset(offset, writtenSoFar);

        out.markReaderIndex();
        if (offset < length) {
            int copies = length / offset;
            for (; copies > 0; copies--) {
                out.readerIndex(initialIndex - offset);
                out.readBytes(out, offset);
            }
            if (length % offset != 0) {
                out.readerIndex(initialIndex - offset);
                out.readBytes(out, length % offset);
            }
        } else {
            out.readerIndex(initialIndex - offset);
            out.readBytes(out, length);
        }
        out.resetReaderIndex();

        return length;
    }

    /**
     * 校验 copy 偏移：须 0 &lt; offset &lt; MAX_VALUE 且不超过已解压长度。
     *
     * @param offset 回指偏移
     * @param chunkSizeSoFar 当前块已输出字节数
     * @throws DecompressionException 偏移非法
     */
    private static void validateOffset(int offset, int chunkSizeSoFar) {
        if (offset == 0) {
            throw new DecompressionException("Offset is less than minimum permissible value");
        }

        if (offset < 0) {
            // 算术溢出导致为负
            throw new DecompressionException("Offset is greater than maximum value supported by this implementation");
        }

        if (offset > chunkSizeSoFar) {
            throw new DecompressionException("Offset exceeds size of chunk");
        }
    }

    /**
     * 计算 CRC32C 并对结果做 Snappy 规定的 mask 变换。
     *
     * @param data 待校验数据
     */
    static int calculateChecksum(ByteBuf data) {
        return calculateChecksum(data, data.readerIndex(), data.readableBytes());
    }

    /**
     * Computes the CRC32C checksum of the supplied data and performs the "mask" operation
     * on the computed checksum
     *
     * @param data The input data to calculate the CRC32C checksum of
     */
    static int calculateChecksum(ByteBuf data, int offset, int length) {
        Crc32c crc32 = new Crc32c();
        try {
            crc32.update(data, offset, length);
            return maskChecksum(crc32.getValue());
        } finally {
            crc32.reset();
        }
    }

    /**
     * 计算 mask 后的 CRC32C 并与期望值比较。
     *
     * @param expectedChecksum 流中的期望校验和
     * @param data 待校验数据
     * @throws DecompressionException 不一致时
     */
    static void validateChecksum(int expectedChecksum, ByteBuf data) {
        validateChecksum(expectedChecksum, data, data.readerIndex(), data.readableBytes());
    }

    /**
     * Computes the CRC32C checksum of the supplied data, performs the "mask" operation
     * on the computed checksum, and then compares the resulting masked checksum to the
     * supplied checksum.
     *
     * @param expectedChecksum The checksum decoded from the stream to compare against
     * @param data The input data to calculate the CRC32C checksum of
     * @throws DecompressionException If the calculated and supplied checksums do not match
     */
    static void validateChecksum(int expectedChecksum, ByteBuf data, int offset, int length) {
        final int actualChecksum = calculateChecksum(data, offset, length);
        if (actualChecksum != expectedChecksum) {
            throw new DecompressionException(
                    "mismatching checksum: " + Integer.toHexString(actualChecksum) +
                            " (expected: " + Integer.toHexString(expectedChecksum) + ')');
        }
    }

    /**
     * 按规范对 CRC 做 mask：循环左移 15 位后加常数 0xa282ead8（无符号环绕）。
     *
     * @param checksum 原始 CRC 值
     * @return mask 后的校验和
     */
    static int maskChecksum(long checksum) {
        return (int) ((checksum >> 15 | checksum << 17) + 0xa282ead8);
    }
}
