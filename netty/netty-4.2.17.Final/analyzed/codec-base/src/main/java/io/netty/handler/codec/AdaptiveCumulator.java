/*
 * Copyright 2026 The Netty Project
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

package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.handler.codec.ByteToMessageDecoder.Cumulator;
import io.netty.util.internal.ObjectUtil;

/**
 * 自适应累积器：在合并（内存拷贝）与组合（零拷贝追加组件）策略间动态切换，
 * 累积 {@link ByteBuf} 数据。
 */
public final class AdaptiveCumulator implements Cumulator {
    private final int composeMinSize;

    /**
     * @param composeMinSize 触发组合策略的最小阈值：仅当尾组件与入站缓冲区的
     *                       可读字节总和达到该值时，才将入站缓冲区作为
     *                       {@link CompositeByteBuf} 的新组件追加；
     *                       否则合并写入尾组件以避免过多小组件。
     */
    public AdaptiveCumulator(int composeMinSize) {
        ObjectUtil.checkPositiveOrZero(composeMinSize, "composeMinSize");
        this.composeMinSize = composeMinSize;
    }

    /**
     * 自适应累积：在零拷贝组合与尾组件合并之间按启发式规则选择。
     * <p>
     * 针对 {@link io.netty.handler.codec.ByteToMessageDecoder#COMPOSITE_CUMULATOR}
     * 的潜在攻击（攻击者逐字节发包导致每个字节独占一个组件）提供防护。
     * 当尾组件与入站数据总大小低于 {@link #composeMinSize} 时执行合并，
     * 否则追加为新组件；合并时尽量原地扩展尾缓冲，必要时按指数增长重分配，
     * 将最坏 {@code O(n^2)} 摊销为 {@code O(n)}。
     */
    @Override
    @SuppressWarnings("ReferenceEquality")
    public ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in) {
        if (cumulation == in) {
            in.release();
            return cumulation;
        }
        if (!cumulation.isReadable()) {
            cumulation.release();
            return in;
        }
        CompositeByteBuf composite = null;
        boolean cumulationTransferred = false;
        try {
            if (isOwnedCompositeBuf(cumulation)) {
                composite = (CompositeByteBuf) cumulation;
                cumulationTransferred = true;
                // 追加新组件前 writerIndex 须等于 capacity
                if (composite.writerIndex() != composite.capacity()) {
                    composite.capacity(composite.writerIndex());
                }
            } else {
                composite = alloc.compositeBuffer(Integer.MAX_VALUE);
                composite.addFlattenedComponents(true, cumulation);
                cumulationTransferred = true;
            }
            ByteBuf b = in;
            in = null;
            addInput(alloc, composite, b);

            CompositeByteBuf result = composite;
            composite = null;
            return result;
        } catch (Throwable t) {
            // 异常路径上 retain 原 cumulation，避免 finally 中 composite.release() 误释放
            if (cumulationTransferred && composite != null && composite != cumulation) {
                cumulation.retain();
            }
            throw t;
        } finally {
            if (in != null) {
                // 所有权未转移时必须 release，否则泄漏
                in.release();
            }
            // 未作为返回值的新分配 composite 也需 release
            if (composite != null && composite != cumulation) {
                composite.release();
            }
        }
    }

    private static boolean isOwnedCompositeBuf(ByteBuf buf) {
        return buf instanceof CompositeByteBuf && buf.refCnt() == 1;
    }

    private void addInput(ByteBufAllocator alloc, CompositeByteBuf composite, ByteBuf in) {
        if (shouldCompose(composite, in, composeMinSize)) {
            composite.addFlattenedComponents(true, in);
        } else {
            // 尾组件与入站数据合计低于阈值，执行合并
            mergeWithCompositeTail(alloc, composite, in);
        }
    }

    private static boolean shouldCompose(CompositeByteBuf composite, ByteBuf in, int composeMinSize) {
        int componentCount = composite.numComponents();
        if (componentCount == 0) {
            return true;
        }
        int inputSize = in.readableBytes();
        int tailStart = composite.toByteIndex(componentCount - 1);
        long tailSize = composite.writerIndex() - tailStart;
        return tailSize + inputSize >= composeMinSize;
    }

    /**
     * 将 {@code in} 合并进 {@code composite} 的尾组件：优先原地扩展或可写追加，
     * 否则分配更大缓冲拷贝尾与入站数据，避免逐字节触发 {@code O(n^2)}。
     */
    private static void mergeWithCompositeTail(
            ByteBufAllocator alloc, CompositeByteBuf composite, ByteBuf in) {
        int inputSize = in.readableBytes();
        int tailComponentIndex = composite.numComponents() - 1;
        int tailStart = composite.toByteIndex(tailComponentIndex);
        int tailSize = composite.writerIndex() - tailStart;
        int newTailSize = inputSize + tailSize;

        ByteBuf tail = composite.component(tailComponentIndex);
        ByteBuf newTail = null;
        // 用 componentSlice 获取组件在 composite 中的正确索引视图
        ByteBuf componentView = composite.componentSlice(tailComponentIndex);
        try {
            // 理想路径：尾组件独占且可原地扩展；须确认组件覆盖底层缓冲全容量，
            // 否则部分 slice 原地扩展会“复活”已丢弃字节导致静默数据损坏
            if (tail.refCnt() == 1 && !tail.isReadOnly() && tail.capacity() == componentView.capacity()
                    && newTailSize <= tail.maxCapacity()) {
                // 取得尾组件所有权
                newTail = tail.retain();

                // 按 composite 内组件视图同步读写索引
                newTail.setIndex(componentView.readerIndex(), componentView.writerIndex());

                /* writeBytes 负责扩容与拷贝；PooledByteBuf 可能快速扩展连续内存段。
                 * component() 返回 duplicate 包装，无法安全 unwrap 以利用 maxFastWritableBytes。 */
                newTail.writeBytes(in);
            } else {
                // 回退：新缓冲拷贝尾与入站数据，保证绝对索引一致，避免 slice 隐藏偏移损坏
                newTail = alloc.buffer(alloc.calculateNewCapacity(newTailSize, Integer.MAX_VALUE));
                newTail.setBytes(0, composite, tailStart, tailSize)
                        .setBytes(tailSize, in, in.readerIndex(), inputSize)
                        .writerIndex(newTailSize);
                in.readerIndex(in.writerIndex());
            }

            // 保存 readerIndex，替换组件时避免 writerIndex 越界
            int prevReader = composite.readerIndex();

            // 移除旧尾组件并追加新尾
            composite.removeComponent(tailComponentIndex).setIndex(0, tailStart);

            // 所有权即将转移给 composite；先置 null 防止 addFlattenedComponents 异常时双重 release
            ByteBuf b = newTail;
            newTail = null;
            composite.addFlattenedComponents(true, b);

            // 在 release in 之前恢复 readerIndex，失败时由调用方 finally 处理 in
            composite.readerIndex(prevReader);
        } finally {
            in.release();
            // 新尾未转移给 composite 时 release 防泄漏
            if (newTail != null) {
                newTail.release();
            }
        }
    }
}
