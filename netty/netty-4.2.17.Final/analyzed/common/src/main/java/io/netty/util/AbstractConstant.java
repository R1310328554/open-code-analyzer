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
package io.netty.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Base implementation of {@link Constant}.
 *
 * <p>{@link Constant} 的抽象基类，由 {@link ConstantPool} 统一管理 id 与名称。
 * 每个实例还持有全局递增的 {@code uniquifier}，用于在 hashCode 相同时打破 {@link #compareTo} 平局。</p>
 */
public abstract class AbstractConstant<T extends AbstractConstant<T>> implements Constant<T> {

    /** 为每个常量分配唯一序号，保证 compareTo 全序。 */
    private static final AtomicLong uniqueIdGenerator = new AtomicLong();
    /** 在常量池中的数值 id。 */
    private final int id;
    /** 常量名称（通常与 {@link #toString()} 一致）。 */
    private final String name;
    /** 构造时分配的全局唯一序号，仅用于 compareTo。 */
    private final long uniquifier;

    /**
     * Creates a new instance.
     *
     * <p>子类由 {@link ConstantPool} 调用；{@code id} 在池内唯一，{@code name} 为注册名。</p>
     */
    protected AbstractConstant(int id, String name) {
        this.id = id;
        this.name = name;
        this.uniquifier = uniqueIdGenerator.getAndIncrement();
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final int id() {
        return id;
    }

    @Override
    public final String toString() {
        return name();
    }

    /** 委托 {@link Object#hashCode()}，保证同一实例 identity 稳定。 */
    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    /** 委托 {@link Object#equals(Object)}，仅同一实例相等。 */
    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * 按 hashCode 再按 uniquifier 排序；两实例 hash 相同且 uniquifier 也相同则不应出现。
     */
    @Override
    public final int compareTo(T o) {
        if (this == o) {
            return 0;
        }

        @SuppressWarnings("UnnecessaryLocalVariable")
        AbstractConstant<T> other = o;
        int returnCode;

        returnCode = hashCode() - other.hashCode();
        if (returnCode != 0) {
            return returnCode;
        }

        if (uniquifier < other.uniquifier) {
            return -1;
        }
        if (uniquifier > other.uniquifier) {
            return 1;
        }

        throw new Error("failed to compare two different constants");
    }

}
