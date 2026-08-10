/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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
 * 单条会话实体变更任务。
 * <p>
 * 在事务内就地修改实体并声明对应的 Infinispan 缓存操作类型，提交时由 {@link MergedUpdate} 合并。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface SessionUpdateTask<S extends SessionEntity> {

    /** 在当前实体副本上执行变更逻辑。 */
    void runUpdate(S entity);

    /** 变更后是否应删除实体（默认否）。 */
    default boolean shouldRemove(S entity) {
        return false;
    }

    /** 返回本任务对应的缓存操作类型。 */
    CacheOperation getOperation();

    /** Infinispan 缓存操作枚举，支持提交前合并。 */
    enum CacheOperation {

        ADD,
        ADD_IF_ABSENT, // 键已存在时抛出异常
        REMOVE,
        REPLACE;

        CacheOperation merge(CacheOperation other, SessionEntity entity) {
            if (this == REMOVE || other == REMOVE) {
                return REMOVE;
            }

            if (this == ADD | this == ADD_IF_ABSENT) {
                if (other == ADD | other == ADD_IF_ABSENT) {
                    throw new IllegalStateException("Illegal state. Task already in progress for session " + entity.toString());
                }

                return this;
            }

            // REPLACE 优先级最低，ADD/REMOVE 优先
            return REPLACE;
        }
    }
}
