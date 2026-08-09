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
import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * 使用 {@link WeakReference} 存储值的 {@link ReferenceMap} 实现。
 * <p>
 * 仅当键仍被强引用持有时值才可能存活；键消失后值可被 GC 回收。
 */
final class WeakReferenceMap<K, V> extends ReferenceMap<K, V> {

    WeakReferenceMap(Map<K, Reference<V>> delegate) {
        super(delegate);
    }

    @Override
    Reference<V> fold(V value) {
        return new WeakReference<V>(value);
    }

}
