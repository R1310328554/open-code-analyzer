/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.models.sessions.infinispan.changes.SessionUpdateTask.CacheOperation;
import org.keycloak.models.sessions.infinispan.entities.SessionEntity;

/**
 * 预定义的同步会话更新任务工厂。
 * <p>
 * 提供无实体字段变更、仅声明缓存操作类型的可复用 {@link SessionUpdateTask} 实例。
 *
 * @author hmlnarik
 */
public class Tasks {

    private static final SessionUpdateTask<? extends SessionEntity> ADD_IF_ABSENT_SYNC = new SessionUpdateTask<>() {
        @Override
        public void runUpdate(SessionEntity entity) {
        }

        @Override
        public CacheOperation getOperation() {
            return CacheOperation.ADD_IF_ABSENT;
        }

    };

    private static final SessionUpdateTask<? extends SessionEntity> REMOVE_SYNC = new PersistentSessionUpdateTask<>() {
        @Override
        public void runUpdate(SessionEntity entity) {
        }

        @Override
        public CacheOperation getOperation() {
            return CacheOperation.REMOVE;
        }

        @Override
        public boolean isOffline() {
            return false;
        }
    };

    private static final SessionUpdateTask<? extends SessionEntity> OFFLINE_REMOVE_SYNC = new PersistentSessionUpdateTask<>() {
        @Override
        public void runUpdate(SessionEntity entity) {
        }

        @Override
        public CacheOperation getOperation() {
            return CacheOperation.REMOVE;
        }

        @Override
        public boolean isOffline() {
            return true;
        }
    };

    /**
     * 返回 {@link CacheOperation#ADD_IF_ABSENT} 类型的同步任务，不修改实体字段。
     * @param <S>
     * @return
     */
    public static <S extends SessionEntity> SessionUpdateTask<S> addIfAbsentSync() {
        return (SessionUpdateTask<S>) ADD_IF_ABSENT_SYNC;
    }

    /**
     * 返回在线会话 {@link CacheOperation#REMOVE} 类型的同步删除任务。
     * @param <S>
     * @return
     */
    public static <S extends SessionEntity> SessionUpdateTask<S> removeSync() {
        return (SessionUpdateTask<S>) REMOVE_SYNC;
    }

    /**
     * 返回 {@link CacheOperation#REMOVE} 类型的同步删除任务。
     *
     * @param offline 是否在离线会话缓存上执行
     * @param <S>
     * @return
     */
    public static <S extends SessionEntity> PersistentSessionUpdateTask<S> removeSync(boolean offline) {
        return offline ? (PersistentSessionUpdateTask<S>) OFFLINE_REMOVE_SYNC : (PersistentSessionUpdateTask<S>) REMOVE_SYNC;
    }


}
