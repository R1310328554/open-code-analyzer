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

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.keycloak.models.sessions.infinispan.changes.remote.remover.ConditionalRemover;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.impl.query.RemoteQuery;
import org.infinispan.commons.util.concurrent.AggregateCompletionStage;
import org.jboss.logging.Logger;

/**
 * 基于 Ickle DELETE 语句的 {@link ConditionalRemover} 抽象基类。
 * <p>
 * 子类提供 ProtoStream 实体名、WHERE 条件及绑定参数，本类负责组装并异步执行删除。
 *
 * @param <K> {@link RemoteCache} 键类型
 * @param <V> {@link RemoteCache} 值类型
 */
abstract class QueryBasedConditionalRemover<K, V> implements ConditionalRemover<K, V> {

    private final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    // Ickle 删除语句模板
    private static final String QUERY_FMT = "DELETE FROM %s WHERE %s";

    @Override
    public void executeRemovals(RemoteCache<K, V> cache, AggregateCompletionStage<Void> stage) {
        // 无条件时跳过，避免发送空 DELETE
        if (isEmpty()) {
            return;
        }
        stage.dependsOn(executeDeleteStatement(cache));
    }

    private CompletionStage<?> executeDeleteStatement(RemoteCache<K, V> cache) {
        var isTrace = logger.isTraceEnabled();
        var deleteStatement = QUERY_FMT.formatted(getEntity(), getQueryConditions());
        if (isTrace) {
            logger.tracef("About to execute delete statement in cache '%s': %s", cache.getName(), deleteStatement);
        }
        // 构建远程查询并异步执行 DELETE
        RemoteQuery<?> query = (RemoteQuery<?>) cache.query(deleteStatement)
                .setParameters(getQueryParameters());
        var stage = query.executeStatementAsync();
        if (isTrace) {
            return stage.thenAccept(removed -> logger.debugf("Delete Statement removed %d entries from cache '%s'", removed, cache.getName()));
        }
        return stage;
    }

    /**
     * @return Infinispan ProtoStream 实体名
     */
    abstract String getEntity();

    /**
     * @return DELETE 语句的 WHERE 条件子句
     */
    abstract String getQueryConditions();

    /**
     * @return 条件参数名到值的映射；无条件时返回空 map
     */
    abstract Map<String, Object> getQueryParameters();

    /**
     * @return {@code true} 表示本删除器无任何待删条件，可跳过 DELETE 语句
     */
    abstract boolean isEmpty();
}
