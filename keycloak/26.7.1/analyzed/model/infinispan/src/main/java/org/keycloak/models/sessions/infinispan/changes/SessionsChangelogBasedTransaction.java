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

package org.keycloak.models.sessions.infinispan.changes;

import org.keycloak.models.sessions.infinispan.entities.SessionEntity;

/**
 * 基于变更日志的会话事务接口。
 * <p>
 * 在单个 Keycloak 事务内累积 {@link SessionUpdateTask}，提交时再合并写入 Infinispan 与持久化层。
 *
 * @param <K> 缓存键类型
 * @param <V> 会话实体类型
 */
public interface SessionsChangelogBasedTransaction<K, V extends SessionEntity> {

    /** 为指定键追加一条会话更新任务。 */
    void addTask(K key, SessionUpdateTask<V> task);

    /** 清空该键已有任务并以 restartTask 重新开始跟踪实体变更。 */
    void restartEntity(K key, SessionUpdateTask<V> restartTask);

}
