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

/**
 * Holds {@link Attribute}s which can be accessed via {@link AttributeKey}.
 *
 * <p>按 {@link AttributeKey} 存取 {@link Attribute} 的容器；{@link io.netty.channel.Channel}、
 * {@link io.netty.util.concurrent.EventExecutor} 等均实现此接口。实现须线程安全。</p>
 *
 * Implementations must be Thread-safe.
 */
public interface AttributeMap {
    /**
     * Get the {@link Attribute} for the given {@link AttributeKey}. This method will never return null, but may return
     * an {@link Attribute} which does not have a value set yet.
     *
     * <p>永不为 null；值未设置时 {@link Attribute#get()} 为 null。</p>
     */
    <T> Attribute<T> attr(AttributeKey<T> key);

    /**
     * Returns {@code true} if and only if the given {@link Attribute} exists in this {@link AttributeMap}.
     *
     * <p>键是否已在 map 中（通常表示曾写入非 null 或显式创建过条目）。</p>
     */
    <T> boolean hasAttr(AttributeKey<T> key);
}
