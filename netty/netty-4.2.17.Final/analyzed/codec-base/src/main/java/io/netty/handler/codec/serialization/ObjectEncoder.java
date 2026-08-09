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
package io.netty.handler.codec.serialization;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 将 Java 对象序列化为 {@link ByteBuf} 的编码器。
 * <p>
 * 本编码器产出的格式与标准 {@link ObjectInputStream} 不兼容。
 * 请配合 {@link ObjectDecoder} 或 {@link ObjectDecoderInputStream} 使用。
 * <p>
 * <strong>安全提示：</strong>Java 序列化存在安全风险，使用前应通过
 * {@code jdk.serialFilter} 等机制限制允许反序列化的类。
 * 详见 <a href="https://docs.oracle.com/en/java/javase/17/core/serialization-filtering1.html">
 * serialization filtering</a>。
 *
 * @deprecated 因序列化存在安全风险，本类已弃用且无替代方案
 */
@Deprecated
@Sharable
public class ObjectEncoder extends MessageToByteEncoder<Serializable> {
    /** 长度字段占位符（4 字节），序列化完成后回填实际长度。 */
    /** 长度字段占位符（4 字节），序列化完成后回填实际长度。 */
    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

    /** 创建编码器，出站消息类型为 {@link Serializable}。 */
    /** 创建编码器，出站消息类型为 {@link Serializable}。 */
    public ObjectEncoder() {
        super(Serializable.class);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
        int startIdx = out.writerIndex();

        ByteBufOutputStream bout = new ByteBufOutputStream(out);
        ObjectOutputStream oout = null;
        try {
            // 先写入 4 字节长度占位符
            bout.write(LENGTH_PLACEHOLDER);
            oout = new CompactObjectOutputStream(bout);
            oout.writeObject(msg);
            oout.flush();
        } finally {
            if (oout != null) {
                oout.close();
            } else {
                bout.close();
            }
        }

        int endIdx = out.writerIndex();

        // 回填实际载荷长度（不含长度字段本身）
        out.setInt(startIdx, endIdx - startIdx - 4);
    }
}
