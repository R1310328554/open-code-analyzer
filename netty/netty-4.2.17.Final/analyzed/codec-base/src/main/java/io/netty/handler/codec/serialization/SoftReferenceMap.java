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
import java.lang.ref.SoftReference;
import java.util.Map;

/**
 * 使用 {@link SoftReference} 存储值的 {@link ReferenceMap} 实现。
 * <p>
 * 内存紧张时 JVM 可回收软引用指向的对象，适合对象反序列化缓存等场景。
 */
final class SoftReferenceMap<K, V> extends ReferenceMap<K, V> {

    SoftReferenceMap(Map<K, Reference<V>> delegate) {
        super(delegate);
    }

    @Override
    Reference<V> fold(V value) {
        return new SoftReference<V>(value);
    }

}
