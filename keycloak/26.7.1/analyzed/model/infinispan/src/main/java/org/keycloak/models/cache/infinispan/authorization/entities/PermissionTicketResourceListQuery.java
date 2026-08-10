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

import java.util.Set;

/**
 * 按资源 ID 索引的权限票据列表查询缓存键。
 *
 * <p>在 {@link PermissionTicketListQuery} 基础上附加 {@link InResource} 维度，
 * 用于缓存“某资源服务器下、指定资源关联的权限票据 ID 集合”的查询结果。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class PermissionTicketResourceListQuery extends PermissionTicketListQuery implements InResource {

    /** 关联的资源 ID。 */
    private final String resourceId;

    /** 构造按资源过滤的权限票据列表查询缓存键。 */
    public PermissionTicketResourceListQuery(long revision, String id, String resourceId, Set<String> permissions, String serverId) {
        super(revision, id, permissions, serverId);
        this.resourceId = resourceId;
    }

    /** {@inheritDoc} 资源或父级查询键失效时返回 true。 */
    @Override
    public boolean isInvalid(Set<String> invalidations) {
        return super.isInvalid(invalidations) || invalidations.contains(getResourceId());
    }

    /** {@inheritDoc} */
    @Override
    public String getResourceId() {
        return resourceId;
    }
}
