/*
 * Copyright 2013 The Netty Project
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
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.ObjectUtil;

import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * 使用 JDK {@link java.util.zip.Inflater} 对 {@link ByteBuf} 进行 inflate 解压缩。
 */
public class JdkZlibDecoder extends ZlibDecoder {
    private static final int FHCRC = 0x02;
    private static final int FEXTRA = 0x04;
    private static final int FNAME = 0x08;
    private static final int FCOMMENT = 0x10;
    private static final int FRESERVED = 0xE0;

    private Inflater inflater;
    private final byte[] dictionary;

    // GZIP 相关字段
    private final ByteBufChecksum crc;
    private final boolean decompressConcatenated;

    private enum GzipState {
        HEADER_START,
        HEADER_END,
        FLG_READ,
        XLEN_READ,
        SKIP_FNAME,
        SKIP_COMMENT,
        PROCESS_FHCRC,
        FOOTER_START,
    }

    private GzipState gzipState = GzipState.HEADER_START;
    private int flags = -1;
    private int xlen = -1;
    private boolean needsRead;

    private static final int DEFAULT_MAX_FORWARD_BYTES = CompressionUtil.DEFAULT_MAX_FORWARD_BYTES;
    private final int maxForwardBytes;

    private volatile boolean finished;

    private boolean decideZlibOrNone;

    /**
     * Creates a new instance with the default wrapper ({@link ZlibWrapper#ZLIB}).
     *
     * @deprecated Use {@link JdkZlibDecoder#JdkZlibDecoder(int)}.
     */
    @Deprecated
    public JdkZlibDecoder() {
        this(ZlibWrapper.ZLIB, null, false, 0);
    }

    /**
     * Creates a new instance with the default wrapper ({@link ZlibWrapper#ZLIB})
     * and the specified maximum buffer allocation.
     *
     * @param maxAllocation
     *          解压缩缓冲最大尺寸，须 &gt;= 0；为 0 时由 {@link ByteBufAllocator} 决定。
     */
    public JdkZlibDecoder(int maxAllocation) {
        this(ZlibWrapper.ZLIB, null, false, maxAllocation);
    }

    /**
     * Creates a new instance with the specified preset dictionary. The wrapper
     * is always {@link ZlibWrapper#ZLIB} because it is the only format that
     * supports the preset dictionary.
     *
     * @deprecated Use {@link JdkZlibDecoder#JdkZlibDecoder(byte[], int)}.
     */
    @Deprecated
    public JdkZlibDecoder(byte[] dictionary) {
        this(ZlibWrapper.ZLIB, dictionary, false, 0);
    }

    /**
     * Creates a new instance with the specified preset dictionary and maximum buffer allocation.
     * The wrapper is always {@link ZlibWrapper#ZLIB} because it is the only format that
     * supports the preset dictionary.
     *
     * @param maxAllocation
     *          Maximum size of the decompression buffer. Must be &gt;= 0.
     *          If zero, maximum size is decided by the {@link ByteBufAllocator}.
     */
    public JdkZlibDecoder(byte[] dictionary, int maxAllocation) {
        this(ZlibWrapper.ZLIB, dictionary, false, maxAllocation);
    }

    /**
     * Creates a new instance with the specified wrapper.
     * 目前仅支持 {@link ZlibWrapper#GZIP}、{@link ZlibWrapper#ZLIB} 与 {@link ZlibWrapper#NONE}。
     *
     * @deprecated Use {@link JdkZlibDecoder#JdkZlibDecoder(ZlibWrapper, int)}.
     */
    @Deprecated
    public JdkZlibDecoder(ZlibWrapper wrapper) {
        this(wrapper, null, false, 0);
    }

    /**
     * Creates a new instance with the specified wrapper and maximum buffer allocation.
     * Be aware that only {@link ZlibWrapper#GZIP}, {@link ZlibWrapper#ZLIB} and {@link ZlibWrapper#NONE} are
     * supported atm.
     *
     * @param maxAllocation
     *          Maximum size of the decompression buffer. Must be &gt;= 0.
     *          If zero, maximum size is decided by the {@link ByteBufAllocator}.
     */
    public JdkZlibDecoder(ZlibWrapper wrapper, int maxAllocation) {
        this(wrapper, null, false, maxAllocation);
    }

