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

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * 将 Java 对象序列化为 {@link ByteBuf} 的编码器（与标准 JDK 对象流互操作）。
 * <p>
 * 本编码器与标准 {@link ObjectInputStream}、{@link ObjectOutputStream} 格式兼容。
 * <p>
 * <strong>安全提示：</strong>Java 序列化存在安全风险，使用前应通过
 * {@code jdk.serialFilter} 等机制限制允许反序列化的类。
 * 详见 <a href="https://docs.oracle.com/en/java/javase/17/core/serialization-filtering1.html">
 * serialization filtering</a>。
 *
 * @deprecated 因序列化存在安全风险，本类已弃用且无替代方案
 */
@Deprecated
public class CompatibleObjectEncoder extends MessageToByteEncoder<Serializable> {
    /** 每隔多少个对象调用一次 {@link ObjectOutputStream#reset()}。 */
    /** 每隔多少个对象调用一次 {@link ObjectOutputStream#reset()}。 */
    private final int resetInterval;
    /** 已写入对象计数，用于触发 reset。 */
    /** 已写入对象计数，用于触发 reset。 */
    private int writtenObjects;

    /**
     * 创建实例，reset 间隔默认为 {@code 16}。
     */
    public CompatibleObjectEncoder() {
        this(16); // Reset at every sixteen writes
    }

    /**
     * 创建实例。
     *
     * @param resetInterval
     *        两次 {@link ObjectOutputStream#reset()} 之间写入的对象个数。
     *        为 {@code 0} 时禁用 reset，对端长期运行可能因重复类描述符积累而 {@link OutOfMemoryError}。
     */
    public CompatibleObjectEncoder(int resetInterval) {
        super(Serializable.class);
        this.resetInterval = checkPositiveOrZero(resetInterval, "resetInterval");
    }

    /**
     * 创建包装指定 {@link OutputStream} 的 {@link ObjectOutputStream}。
     * 子类可覆写以返回自定义 {@link ObjectOutputStream} 子类。
     */
    protected ObjectOutputStream newObjectOutputStream(OutputStream out) throws Exception {
        return new ObjectOutputStream(out);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
        // Suppress a warning about resource leak since oss is closed below
        ObjectOutputStream oos = newObjectOutputStream(
                new ByteBufOutputStream(out));
        try {
            if (resetInterval != 0) {
                // 定期 reset 防止对端 OOM
                writtenObjects ++;
                if (writtenObjects % resetInterval == 0) {
                    oos.reset();
                }
            }

            oos.writeObject(msg);
            oos.flush();
        } finally {
            oos.close();
        }
    }
}
