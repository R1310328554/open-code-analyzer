/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.models.sessions.infinispan.changes.remote.updater;

import java.util.Objects;

import org.infinispan.client.hotrod.MetadataValue;

/**
 * 创建、包装或标记删除 {@link Updater} 的工厂接口。
 *
 * @param <K> Infinispan 键类型
 * @param <V> Infinispan 值类型
 * @param <T> 具体 {@link Updater} 实现类型
 */
public interface UpdaterFactory<K, V, T extends Updater<K, V>> {

    /**
     * 为 Keycloak 事务中新创建的实体构造 {@link Updater}。
     *
     * @param key    Infinispan 键
     * @param entity Infinispan 值
     * @return 用于跟踪后续变更的 {@link Updater}
     */
    T create(K key, V entity);

    /**
     * 包装从 Infinispan 缓存读取的实体（含元数据版本）。
     *
     * @param key    Infinispan 键
     * @param entity 含版本信息的缓存条目
     * @return 用于跟踪后续变更的 {@link Updater}
     */
    default T wrapFromCache(K key, MetadataValue<V> entity) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(entity);
        return wrapFromCache(key, entity.getValue(), entity.getVersion());
    }

    /**
     * 包装从 Infinispan 缓存读取的实体。
     *
     * @param key     Infinispan 键
     * @param value   缓存值
     * @param version 条目版本号
     * @return 用于跟踪后续变更的 {@link Updater}
     */
    T wrapFromCache(K key, V value, long version);

    /**
     * 为未在本事务中读取过的键创建“已删除” {@link Updater}。
     *
     * @param key Infinispan 键
     * @return 表示删除操作的 {@link Updater}
     */
    T deleted(K key);
}
