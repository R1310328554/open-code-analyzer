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

package org.keycloak.models.sessions.infinispan.entities;

import org.keycloak.common.util.MultiSiteUtils;
import org.keycloak.models.sessions.infinispan.changes.SessionEntityWrapper;

import org.infinispan.api.annotations.indexing.Basic;
import org.infinispan.protostream.annotations.ProtoField;

/**
 * 会话实体的抽象基类，表示存入 Infinispan 缓存的会话数据对象。
 * <p>
 * {@code InfinispanChangelogBasedTransaction} 通过 Infinispan 的 {@code replace()} 做冲突合并，
 * 因此子类<b>必须</b>正确实现 {@link #hashCode()} 与 {@link #equals(java.lang.Object)}。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public abstract class SessionEntity {

    /** 所属 realm 的 ID。 */
    private String realmId;
    /** 是否为离线会话（仅持久化会话环境支持）。 */
    private boolean isOffline;

    /**
     * 返回所属 realm 的 ID。
     *
     * @return realm ID
     */
    @ProtoField(1)
    @Basic
    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public SessionEntity() {
    }

    protected SessionEntity(String realmId) {
        this.realmId = realmId;
    }

    @Deprecated(since = "26.4", forRemoval = true)
    // 已不再使用；默认将远程实体与本地元数据合并
    public SessionEntityWrapper mergeRemoteEntityWithLocalEntity(SessionEntityWrapper localEntityWrapper) {
        if (localEntityWrapper == null) {
            return new SessionEntityWrapper<>(this);
        } else {
            return new SessionEntityWrapper<>(localEntityWrapper.getLocalMetadata(), this);
        }
    }

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    public boolean isOffline() {
        if (!MultiSiteUtils.isPersistentSessionsEnabled()) {
            throw new IllegalArgumentException("Offline flags are not supported in non-persistent-session environments.");
        }
        return isOffline;
    }

    public void setOffline(boolean offline) {
        if (!MultiSiteUtils.isPersistentSessionsEnabled()) {
            throw new IllegalArgumentException("Offline flags are not supported in non-persistent-session environments.");
        }
        isOffline = offline;
    }

    /** 子类可覆盖：是否在合并/更新时评估删除条件。 */
    public boolean shouldEvaluateRemoval() {
        return false;
    }
}
