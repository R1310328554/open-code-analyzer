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

import java.lang.ref.Reference;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 基于 {@link Reference} 的 {@link Map} 适配器，将值包装为引用类型存储。
 * <p>
 * 子类通过 {@link #fold(Object)} 决定使用软引用还是弱引用；对外 API 仍暴露强类型 {@code V}，
 * 读写时自动完成引用与实体的转换。
 */
abstract class ReferenceMap<K, V> implements Map<K, V> {

    /** 底层委托 Map，值类型为 {@link Reference}。 */
    private final Map<K, Reference<V>> delegate;

    protected ReferenceMap(Map<K, Reference<V>> delegate) {
        this.delegate = delegate;
    }

    /** 将强引用值折叠为具体 {@link Reference} 实现（由子类提供）。 */
    abstract Reference<V> fold(V value);

    /** 从引用中解包出强引用值；引用为 null 或已被 GC 回收时返回 null。 */
    private V unfold(Reference<V> ref) {
        if (ref == null) {
            return null;
        }

        return ref.get();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        // 需遍历并解引用所有条目，本实现不支持
        throw new UnsupportedOperationException();
    }

    @Override
    public V get(Object key) {
        return unfold(delegate.get(key));
    }

    @Override
    public V put(K key, V value) {
        return unfold(delegate.put(key, fold(value)));
    }

    @Override
    public V remove(Object key) {
        return unfold(delegate.remove(key));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            delegate.put(entry.getKey(), fold(entry.getValue()));
        }
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }
}
