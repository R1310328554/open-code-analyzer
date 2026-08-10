/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.models.sessions.infinispan.stream;

import java.util.function.BiFunction;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoTypeId;

import static org.keycloak.marshalling.Marshalling.VALUE_IDENTITY_BI_FUNCTION;

/**
 * 恒等 {@link BiFunction}：忽略第一个参数，原样返回第二个参数（值）。
 * <p>
 * 用于 Infinispan 需要 {@link BiFunction} 签名但仅需保留现有值的场景（如仅更新 TTL）。
 * <p>
 * 无状态单例，通过 ProtoStream 序列化以支持远程缓存分布式操作。
 *
 * @param <K> 第一个参数（键）类型，被忽略
 * @param <V> 第二个参数（值）类型，原样返回
 */
@ProtoTypeId(VALUE_IDENTITY_BI_FUNCTION)
public final class ValueIdentityBiFunction<K, V> implements BiFunction<K, V, V> {

    /** 单例实例。 */
    private static final ValueIdentityBiFunction<?, ?> INSTANCE = new ValueIdentityBiFunction<>();

    private ValueIdentityBiFunction() {
    }

    /**
     * 返回此函数的单例实例。
     * <p>
     * 标注 {@link ProtoFactory} 以支持 Infinispan ProtoStream 远程缓存序列化。
     *
     * @param <T> 键参数类型
     * @param <E> 值参数类型
     * @return {@link ValueIdentityBiFunction} 单例
     */
    @ProtoFactory
    @SuppressWarnings("unchecked")
    public static <T, E> ValueIdentityBiFunction<T, E> getInstance() {
        return (ValueIdentityBiFunction<T, E>) INSTANCE;
    }

    /**
     * 忽略键参数，原样返回值参数。
     *
     * @param k 键参数（被忽略）
     * @param v 待返回的值参数
     * @return 未修改的值参数
     */
    @Override
    public V apply(K k, V v) {
        return v;
    }
}
