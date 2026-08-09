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
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

/**
 * 将收到的 {@link ByteBuf} 反序列化为 Java 对象的解码器。
 * <p>
 * 本解码器期望的序列化格式与标准 {@link ObjectOutputStream} 不兼容。
 * 请配合 {@link ObjectEncoder} 或 {@link ObjectEncoderOutputStream} 使用。
 * <p>
 * <strong>安全提示：</strong>Java 序列化存在安全风险，使用前应通过
 * {@code jdk.serialFilter} 等机制限制允许反序列化的类。
 * 详见 <a href="https://docs.oracle.com/en/java/javase/17/core/serialization-filtering1.html">
 * serialization filtering</a>。
 *
 * @deprecated 因序列化存在安全风险，本类已弃用且无替代方案
 */
@Deprecated
public class ObjectDecoder extends LengthFieldBasedFrameDecoder {

    /** 反序列化时解析类名的解析器。 */
    /** 反序列化时解析类名的解析器。 */
    private final ClassResolver classResolver;

    /**
     * 创建解码器，单对象最大字节数为 {@code 1048576}（1 MB）。
     * 超出时抛出 {@link StreamCorruptedException}。
     *
      * @param classResolver 本解码器使用的 {@link ClassResolver}
     */
    public ObjectDecoder(ClassResolver classResolver) {
        this(1048576, classResolver);
    }

    /**
     * 创建指定最大对象大小的解码器。
     *
      * @param maxObjectSize  序列化对象允许的最大字节长度；
     *                       超出时抛出 {@link StreamCorruptedException}
      * @param classResolver    加载序列化对象类的 {@link ClassResolver}
     */
    public ObjectDecoder(int maxObjectSize, ClassResolver classResolver) {
        super(maxObjectSize, 0, 4, 0, 4);
        this.classResolver = classResolver;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }

        ObjectInputStream ois = new CompactObjectInputStream(new ByteBufInputStream(frame, true), classResolver);
        try {
            return ois.readObject();
        } finally {
            ois.close();
        }
    }
}