    /**
     * @deprecated Use {@link JdkZlibDecoder#JdkZlibDecoder(ZlibWrapper, boolean, int)}.
     */
    @Deprecated
    public JdkZlibDecoder(ZlibWrapper wrapper, boolean decompressConcatenated) {
        this(wrapper, null, decompressConcatenated, 0);
    }

    public JdkZlibDecoder(ZlibWrapper wrapper, boolean decompressConcatenated, int maxAllocation) {
        this(wrapper, null, decompressConcatenated, maxAllocation);
    }

    /**
     * @deprecated Use {@link JdkZlibDecoder#JdkZlibDecoder(boolean, int)}.
     */
    @Deprecated
    public JdkZlibDecoder(boolean decompressConcatenated) {
        this(ZlibWrapper.GZIP, null, decompressConcatenated, 0);
    }

    public JdkZlibDecoder(boolean decompressConcatenated, int maxAllocation) {
        this(ZlibWrapper.GZIP, null, decompressConcatenated, maxAllocation);
    }

    private JdkZlibDecoder(ZlibWrapper wrapper, byte[] dictionary, boolean decompressConcatenated, int maxAllocation) {
        super(maxAllocation);
        this.maxForwardBytes = maxAllocation > 0 ? maxAllocation : DEFAULT_MAX_FORWARD_BYTES;

        ObjectUtil.checkNotNull(wrapper, "wrapper");

        this.decompressConcatenated = decompressConcatenated;
        switch (wrapper) {
            case GZIP:
                inflater = new Inflater(true);
                crc = ByteBufChecksum.wrapChecksum(new CRC32());
                break;
            case NONE:
                inflater = new Inflater(true);
                crc = null;
                break;
            case ZLIB:
                inflater = new Inflater();
                crc = null;
                break;
            case ZLIB_OR_NONE:
                // 推迟到 decode(...) 调用时再判定 ZLIB 或 NONE
                decideZlibOrNone = true;
                crc = null;
                break;
            default:
                throw new IllegalArgumentException("Only GZIP or ZLIB is supported, but you used " + wrapper);
        }
        this.dictionary = dictionary;
    }

    @Override
    public boolean isClosed() {
        return finished;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        needsRead = true;
        if (finished) {
            // 已完成时跳过后续数据
            in.skipBytes(in.readableBytes());
            return;
        }

        int readableBytes = in.readableBytes();
        if (readableBytes == 0) {
            return;
        }

        if (decideZlibOrNone) {
            // 需至少两字节判断是否为 ZLIB 流
            if (readableBytes < 2) {
                return;
            }

            boolean nowrap = !looksLikeZlib(in.getShort(in.readerIndex()));
            inflater = new Inflater(nowrap);
            decideZlibOrNone = false;
        }

        if (crc != null) {
            if (gzipState != GzipState.HEADER_END) {
                if (gzipState == GzipState.FOOTER_START) {
                    if (!handleGzipFooter(in)) {
                        // Either there was not enough data or the input is finished.
                        return;
                    }
                    // If we consumed the footer we will start with the header again.
                    assert gzipState == GzipState.HEADER_START;
                }
                if (!readGZIPHeader(in)) {
                    // There was not enough data readable to read the GZIP header.
                    return;
                }
                // Some bytes may have been consumed, and so we must re-set the number of readable bytes.
                readableBytes = in.readableBytes();
                if (readableBytes == 0) {
                    return;
                }
            }
        }

        if (inflater.needsInput()) {
            if (in.hasArray()) {
                inflater.setInput(in.array(), in.arrayOffset() + in.readerIndex(), readableBytes);
            } else {
                byte[] array = new byte[readableBytes];
                in.getBytes(in.readerIndex(), array);
                inflater.setInput(array);
            }
        }

        ByteBuf decompressed = prepareDecompressBuffer(ctx, null, inflater.getRemaining() << 1);
        try {
            boolean readFooter = false;
            while (!inflater.needsInput()) {
                byte[] outArray = decompressed.array();
                int writerIndex = decompressed.writerIndex();
                int outIndex = decompressed.arrayOffset() + writerIndex;
                int writable = decompressed.writableBytes();
                int outputLength = inflater.inflate(outArray, outIndex, writable);
                if (outputLength > 0) {
                    decompressed.writerIndex(writerIndex + outputLength);
                    if (crc != null) {
                        crc.update(outArray, outIndex, outputLength);
                    }
                    if (maxAllocation == 0 && decompressed.readableBytes() >= maxForwardBytes) {
                        // 超过阈值即转发，限制内存并减少 fireChannelRead 次数
                        ByteBuf buffer = decompressed;
                        decompressed = null;
                        needsRead = false;
                        ctx.fireChannelRead(buffer);
                    }
                } else if (inflater.needsDictionary()) {
                    if (dictionary == null) {
                        throw new DecompressionException(
                                "decompression failure, unable to set dictionary as non was specified");
                    }
                    inflater.setDictionary(dictionary);
                }

                if (inflater.finished()) {
                    if (crc == null) {
                        finished = true; // 不再解码
                    } else {
                        readFooter = true;
                    }
                    break;
                } else {
                    decompressed = prepareDecompressBuffer(ctx, decompressed, inflater.getRemaining() << 1);
                }
            }

            in.skipBytes(readableBytes - inflater.getRemaining());

            if (readFooter) {
                gzipState = GzipState.FOOTER_START;
                handleGzipFooter(in);
            }
        } catch (DataFormatException e) {
            throw new DecompressionException("decompression failure", e);
        } finally {
            if (decompressed != null) {
                if (decompressed.isReadable()) {
                    needsRead = false;
                    ctx.fireChannelRead(decompressed);
                } else {
                    decompressed.release();
                }
            }
        }
    }

