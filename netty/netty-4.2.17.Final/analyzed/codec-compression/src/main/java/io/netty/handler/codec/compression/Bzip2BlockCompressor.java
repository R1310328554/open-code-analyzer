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
import io.netty.util.ByteProcessor;

import static io.netty.handler.codec.compression.Bzip2Constants.BLOCK_HEADER_MAGIC_1;
import static io.netty.handler.codec.compression.Bzip2Constants.BLOCK_HEADER_MAGIC_2;
import static io.netty.handler.codec.compression.Bzip2Constants.HUFFMAN_SYMBOL_RANGE_SIZE;

/**
 * 压缩并写出单个 Bzip2 数据块。<br><br>
 *
 * 块编码包含以下阶段：<br>
 * 1. 游程编码[1] — {@link #write(int)}<br>
 * 2. Burrows-Wheeler 变换 — {@link #close(ByteBuf)}（经 {@link Bzip2DivSufSort}）<br>
 * 3. 写出块头 — {@link #close(ByteBuf)}<br>
 * 4. 移到前面（MTF）变换 — {@link #close(ByteBuf)}（经 {@link Bzip2HuffmanStageEncoder}）<br>
 * 5. 游程编码[2] — {@link #close(ByteBuf)}（经 {@link Bzip2HuffmanStageEncoder}）<br>
 * 6. 创建并写出 Huffman 表 — {@link #close(ByteBuf)}（经 {@link Bzip2HuffmanStageEncoder}）<br>
 * 7. Huffman 编码并写出数据 — {@link #close(ByteBuf)}（经 {@link Bzip2HuffmanStageEncoder}）
 */
final class Bzip2BlockCompressor {
    private final ByteProcessor writeProcessor = new ByteProcessor() {
        @Override
        public boolean process(byte value) throws Exception {
            return write(value);
        }
    };

    /** 提供位级写入的 {@link Bzip2BitWriter}。 */
    private final Bzip2BitWriter writer;

    /** 当前块的 CRC 计算器。 */
    private final Crc32 crc = new Crc32();

    /** 经游程编码[1]后的块数据。 */
    private final byte[] block;

    /** {@link #block} 数组中当前有效数据长度。 */
    private int blockLength;

    /** 块容量上限，超出后不再接受新数据。 */
    private final int blockLengthLimit;

    /**
     * The values that are present within the RLE'd block data. For each index, {@code true} if that
     * 值是否出现在数据中，否则为 {@code false}。
     */
    private final boolean[] blockValuesPresent = new boolean[256];

    /** BWT 变换后的块数据（后缀数组形式）。 */
    private final int[] bwtBlock;

    /** 当前正在累积游程的字节值（{@link #rleLength} 为 0 时未定义）。 */
    private int rleCurrentValue = -1;

    /** 当前游程值的重复次数。 */
    private int rleLength;

    /**
     * @param writer The {@link Bzip2BitWriter} which provides bit-level writes
     * @param blockSize The declared block size in bytes. Up to this many bytes will be accepted
     *                  into the block after Run-Length Encoding is applied
     */
    Bzip2BlockCompressor(final Bzip2BitWriter writer, final int blockSize) {
        this.writer = writer;

        // 多分配一字节，供 close() 中 BWT 所需的块环绕使用
        block = new byte[blockSize + 1];
        bwtBlock = new int[blockSize + 1];
        blockLengthLimit = blockSize - 6; // 5 bytes for one RLE run plus one byte - see {@link #write(int)}
    }

    /**
     * 将 Huffman 符号映射（哪些字节值出现）写入输出流。
     */
    private void writeSymbolMap(ByteBuf out) {
        Bzip2BitWriter writer = this.writer;

        final boolean[] blockValuesPresent = this.blockValuesPresent;
        final boolean[] condensedInUse = new boolean[16];

        for (int i = 0; i < condensedInUse.length; i++) {
            for (int j = 0, k = i << 4; j < HUFFMAN_SYMBOL_RANGE_SIZE; j++, k++) {
                if (blockValuesPresent[k]) {
                    condensedInUse[i] = true;
                    break;
                }
            }
        }

        for (boolean isCondensedInUse : condensedInUse) {
            writer.writeBoolean(out, isCondensedInUse);
        }

        for (int i = 0; i < condensedInUse.length; i++) {
            if (condensedInUse[i]) {
                for (int j = 0, k = i << 4; j < HUFFMAN_SYMBOL_RANGE_SIZE; j++, k++) {
                    writer.writeBoolean(out, blockValuesPresent[k]);
                }
            }
        }
    }

