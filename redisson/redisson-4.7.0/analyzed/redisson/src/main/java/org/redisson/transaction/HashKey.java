/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.transaction;

import org.redisson.client.codec.Codec;

import java.util.Objects;

/**
 * 本地缓存 Map 在事务 commit 协调中的键标识。
 * <p>
 * 由 Redis 对象 {@link #name} 与 {@link #codec} 组成；
 * {@link #equals}/{@link #hashCode} 仅比较 name（同一名称视为同一 LocalCachedMap）。
 *
 * @author Nikita Koksharov
 *
 */
public final class HashKey {

    /** 对象编解码器（影响 disabled-keys 结构）。 */
    final Codec codec;
    /** LocalCachedMap 的 Redis 名称。 */
    final String name;
    
    /** @param name Map 名称 @param codec 编解码器 */
    public HashKey(String name, Codec codec) {
        this.name = name;
        this.codec = codec;
    }
    
    public Codec getCodec() {
        return codec;
    }
    
    public String getName() {
        return name;
    }

    /** 相等性仅基于 {@link #name}。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashKey hashKey = (HashKey) o;
        return Objects.equals(name, hashKey.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
