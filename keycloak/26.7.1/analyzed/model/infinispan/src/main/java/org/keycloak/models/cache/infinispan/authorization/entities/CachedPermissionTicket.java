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

package org.keycloak.models.cache.infinispan.authorization.entities;

import org.keycloak.authorization.model.PermissionTicket;
import org.keycloak.authorization.model.Policy;
import org.keycloak.models.cache.infinispan.entities.AbstractRevisioned;

/**
 * 权限票据（PermissionTicket）的 Infinispan 缓存快照实体。
 * <p>
 * 存储票据所有者、请求者、资源/作用域 ID、授予状态与时间戳等不可变字段，
 * 实现 {@link InResourceServer} 以支持按资源服务器批量失效。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class CachedPermissionTicket extends AbstractRevisioned implements InResourceServer {

    /** 权限票据请求者（被授权用户）ID。 */
    private final String requester;
    /** 资源所有者 ID。 */
    private String owner;
    /** 所属资源服务器 ID。 */
    private String resourceServerId;
    /** 关联资源 ID。 */
    private String resourceId;
    /** 关联作用域 ID，可为 null。 */
    private String scopeId;
    /** 是否已授予。 */
    private boolean granted;
    /** 创建时间戳。 */
    private Long createdTimestamp;
    /** 授予时间戳。 */
    private Long grantedTimestamp;
    /** 关联策略 ID，可为 null。 */
    private String policy;

    /** 从 PermissionTicket 模型构造缓存快照。 */
    public CachedPermissionTicket(long revision, PermissionTicket permissionTicket) {
        super(revision, permissionTicket.getId());
        this.owner = permissionTicket.getOwner();
        requester = permissionTicket.getRequester();
        this.resourceServerId = permissionTicket.getResourceServer().getId();
        this.resourceId = permissionTicket.getResource().getId();
        if (permissionTicket.getScope() != null) {
            this.scopeId = permissionTicket.getScope().getId();
        }
        this.granted = permissionTicket.isGranted();
        createdTimestamp = permissionTicket.getCreatedTimestamp();
        grantedTimestamp = permissionTicket.getGrantedTimestamp();
        Policy policy = permissionTicket.getPolicy();
        if (policy != null) {
            this.policy = policy.getId();
        }
    }

    public String getOwner() {
        return owner;
    }

    public String getRequester() {
        return requester;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getScopeId() {
        return scopeId;
    }

    public boolean isGranted() {
        return granted;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public Long getGrantedTimestamp() {
        return grantedTimestamp;
    }

    public String getResourceServerId() {
        return this.resourceServerId;
    }

    public String getPolicy() {
        return policy;
    }
}
