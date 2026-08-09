/*
 * Copyright 2025 The Netty Project
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
import io.netty.util.internal.UnstableApi;

/**
 * 各类解压算法的统一 API；当前状态由 {@link #status()} 报告，调用方须按状态调用合法操作。
 * <p>
 * 任一方法抛异常后除 {@link #close()} 外不得再调用其他方法。
 * <p>
 * 该 API 仍在演进，见 <a href="https://github.com/netty/netty/issues/16743">#16743</a>。
 */
@UnstableApi
public interface Decompressor extends AutoCloseable {
    /**
     * 获取当前解压状态；失败或已关闭后不可调用。
     *
     * @return The current status
     */
    Status status() throws DecompressionException;

    /**
     * 追加输入缓冲；仅 {@link Status#NEED_INPUT} 时允许，所有权移交给解压器。
     *
     * @param buf The input buffer. Buffer ownership transfers to the decompressor (also on exception).
     */
    void addInput(ByteBuf buf) throws DecompressionException;

    /**
     * 通知输入已结束；部分实现会刷出剩余数据或校验截断，多数为空操作。
     * 仅 {@link Status#NEED_INPUT} 时允许。
     */
    void endOfInput() throws DecompressionException;

    /**
     * 取出一帧解压输出；仅 {@link Status#NEED_OUTPUT} 时允许，所有权交给调用方。
     *
     * @return The decompressed buffer. May be empty.
     */
    ByteBuf takeOutput() throws DecompressionException;

    /** 关闭解压器并释放资源；<b>幂等</b>，可重复调用。 */
    @Override
    void close() throws DecompressionException;

    /** 解压器状态枚举，指示下一步应调用哪个方法以推进解压。 */
    @UnstableApi
    enum Status {
        /** 需要更多输入；仅允许 {@link #addInput} 与 {@link #endOfInput()}。 */
        NEED_INPUT,
        /** 须先消费输出再接收输入；仅允许 {@link #takeOutput()}。 */
        NEED_OUTPUT,
        /** 全部数据处理完毕；除 {@link Decompressor#close()} 外不得再调用解压操作。 */
        COMPLETE,
    }

    /** {@link Decompressor} 构建器抽象基类。 */
    @UnstableApi
    abstract class AbstractDecompressorBuilder {

        protected AbstractDecompressorBuilder() {
        }

        /** 使用给定 {@link ByteBufAllocator} 构建 {@link Decompressor} 实例。 */
        public abstract Decompressor build(ByteBufAllocator allocator) throws DecompressionException;
    }
}
