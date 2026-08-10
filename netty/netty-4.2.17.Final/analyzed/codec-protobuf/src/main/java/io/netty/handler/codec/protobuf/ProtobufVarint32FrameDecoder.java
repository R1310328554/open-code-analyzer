/*
 * Copyright 2015 The Netty Project
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
package io.netty.handler.codec.protobuf;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.nano.CodedInputByteBufferNano;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.TooLongFrameException;

import java.util.List;

import static io.netty.util.internal.ObjectUtil.checkPositive;

/**
 * A decoder that splits the received {@link ByteBuf}s dynamically by the
 * value of the Google Protocol Buffers
 * <a href="https://developers.google.com/protocol-buffers/docs/encoding#varints">Base
 * 128 Varints</a> integer length field in the message. For example:
 * <pre>
 * BEFORE DECODE (302 bytes)       AFTER DECODE (300 bytes)
 * +--------+---------------+      +---------------+
 * | Length | Protobuf Data |----->| Protobuf Data |
 * | 0xAC02 |  (300 bytes)  |      |  (300 bytes)  |
 * +--------+---------------+      +---------------+
 * </pre>
 * <p>按 protobuf Base-128 varint 长度前缀对 TCP 字节流做帧切分，每帧输出不含长度头的纯消息体
 * {@link ByteBuf}。与 {@link ProtobufDecoder} 配合使用时须放在 pipeline 最靠近网络的一侧。</p>
 *
 * @see CodedInputStream
 * @see CodedInputByteBufferNano
 */
public class ProtobufVarint32FrameDecoder extends ByteToMessageDecoder {

    /** 单帧允许的最大 payload 字节数，超出则抛 {@link TooLongFrameException} 并丢弃剩余帧体。 */
    private final int maxFrameLength;
    /** 超长帧被截断后，尚未跳过的剩余字节数（跨多次 decode 调用累积跳过）。 */
    private long bytesToDiscard;

