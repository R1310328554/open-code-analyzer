package org.keycloak.models.cache.infinispan.authorization.entities;

import java.util.Set;

/**
 * 按作用域 ID 索引的资源列表查询缓存键。
 *
 * <p>在 {@link ResourceListQuery} 基础上附加 {@link InScope} 维度，
 * 用于缓存“某资源服务器下、指定作用域关联的资源 ID 集合”的查询结果。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ResourceScopeListQuery extends ResourceListQuery implements InScope {

    /** 关联的作用域 ID。 */
    private final String scopeId;

    /** 构造按作用域过滤的资源列表查询缓存键。 */
    public ResourceScopeListQuery(long revision, String id, String scopeId, Set<String> resources, String serverId) {
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
