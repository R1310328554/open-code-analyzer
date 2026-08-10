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
 * 按作用域 ID 索引的策略列表查询缓存键。
 *
 * <p>在 {@link PolicyListQuery} 基础上附加 {@link InScope} 维度，
 * 用于缓存“某资源服务器下、指定作用域关联的策略 ID 集合”的查询结果。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class PolicyScopeListQuery extends PolicyListQuery implements InScope {

    /** 关联的作用域 ID。 */
    private final String scopeId;

    /** 构造按作用域过滤的策略列表查询缓存键。 */
    public PolicyScopeListQuery(long revision, String id, String scopeId, Set<String> resources, String serverId) {
        super(revision, id, resources, serverId);
        this.scopeId = scopeId;
    }

    /** {@inheritDoc} */
    @Override
    public String getScopeId() {
        return scopeId;
    }

    /** {@inheritDoc} 作用域或父级查询键失效时返回 true。 */
    @Override
    public boolean isInvalid(Set<String> invalidations) {
        return super.isInvalid(invalidations) || invalidations.contains(getScopeId());
    }
}