    /**
     * Creates a new instance with no frame length limit.
     * <p>无帧长上限，等价于 {@code Integer.MAX_VALUE}。</p>
     */
    public ProtobufVarint32FrameDecoder() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Creates a new instance with the specified maximum frame length.
     *
     * @param maxFrameLength the maximum length of the frame.
     *                       If the length exceeds this value,
     *                       {@link TooLongFrameException} will be thrown.
     */
    public ProtobufVarint32FrameDecoder(int maxFrameLength) {
        this.maxFrameLength = checkPositive(maxFrameLength, "maxFrameLength");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
            throws Exception {
        // 上一帧因超长被判定无效，先清空缓冲区中属于该帧的残留字节
        if (bytesToDiscard > 0) {
            int localBytesToDiscard = (int) Math.min(bytesToDiscard, in.readableBytes());
            in.skipBytes(localBytesToDiscard);
            bytesToDiscard -= localBytesToDiscard;
            return;
        }

        in.markReaderIndex();
        int preIndex = in.readerIndex();
        int length = readRawVarint32(in);
        // readerIndex 未前进说明 varint 尚未收齐，等待更多数据
        if (preIndex == in.readerIndex()) {
            return;
        }
        if (length < 0) {
            throw new CorruptedFrameException("negative length: " + length);
        }

        if (length > maxFrameLength) {
            long discard = length - in.readableBytes();
            if (discard <= 0) {
                in.skipBytes(length);
            } else {
                // 当前缓冲区装不下整帧，记录待丢弃量并在后续 decode 中继续 skip
                bytesToDiscard = discard;
                in.skipBytes(in.readableBytes());
            }
            throw new TooLongFrameException(
                    "Frame length exceeds " + maxFrameLength
                    + ": " + length);
        }

        if (in.readableBytes() < length) {
            // payload 未收齐，回退 readerIndex 保留已读到的 varint
            in.resetReaderIndex();
        } else {
            out.add(in.readRetainedSlice(length));
        }
    }

    /**
     * Reads variable length 32bit int from buffer
     *
     * @return decoded int if buffers readerIndex has been forwarded else nonsense value
     * <p>若 varint 不完整则回退 readerIndex 并返回 0，调用方通过 index 是否变化判断是否可读。</p>
     */
    static int readRawVarint32(ByteBuf buffer) {
        if (buffer.readableBytes() < 4) {
            return readRawVarint24(buffer);
        }
        int wholeOrMore = buffer.getIntLE(buffer.readerIndex());
        // 每个字节的最高位为 1 表示后续还有 continuation 字节
        int firstOneOnStop = ~wholeOrMore & 0x80808080;
        if (firstOneOnStop == 0) {
            // 前 4 字节 continuation 位全为 1，需要读第 5 字节（最多 5 字节 varint32）
            return readRawVarint40(buffer, wholeOrMore);
        }
        int bitsToKeep = Integer.numberOfTrailingZeros(firstOneOnStop) + 1;
        buffer.skipBytes(bitsToKeep >> 3);
        int thisVarintMask = firstOneOnStop ^ (firstOneOnStop - 1);
        int wholeWithContinuations = wholeOrMore & thisVarintMask;
        // mix them up as per varint spec while dropping the continuation bits:
        // 0x7F007F isolate the first byte and the third byte dropping the continuation bits
        // 0x7F007F00 isolate the second byte and the fourth byte dropping the continuation bits
        // the second and fourth byte are shifted to the right by 1, filling the gaps left by the first and third byte
        // it means that the first and second bytes now occupy the first 14 bits (7 bits each)
        // and the third and fourth bytes occupy the next 14 bits (7 bits each), with a gap between the 2s of 2 bytes
        // and another gap of 2 bytes after the forth and third.
        // 按 varint 规范剥离 continuation 位并重组为 32 位整数（快速路径，一次读 4 字节）
        wholeWithContinuations = (wholeWithContinuations & 0x7F007F) | ((wholeWithContinuations & 0x7F007F00) >> 1);
        // 0x3FFF isolate the first 14 bits i.e. the first and second bytes
        // 0x3FFF0000 isolate the next 14 bits i.e. the third and forth bytes
        // the third and forth bytes are shifted to the right by 2, filling the gaps left by the first and second bytes
        return (wholeWithContinuations & 0x3FFF) | ((wholeWithContinuations & 0x3FFF0000) >> 2);
    }

    /** 解析需要第 5 字节的 32 位 varint（值 ≥ 2^28）。 */
    private static int readRawVarint40(ByteBuf buffer, int wholeOrMore) {
        byte lastByte;
        if (buffer.readableBytes() == 4 || (lastByte = buffer.getByte(buffer.readerIndex() + 4)) < 0) {
            throw new CorruptedFrameException("malformed varint.");
        }
        buffer.skipBytes(5);
        // add it to wholeOrMore
        return wholeOrMore & 0x7F |
               (((wholeOrMore >> 8) & 0x7F) << 7) |
               (((wholeOrMore >> 16) & 0x7F) << 14) |
               (((wholeOrMore >> 24) & 0x7F) << 21) |
               (lastByte << 28);
    }

    /** 缓冲区不足 4 字节时的逐字节 varint 解析（最多 3 个 continuation 字节）。 */
    private static int readRawVarint24(ByteBuf buffer) {
        if (!buffer.isReadable()) {
            return 0;
        }
        buffer.markReaderIndex();

        byte tmp = buffer.readByte();
        if (tmp >= 0) {
            return tmp;
        }
        int result = tmp & 127;
        if (!buffer.isReadable()) {
            buffer.resetReaderIndex();
            return 0;
        }
        if ((tmp = buffer.readByte()) >= 0) {
            return result | tmp << 7;
        }
        result |= (tmp & 127) << 7;
        if (!buffer.isReadable()) {
            buffer.resetReaderIndex();
            return 0;
        }
        if ((tmp = buffer.readByte()) >= 0) {
            return result | tmp << 14;
        }
        return result | (tmp & 127) << 14;
    }
}
