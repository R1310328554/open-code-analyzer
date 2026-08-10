package org.keycloak.models.cache.infinispan.authorization.entities;

import java.util.HashSet;
import java.util.Set;

import org.keycloak.models.cache.infinispan.entities.AbstractRevisioned;

/**
 * 资源列表查询缓存键，缓存某资源服务器下匹配查询的资源 ID 集合。
 *
 * <p>实现 {@link ResourceQuery} 与 {@link InResourceServer}，以 revision 跟踪缓存版本，
 * 并在资源或资源服务器变更时判定失效。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ResourceListQuery extends AbstractRevisioned implements ResourceQuery, InResourceServer {
    /** 查询结果包含的资源 ID 集合。 */
    private final Set<String> resources;
    /** 所属资源服务器 ID。 */
    private final String serverId;

    /** 构造仅包含单个资源 ID 的列表查询缓存键。 */
    public ResourceListQuery(long revision, String id, String resourceId, String serverId) {
        super(revision, id);
        this.serverId = serverId;
        resources = new HashSet<>();
        resources.add(resourceId);
    }

    /** 构造包含完整资源 ID 集合的列表查询缓存键。 */
    public ResourceListQuery(long revision, String id, Set<String> resources, String serverId) {
        super(revision, id);
        this.serverId = serverId;
        this.resources = resources;
    }

    /** {@inheritDoc} */
    @Override
    public String getResourceServerId() {
        return serverId;
    }

    /** 返回缓存的资源 ID 集合。 */
    public Set<String> getResources() {
        return resources;
    }

    /** {@inheritDoc} 查询键本身或资源服务器失效时返回 true。 */
    @Override
    public boolean isInvalid(Set<String> invalidations) {
        return invalidations.contains(getId()) || invalidations.contains(getResourceServerId());
    }
}
