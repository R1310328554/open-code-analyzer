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
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lzma.sdk.lzma.Base;
import lzma.sdk.lzma.Encoder;

import java.io.InputStream;

import static lzma.sdk.lzma.Encoder.EMatchFinderTypeBT4;

/**
 * 使用 LZMA 算法压缩 {@link ByteBuf}。
 *
 * 格式见 <a href="https://en.wikipedia.org/wiki/Lempel%E2%80%93Ziv%E2%80%93Markov_chain_algorithm">LZMA</a>
 * 与 7-Zip SDK 文档。
 */
public class LzmaFrameEncoder extends MessageToByteEncoder<ByteBuf> {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(LzmaFrameEncoder.class);

    private static final int MEDIUM_DICTIONARY_SIZE = 1 << 16;

    private static final int MIN_FAST_BYTES = 5;
    private static final int MEDIUM_FAST_BYTES = 0x20;
    private static final int MAX_FAST_BYTES = Base.kMatchMaxLen;

    private static final int DEFAULT_MATCH_FINDER = EMatchFinderTypeBT4;

    private static final int DEFAULT_LC = 3;
    private static final int DEFAULT_LP = 0;
    private static final int DEFAULT_PB = 2;

    /** 底层 LZMA 编码器。 */
    private final Encoder encoder;

    /** Properties 字节：{@code (pb * 5 + lp) * 9 + lc}，含字面上下文位 lc、
     *  字面位置位 lp 与位置位 pb。 */
    private final byte properties;

    /** 字典大小，以小端 32 位无符号整数存储。 */
    private final int littleEndianDictionarySize;

    /** 仅记录一次兼容性警告。 */
    private static boolean warningLogged;

    /**
     * 使用默认参数创建 LZMA 编码器。
     */
    public LzmaFrameEncoder() {
        this(MEDIUM_DICTIONARY_SIZE);
    }

    /**
     * Creates LZMA encoder with specified {@code lc}, {@code lp}, {@code pb}
     * values and the medium dictionary size of {@value #MEDIUM_DICTIONARY_SIZE}.
     */
    public LzmaFrameEncoder(int lc, int lp, int pb) {
        this(lc, lp, pb, MEDIUM_DICTIONARY_SIZE);
    }

    /**
     * Creates LZMA encoder with specified dictionary size and default values of
     * {@code lc} = {@value #DEFAULT_LC},
     * {@code lp} = {@value #DEFAULT_LP},
     * {@code pb} = {@value #DEFAULT_PB}.
     */
    public LzmaFrameEncoder(int dictionarySize) {
        this(DEFAULT_LC, DEFAULT_LP, DEFAULT_PB, dictionarySize);
    }

    /**
     * Creates LZMA encoder with specified {@code lc}, {@code lp}, {@code pb} values and custom dictionary size.
     */
    public LzmaFrameEncoder(int lc, int lp, int pb, int dictionarySize) {
        this(lc, lp, pb, dictionarySize, false, MEDIUM_FAST_BYTES);
    }

    /**
     * Creates LZMA encoder with specified settings.
     *
     * @param lc
     *        the number of "literal context" bits, available values [0, 8], default value {@value #DEFAULT_LC}.
     * @param lp
     *        the number of "literal position" bits, available values [0, 4], default value {@value #DEFAULT_LP}.
     * @param pb
     *        the number of "position" bits, available values [0, 4], default value {@value #DEFAULT_PB}.
     * @param dictionarySize
     *        available values [0, {@link java.lang.Integer#MAX_VALUE}],
     *        default value is {@value #MEDIUM_DICTIONARY_SIZE}.
     * @param endMarkerMode 是否写入流结束标记；帧头已含未压缩长度，通常不需要 EOS。
     * @param numFastBytes
     *        available values [{@value #MIN_FAST_BYTES}, {@value #MAX_FAST_BYTES}].
     */
    public LzmaFrameEncoder(int lc, int lp, int pb, int dictionarySize, boolean endMarkerMode, int numFastBytes) {
        super(ByteBuf.class);
        if (lc < 0 || lc > 8) {
            throw new IllegalArgumentException("lc: " + lc + " (expected: 0-8)");
        }
        if (lp < 0 || lp > 4) {
            throw new IllegalArgumentException("lp: " + lp + " (expected: 0-4)");
        }
        if (pb < 0 || pb > 4) {
            throw new IllegalArgumentException("pb: " + pb + " (expected: 0-4)");
        }
        if (lc + lp > 4) {
            if (!warningLogged) {
                logger.warn("The latest versions of LZMA libraries (for example, XZ Utils) " +
                        "has an additional requirement: lc + lp <= 4. Data which don't follow " +
                        "this requirement cannot be decompressed with this libraries.");
                warningLogged = true;
            }
        }
        if (dictionarySize < 0) {
            throw new IllegalArgumentException("dictionarySize: " + dictionarySize + " (expected: 0+)");
        }
        if (numFastBytes < MIN_FAST_BYTES || numFastBytes > MAX_FAST_BYTES) {
            throw new IllegalArgumentException(String.format(
                    "numFastBytes: %d (expected: %d-%d)", numFastBytes, MIN_FAST_BYTES, MAX_FAST_BYTES
            ));
        }

        encoder = new Encoder();
        encoder.setDictionarySize(dictionarySize);
        encoder.setEndMarkerMode(endMarkerMode);
        encoder.setMatchFinder(DEFAULT_MATCH_FINDER);
        encoder.setNumFastBytes(numFastBytes);
        encoder.setLcLpPb(lc, lp, pb);

        properties = (byte) ((pb * 5 + lp) * 9 + lc);
        littleEndianDictionarySize = Integer.reverseBytes(dictionarySize);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        final int length = in.readableBytes();
        try (InputStream bbIn = new ByteBufInputStream(in);
             ByteBufOutputStream bbOut = new ByteBufOutputStream(out)) {
            bbOut.writeByte(properties);
            bbOut.writeInt(littleEndianDictionarySize);
            bbOut.writeLong(Long.reverseBytes(length));
            encoder.code(bbIn, bbOut, -1, -1, null);
        }
    }

    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf in, boolean preferDirect) throws Exception {
        final int length = in.readableBytes();
        final int maxOutputLength = maxOutputBufferLength(length);
        return ctx.alloc().ioBuffer(maxOutputLength);
    }

    /**
     * 估算不可压缩数据时的最大输出缓冲尺寸。
     */
    private static int maxOutputBufferLength(int inputLength) {
        double factor;
        if (inputLength < 200) {
            factor = 1.5;
        } else if (inputLength < 500) {
            factor = 1.2;
        } else if (inputLength < 1000) {
            factor = 1.1;
        } else if (inputLength < 10000) {
            factor = 1.05;
        } else {
            factor = 1.02;
        }
        return 13 + (int) (inputLength * factor);
    }
}
