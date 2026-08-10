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
 * 持久化会话更新任务的标记接口。
 * <p>
 * 扩展 {@link SessionUpdateTask}，额外声明会话是否为离线模式，
 * 供持久化事务区分在线/离线缓存与数据库写入路径。
 */
public interface PersistentSessionUpdateTask<S extends SessionEntity> extends SessionUpdateTask<S> {
    /** 该任务是否针对离线会话。 */
    boolean isOffline();
}
