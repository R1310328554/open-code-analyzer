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

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * An encoder that prepends the Google Protocol Buffers
 * <a href="https://developers.google.com/protocol-buffers/docs/encoding?csw=1#varints">Base
 * 128 Varints</a> integer length field. For example:
 * <pre>
 * BEFORE ENCODE (300 bytes)       AFTER ENCODE (302 bytes)
 * +---------------+               +--------+---------------+
 * | Protobuf Data |-------------->| Length | Protobuf Data |
 * |  (300 bytes)  |               | 0xAC02 |  (300 bytes)  |
 * +---------------+               +--------+---------------+
 * </pre> *
 * <p>在序列化后的 protobuf 消息体前写入 Base-128 varint 长度前缀，与
 * {@link ProtobufVarint32FrameDecoder} 成对使用。{@link Sharable} 表示无状态，可共享实例。</p>
 *
 * @see CodedOutputStream
 * @see CodedOutputByteBufferNano
 */
@Sharable
public class ProtobufVarint32LengthFieldPrepender extends MessageToByteEncoder<ByteBuf> {

    public ProtobufVarint32LengthFieldPrepender() {
        super(ByteBuf.class);
    }

    @Override
    protected void encode(
            ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        int bodyLen = msg.readableBytes();
        int headerLen = computeRawVarint32Size(bodyLen);
        out.ensureWritable(headerLen + bodyLen);
        writeRawVarint32(out, bodyLen);
        out.writeBytes(msg, msg.readerIndex(), bodyLen);
    }

    /**
     * Writes protobuf varint32 to (@link ByteBuf).
     * @param out to be written to
     * @param value to be written
     * <p>按 7 位一组、小端序写入 varint；末字节最高位为 0，其余为 1 表示还有后续字节。</p>
     */
    static void writeRawVarint32(ByteBuf out, int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                out.writeByte(value);
                return;
            } else {
                out.writeByte((value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    /**
     * Computes size of protobuf varint32 after encoding.
     * @param value which is to be encoded.
     * @return size of value encoded as protobuf varint32.
     * <p>编码前预计算 varint 占用的字节数（1～5），用于 {@code ensureWritable} 避免扩容。</p>
     */
    static int computeRawVarint32Size(final int value) {
        if ((value & (0xffffffff <<  7)) == 0) {
            return 1;
        }
        if ((value & (0xffffffff << 14)) == 0) {
            return 2;
        }
        if ((value & (0xffffffff << 21)) == 0) {
            return 3;
        }
        if ((value & (0xffffffff << 28)) == 0) {
            return 4;
        }
        return 5;
    }
}
