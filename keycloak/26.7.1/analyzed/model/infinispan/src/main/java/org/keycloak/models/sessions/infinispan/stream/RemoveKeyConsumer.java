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

import java.util.function.BiConsumer;

import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoTypeId;

import static org.keycloak.marshalling.Marshalling.REMOVE_KEY_BI_CONSUMER;

/**
 * 从 {@link Cache} 中移除指定键的 {@link BiConsumer}。
 * <p>
 * 尽力而为（best-effort）实现：删除失败时不抛出异常。
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 */
@ProtoTypeId(REMOVE_KEY_BI_CONSUMER)
public class RemoveKeyConsumer<K, V> implements BiConsumer<Cache<K, V>, K> {

    /** 单例实例，供分布式流终端操作复用。 */
    private static final RemoveKeyConsumer<Object, Object> INSTANCE = new RemoveKeyConsumer<>();

    /** 返回可 ProtoStream 序列化的单例消费者。 */
    @ProtoFactory
    @SuppressWarnings("unchecked")
    public static <K, V> RemoveKeyConsumer<K, V> getInstance() {
        return (RemoveKeyConsumer<K, V>) INSTANCE;
    }

    /** 以无锁、静默方式从缓存移除键，忽略返回值。 */
    @Override
    public void accept(Cache<K, V> cache, K key) {
        cache.getAdvancedCache()
                .withFlags(Flag.ZERO_LOCK_ACQUISITION_TIMEOUT, Flag.FAIL_SILENTLY, Flag.IGNORE_RETURN_VALUES)
                .remove(key);
    }
}
