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

package org.keycloak.models.sessions.infinispan.changes.remote.updater.helper;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 跟踪 {@link Map} 修改的可重放包装器。
 * <p>
 * 记录每次 put/remove/clear 操作，提交时可重放到另一 {@link Map} 实例。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class MapUpdater<K, V> extends AbstractMap<K, V> {

    private final Map<K, V> map;
    // 待重放的变更操作队列
    private final List<Consumer<Map<K, V>>> changes;

    public MapUpdater(Map<K, V> map) {
        this.map = map == null ? new HashMap<>() : map;
        changes = new ArrayList<>(4);
    }

    @Override
    public void clear() {
        changes.clear();
        addChange(Map::clear);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        addChange(kvMap -> kvMap.put(key, value));
        return map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        addChange(kvMap -> kvMap.remove(key));
        return map.remove(key);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    private void addChange(Consumer<Map<K, V>> change) {
        changes.add(change);
    }

    /**
     * 将已记录的变更重放到 {@code other} map。
     *
     * @param other 目标 {@link Map}
     */
    public void applyChanges(Map<K, V> other) {
        changes.forEach(consumer -> consumer.accept(other));
    }

    /**
     * @return {@code true} 表示本 map 未被修改
     */
    public boolean isUnchanged() {
        return changes.isEmpty();
    }
}
