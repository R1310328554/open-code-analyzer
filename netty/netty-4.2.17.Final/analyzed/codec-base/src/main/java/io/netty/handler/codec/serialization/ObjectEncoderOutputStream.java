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
import io.netty.buffer.Unpooled;
import io.netty.util.internal.ObjectUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * 与 {@link ObjectDecoder}、{@link ObjectDecoderInputStream} 互操作的 {@link ObjectOutput} 实现。
 * <p>
 * <strong>安全提示：</strong>Java 序列化存在安全风险，使用前应通过
 * {@code jdk.serialFilter} 等机制限制允许反序列化的类。
 * 详见 <a href="https://docs.oracle.com/en/java/javase/17/core/serialization-filtering1.html">
 * serialization filtering</a>。
 *
 * @deprecated 因序列化存在安全风险，本类已弃用且无替代方案
 */
@Deprecated
public class ObjectEncoderOutputStream extends OutputStream implements
        ObjectOutput {

    /** 底层数据输出流。 */
    /** 底层数据输出流。 */
    private final DataOutputStream out;
    /** 序列化对象的预估字节长度，用于初始缓冲区大小。 */
    /** 序列化对象的预估字节长度，用于初始缓冲区大小。 */
    private final int estimatedLength;

    /**
     * 创建 {@link ObjectOutput}，预估长度为 512 字节。
     *
     * @param out
     *        写入序列化数据的 {@link OutputStream}
     */
    public ObjectEncoderOutputStream(OutputStream out) {
        this(out, 512);
    }

    /**
     * 创建 {@link ObjectOutput}。
     *
     * @param out
     *        写入序列化数据的 {@link OutputStream}
     *
     * @param estimatedLength
     *        单个对象序列化形式的预估字节长度。
     *        实际超出时会自动扩容；过大则浪费内存带宽。
     *        为减少拷贝与分配，请给出合理预估值。
     */
    public ObjectEncoderOutputStream(OutputStream out, int estimatedLength) {
        ObjectUtil.checkNotNull(out, "out");
        ObjectUtil.checkPositiveOrZero(estimatedLength, "estimatedLength");

        if (out instanceof DataOutputStream) {
            this.out = (DataOutputStream) out;
        } else {
            this.out = new DataOutputStream(out);
        }
        this.estimatedLength = estimatedLength;
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        ByteBuf buf = Unpooled.buffer(estimatedLength);
        try {
            // Suppress a warning about resource leak since oout is closed below
            ObjectOutputStream oout = new CompactObjectOutputStream(
                    new ByteBufOutputStream(buf));
            try {
                oout.writeObject(obj);
                oout.flush();
            } finally {
                oout.close();
            }

            int objectSize = buf.readableBytes();
            writeInt(objectSize);
            buf.getBytes(0, this, objectSize);
        } finally {
            buf.release();
        }
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    /** 返回底层 {@link DataOutputStream} 已写入的字节数。 */
    /** 返回底层 {@link DataOutputStream} 已写入的字节数。 */
    public final int size() {
        return out.size();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    @Override
    public final void writeBoolean(boolean v) throws IOException {
        out.writeBoolean(v);
    }

    @Override
    public final void writeByte(int v) throws IOException {
        out.writeByte(v);
    }

    @Override
    public final void writeBytes(String s) throws IOException {
        out.writeBytes(s);
    }

    @Override
    public final void writeChar(int v) throws IOException {
        out.writeChar(v);
    }

    @Override
    public final void writeChars(String s) throws IOException {
        out.writeChars(s);
    }

    @Override
    public final void writeDouble(double v) throws IOException {
        out.writeDouble(v);
    }

    @Override
    public final void writeFloat(float v) throws IOException {
        out.writeFloat(v);
    }

    @Override
    public final void writeInt(int v) throws IOException {
        out.writeInt(v);
    }

    @Override
    public final void writeLong(long v) throws IOException {
        out.writeLong(v);
    }

    @Override
    public final void writeShort(int v) throws IOException {
        out.writeShort(v);
    }

    @Override
    public final void writeUTF(String str) throws IOException {
        out.writeUTF(str);
    }
}
