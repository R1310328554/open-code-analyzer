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

package org.keycloak.models.sessions.infinispan.changes.remote.remover.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.infinispan.client.hotrod.RemoteCache;

/**
 * 支持多条删除条件的 {@link QueryBasedConditionalRemover} 基类。
 * <p>
 * 条件可动态追加；执行查询时用 {@code ||} 连接，匹配任一条件即删除。
 *
 * @param <K> {@link RemoteCache} 键类型
 * @param <V> {@link RemoteCache} 值类型
 */
abstract class MultipleConditionQueryRemover<K, V> extends QueryBasedConditionalRemover<K, V> {

    // 已登记的删除条件列表
    private final List<RemoveCondition<K, V>> removes;
    // 生成唯一 Ickle 参数名的计数器
    private int parameterIndex;

    MultipleConditionQueryRemover() {
        removes = new ArrayList<>();
    }

    @Override
    String getQueryConditions() {
        // 各条件子句以 OR 连接
        return removes.stream()
                .map(RemoveCondition::getConditionalClause)
                .collect(Collectors.joining(" || "));
    }

    @Override
    Map<String, Object> getQueryParameters() {
        Map<String, Object> parameters = new HashMap<>();
        removes.forEach(removeCondition -> removeCondition.addParameters(parameters));
        return parameters;
    }

    @Override
    boolean isEmpty() {
        return removes.isEmpty();
    }

    @Override
    public boolean willRemove(K key, V value) {
        return !isEmpty() && removes.stream().anyMatch(c -> c.willRemove(key, value));
    }

    /**
     * 为 Ickle 查询生成唯一参数名（如 p0、p1）。
     */
    String nextParameter() {
        return "p" + parameterIndex++;
    }

    /** 追加一条删除条件。 */
    void add(RemoveCondition<K, V> condition) {
        removes.add(condition);
    }

    /**
     * 单条删除条件：提供 WHERE 子句、参数绑定及本地预判逻辑。
     */
    interface RemoveCondition<K, V> {
        /**
         * @return 带命名参数的 WHERE 子句片段
         */
        String getConditionalClause();

        /**
         * 将本条件的参数写入查询参数 map
         */
        void addParameters(Map<String, Object> parameters);

        /**
         * @return {@code true} 表示该键值对会被本次查询删除
         */
        boolean willRemove(K key, V value);
    }
}