    /**
     * Writes an RLE run to the block array, updating the block CRC and present values array as required.
     * @param value 要写入的字节值
     * @param runLength 该值的游程长度
     */
    private void writeRun(final int value, int runLength) {
        final int blockLength = this.blockLength;
        final byte[] block = this.block;

        blockValuesPresent[value] = true;
        crc.updateCRC(value, runLength);

        final byte byteValue = (byte) value;
        switch (runLength) {
            case 1:
                block[blockLength] = byteValue;
                this.blockLength = blockLength + 1;
                break;
            case 2:
                block[blockLength] = byteValue;
                block[blockLength + 1] = byteValue;
                this.blockLength = blockLength + 2;
                break;
            case 3:
                block[blockLength] = byteValue;
                block[blockLength + 1] = byteValue;
                block[blockLength + 2] = byteValue;
                this.blockLength = blockLength + 3;
                break;
            default:
                runLength -= 4;
                blockValuesPresent[runLength] = true;
                block[blockLength] = byteValue;
                block[blockLength + 1] = byteValue;
                block[blockLength + 2] = byteValue;
                block[blockLength + 3] = byteValue;
                block[blockLength + 4] = (byte) runLength;
                this.blockLength = blockLength + 5;
                break;
        }
    }

    /**
     * Writes a byte to the block, accumulating to an RLE run where possible.
     * @param value The byte to write
     * @return {@code true} if the byte was written, or {@code false} if the block is already full
     */
    boolean write(final int value) {
        if (blockLength > blockLengthLimit) {
            return false;
        }
        final int rleCurrentValue = this.rleCurrentValue;
        final int rleLength = this.rleLength;

        if (rleLength == 0) {
            this.rleCurrentValue = value;
            this.rleLength = 1;
        } else if (rleCurrentValue != value) {
            // 切换游程值时至少需写 6 字节：一个 RLE 段（5 字节）加一个新字节
            writeRun(rleCurrentValue & 0xff, rleLength);
            this.rleCurrentValue = value;
            this.rleLength = 1;
        } else {
            if (rleLength == 254) {
                writeRun(rleCurrentValue & 0xff, 255);
                this.rleLength = 0;
            } else {
                this.rleLength = rleLength + 1;
            }
        }
        return true;
    }

    /**
     * Writes an array to the block.
     * @param buffer The buffer to write
     * @param offset The offset within the input data to write from
     * @param length The number of bytes of input data to write
     * @return The actual number of input bytes written. May be less than the number requested, or
     *         zero if the block is already full
     */
    int write(final ByteBuf buffer, int offset, int length) {
        int index = buffer.forEachByte(offset, length, writeProcessor);
        return index == -1 ? length : index - offset;
    }

    /**
     * Compresses and writes out the block.
     */
    void close(ByteBuf out) {
        // 若仍有未刷新的游程，先写出
        if (rleLength > 0) {
            writeRun(rleCurrentValue & 0xff, rleLength);
        }

        // BWT 算法要求块末尾追加首字节形成环绕
        block[blockLength] = block[0];

        // 执行 Burrows-Wheeler 变换
        Bzip2DivSufSort divSufSort = new Bzip2DivSufSort(block, bwtBlock, blockLength);
        int bwtStartPointer = divSufSort.bwt();

        Bzip2BitWriter writer = this.writer;

        // 写出块头（魔数、CRC、随机化标志、BWT 起始指针）
        writer.writeBits(out, 24, BLOCK_HEADER_MAGIC_1);
        writer.writeBits(out, 24, BLOCK_HEADER_MAGIC_2);
        writer.writeInt(out, crc.getCRC());
        writer.writeBoolean(out, false); // 随机化块标志；本实现不生成随机化块
        writer.writeBits(out, 24, bwtStartPointer);

        // 写出符号使用位图
        writeSymbolMap(out);

        // 执行 MTF 变换与游程编码[2]
        Bzip2MTFAndRLE2StageEncoder mtfEncoder = new Bzip2MTFAndRLE2StageEncoder(bwtBlock, blockLength,
                                                                                    blockValuesPresent);
        mtfEncoder.encode();

        // 执行 Huffman 编码并写出压缩数据
        Bzip2HuffmanStageEncoder huffmanEncoder = new Bzip2HuffmanStageEncoder(writer,
                mtfEncoder.mtfBlock(),
                mtfEncoder.mtfLength(),
                mtfEncoder.mtfAlphabetSize(),
                mtfEncoder.mtfSymbolFrequencies());
        huffmanEncoder.encode(out);
    }

    /**
     * Gets available size of the current block.
     * @return Number of available bytes which can be written
     */
    int availableSize() {
        if (blockLength == 0) {
            return blockLengthLimit + 2;
        }
        return blockLengthLimit - blockLength + 1;
    }

    /**
     * Determines if the block is full and ready for compression.
     * @return {@code true} if the block is full, otherwise {@code false}
     */
    boolean isFull() {
        return blockLength > blockLengthLimit;
    }

    /**
     * Determines if any bytes have been written to the block.
     * @return {@code true} if one or more bytes has been written to the block, otherwise {@code false}
     */
    boolean isEmpty() {
        return blockLength == 0 && rleLength == 0;
    }

    /**
     * Gets the CRC of the completed block. Only valid after calling {@link #close(ByteBuf)}.
     * @return The block's CRC
     */
    int crc() {
        return crc.getCRC();
    }
}
