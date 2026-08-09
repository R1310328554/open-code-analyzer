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
package io.netty.buffer;

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

/**
 * 在 {@link ByteBuf} 中保存数据的 {@link ByteBufHolder} 默认实现。
 *
 */
public class DefaultByteBufHolder implements ByteBufHolder {

    private final ByteBuf data;

    public DefaultByteBufHolder(ByteBuf data) {
        this.data = ObjectUtil.checkNotNull(data, "data");
    }

    @Override
    public ByteBuf content() {
        return ByteBufUtil.ensureAccessible(data);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 默认调用 {@code replace(content().copy())}。
     */
    @Override
    public ByteBufHolder copy() {
        return replace(data.copy());
    }

    /**
     * {@inheritDoc}
     * <p>
     * 默认调用 {@code replace(content().duplicate())}。
     */
    @Override
    public ByteBufHolder duplicate() {
        return replace(data.duplicate());
    }

    /**
     * {@inheritDoc}
     * <p>
     * 默认调用 {@code replace(content().retainedDuplicate())}。
     */
    @Override
    public ByteBufHolder retainedDuplicate() {
        return replace(data.retainedDuplicate());
    }

    /**
     * {@inheritDoc}
     * <p>
     * 子类可覆盖此方法，返回内容设为指定 {@code content} 的新实例；
     * {@link #copy()}、{@link #duplicate()}、{@link #retainedDuplicate()} 默认通过此方法创建副本。
     */
    @Override
    public ByteBufHolder replace(ByteBuf content) {
        return new DefaultByteBufHolder(content);
    }

    @Override
    public int refCnt() {
        return data.refCnt();
    }

    @Override
    public ByteBufHolder retain() {
        data.retain();
        return this;
    }

    @Override
    public ByteBufHolder retain(int increment) {
        data.retain(increment);
        return this;
    }

    @Override
    public ByteBufHolder touch() {
        data.touch();
        return this;
    }

    @Override
    public ByteBufHolder touch(Object hint) {
        data.touch(hint);
        return this;
    }

    @Override
    public boolean release() {
        return data.release();
    }

    @Override
    public boolean release(int decrement) {
        return data.release(decrement);
    }

    /**
     * 不检查引用计数即调用 {@link ByteBuf#toString()}，便于实现 {@link #toString()}。
     */
    protected final String contentToString() {
        return data.toString();
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + '(' + contentToString() + ')';
    }

    /**
     * 本 {@code equals} 仅与同类型实例比较，以保持 Netty 各子类扩展时的对称性。
     *
      * @param   o   the reference object with which to compare.
     * @return 若与 obj 相同则为 {@code true}，否则为 {@code false}。
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && getClass() == o.getClass()) {
            return data.equals(((DefaultByteBufHolder) o).data);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }
}