    private boolean handleGzipFooter(ByteBuf in) {
        if (readGZIPFooter(in)) {
            finished = !decompressConcatenated;

            if (!finished) {
                inflater.reset();
                crc.reset();
                xlen = -1;
                gzipState = GzipState.HEADER_START;
                return true;
            }
        }
        return false;
    }

    @Override
    protected void decompressionBufferExhausted(ByteBuf buffer) {
        finished = true;
    }

    @Override
    protected void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved0(ctx);
        if (inflater != null) {
            inflater.end();
        }
    }

    private boolean readGZIPHeader(ByteBuf in) {
        switch (gzipState) {
            case HEADER_START:
                if (in.readableBytes() < 10) {
                    return false;
                }
                // read magic numbers
                int magic0 = in.readByte();
                int magic1 = in.readByte();

                if (magic0 != 31) {
                    throw new DecompressionException("Input is not in the GZIP format");
                }
                crc.update(magic0);
                crc.update(magic1);

                int method = in.readUnsignedByte();
                if (method != Deflater.DEFLATED) {
                    throw new DecompressionException("Unsupported compression method "
                            + method + " in the GZIP header");
                }
                crc.update(method);

                flags = in.readUnsignedByte();
                crc.update(flags);

                if ((flags & FRESERVED) != 0) {
                    throw new DecompressionException(
                            "Reserved flags are set in the GZIP header");
                }

                // mtime (int)
                crc.update(in, in.readerIndex(), 4);
                in.skipBytes(4);

                crc.update(in.readUnsignedByte()); // extra flags
                crc.update(in.readUnsignedByte()); // operating system

                gzipState = GzipState.FLG_READ;
                // fall through
            case FLG_READ:
                if ((flags & FEXTRA) != 0) {
                    if (in.readableBytes() < 2) {
                        return false;
                    }
                    int xlen1 = in.readUnsignedByte();
                    int xlen2 = in.readUnsignedByte();
                    crc.update(xlen1);
                    crc.update(xlen2);

                    // XLEN is a little-endian unsigned 16-bit value (RFC 1952), so xlen1 is the
                    // low byte and xlen2 the high byte. This must be an assignment, not |=: xlen
                    // starts at the -1 "no extra field" sentinel, and OR-ing into -1 (0xFFFFFFFF)
                    // would leave it -1, so the extra field was never skipped.
                    xlen = xlen2 << 8 | xlen1;
                }
                gzipState = GzipState.XLEN_READ;
                // fall through
            case XLEN_READ:
                if (xlen != -1) {
                    if (in.readableBytes() < xlen) {
                        return false;
                    }
                    crc.update(in, in.readerIndex(), xlen);
                    in.skipBytes(xlen);
                }
                gzipState = GzipState.SKIP_FNAME;
                // fall through
            case SKIP_FNAME:
                if (!skipIfNeeded(in, FNAME)) {
                    return false;
                }
                gzipState = GzipState.SKIP_COMMENT;
                // fall through
            case SKIP_COMMENT:
                if (!skipIfNeeded(in, FCOMMENT)) {
                    return false;
                }
                gzipState = GzipState.PROCESS_FHCRC;
                // fall through
            case PROCESS_FHCRC:
                if ((flags & FHCRC) != 0) {
                    if (!verifyCrc16(in)) {
                        return false;
                    }
                }
                crc.reset();
                gzipState = GzipState.HEADER_END;
                // fall through
            case HEADER_END:
                return true;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * 若标志位要求，跳过输入直至遇到结束标记 {@code 0x00}。
     * @param   in 输入缓冲
     * @param   flagMask 需要跳过时 {@code flags} 中应置位的掩码
     * @return  完成则 {@code true} 可进入下一状态；可读字节不足则 {@code false} 待重试
     */
    private boolean skipIfNeeded(ByteBuf in, int flagMask) {
        if ((flags & flagMask) != 0) {
            for (;;) {
                if (!in.isReadable()) {
                    // We didnt find the end yet, need to retry again once more data is readable
                    return false;
                }
                int b = in.readUnsignedByte();
                crc.update(b);
                if (b == 0x00) {
                    break;
                }
            }
        }
        // Skip is handled, we can move to the next processing state.
        return true;
    }

    /**
     * 读取 GZIP 尾部。
     *
     * @param   in 输入缓冲
     * @return  成功读取为 {@code true}；可读字节不足 8 则为 {@code false}
     */
    private boolean readGZIPFooter(ByteBuf in) {
        if (in.readableBytes() < 8) {
            return false;
        }

        boolean enoughData = verifyCrc(in);
        assert enoughData;

        // read ISIZE and verify
        int dataLength = in.readIntLE();
        int readLength = inflater.getTotalOut();
        if (dataLength != readLength) {
            throw new DecompressionException(
                    "Number of bytes mismatch. Expected: " + dataLength + ", Got: " + readLength);
        }
        return true;
    }

    /**
     * 校验 CRC32。
     *
     * @param   in 输入缓冲
     * @return  可读 4 字节并完成校验为 {@code true}，否则 {@code false}
     */
    private boolean verifyCrc(ByteBuf in) {
        if (in.readableBytes() < 4) {
            return false;
        }
        long crcValue = in.readUnsignedIntLE();

        long readCrc = crc.getValue();
        if (crcValue != readCrc) {
            throw new DecompressionException(
                    "CRC value mismatch. Expected: " + crcValue + ", Got: " + readCrc);
        }
        return true;
    }

    private boolean verifyCrc16(ByteBuf in) {
        if (in.readableBytes() < 2) {
            return false;
        }

        int crc16Value = in.readUnsignedShortLE();
        // the two least significant bytes from the CRC32
        int readCrc16 = (int) (crc.getValue() & 0xFFFF);

        if (crc16Value != readCrc16) {
            throw new DecompressionException(
                    "CRC16 value mismatch. Expected: " + crc16Value + ", Got: " + readCrc16);
        }
        return true;
    }

    /*
     * 根据 cmf_flg（ZLIB 流前两字节）判断是否为 ZLIB 流。
     * <p>
     * You can lookup the details in the ZLIB RFC:
     * <a href="https://tools.ietf.org/html/rfc1950#section-2.2">RFC 1950</a>.
     */
    private static boolean looksLikeZlib(short cmf_flg) {
        return (cmf_flg & 0x7800) == 0x7800 &&
                cmf_flg % 31 == 0;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // Discard bytes of the cumulation buffer if needed.
        discardSomeReadBytes();

        if (needsRead && !ctx.channel().config().isAutoRead()) {
            ctx.read();
        }
        ctx.fireChannelReadComplete();
    }
}
