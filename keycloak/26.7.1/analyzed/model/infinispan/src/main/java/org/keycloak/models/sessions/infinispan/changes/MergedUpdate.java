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

import java.util.LinkedList;
import java.util.List;

import org.keycloak.models.sessions.infinispan.entities.SessionEntity;
import org.keycloak.models.sessions.infinispan.util.SessionTimeouts;

import org.jboss.logging.Logger;

/**
 * 将同一事务内多条 {@link SessionUpdateTask} 合并为单次缓存操作的更新任务。
 * <p>
 * 提交时按序执行子任务、合并 {@link CacheOperation}，并携带 lifespan/maxIdle 元数据。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MergedUpdate<S extends SessionEntity> implements SessionUpdateTask<S> {

    private static final Logger logger = Logger.getLogger(MergedUpdate.class);

    /** 待合并的子更新任务列表。 */
    private final List<SessionUpdateTask<S>> childUpdates = new LinkedList<>();
    private CacheOperation operation;
    /** 缓存条目 lifespan（毫秒）。 */
    private final long lifespanMs;
    /** 缓存条目 max-idle（毫秒）。 */
    private final long maxIdleTimeMs;

    private MergedUpdate(CacheOperation operation, long lifespanMs, long maxIdleTimeMs) {
        this.operation = operation;
        this.lifespanMs = lifespanMs;
        this.maxIdleTimeMs = maxIdleTimeMs;
    }

    @Override
    public void runUpdate(S session) {
        for (SessionUpdateTask<S> child : childUpdates) {
            child.runUpdate(session);
        }
    }

    @Override
    public boolean shouldRemove(S session) {
        for (SessionUpdateTask<S> child : childUpdates) {
            if(child.shouldRemove(session)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public CacheOperation getOperation() {
        return operation;
    }

    public long getLifespanMs() {
        return lifespanMs;
    }

    public long getMaxIdleTimeMs() {
        return maxIdleTimeMs;
    }


    /**
     * 将子任务列表合并为单个 {@link MergedUpdate}；空列表或已过期条目返回 null 或 REMOVE。
     */
    public static <S extends SessionEntity> MergedUpdate<S> computeUpdate(List<SessionUpdateTask<S>> childUpdates, SessionEntityWrapper<S> sessionWrapper, long lifespanMs, long maxIdleTimeMs) {
        if (childUpdates == null || childUpdates.isEmpty()) {
            return null;
        }

        MergedUpdate<S> result = null;
        S session = sessionWrapper.getEntity();
        for (SessionUpdateTask<S> child : childUpdates) {
            if (result == null) {
                CacheOperation operation = child.getOperation();

                if (lifespanMs == SessionTimeouts.ENTRY_EXPIRED_FLAG || maxIdleTimeMs == SessionTimeouts.ENTRY_EXPIRED_FLAG) {
                    operation = CacheOperation.REMOVE;
                    logger.tracef("Entry '%s' is expired. Will remove it from the cache", sessionWrapper);
                }

                result = new MergedUpdate<>(operation, lifespanMs, maxIdleTimeMs);
                result.childUpdates.add(child);
            } else {

                // 合并缓存操作类型
                result.operation = result.getOperation().merge(child.getOperation(), session);

                // REMOVE 为终态，后续子任务可丢弃
                if (result.operation == CacheOperation.REMOVE) {
                    result = new MergedUpdate<>(result.operation, lifespanMs, maxIdleTimeMs);
                    result.childUpdates.add(child);
                    return result;
                }

                result.childUpdates.add(child);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "MergedUpdate" + childUpdates;
    }

}
