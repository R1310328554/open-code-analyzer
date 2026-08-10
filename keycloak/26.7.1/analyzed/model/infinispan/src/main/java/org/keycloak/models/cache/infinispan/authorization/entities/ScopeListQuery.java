package org.keycloak.models.cache.infinispan.authorization.entities;

import java.util.HashSet;
import java.util.Set;

import org.keycloak.models.cache.infinispan.entities.AbstractRevisioned;

/**
 * 作用域列表查询缓存键，缓存某资源服务器下匹配查询的作用域 ID 集合。
 *
 * <p>实现 {@link InResourceServer}，以 revision 跟踪缓存版本。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ScopeListQuery extends AbstractRevisioned implements InResourceServer {
    /** 查询结果包含的作用域 ID 集合。 */
    private final Set<String> scopes;
    /** 所属资源服务器 ID。 */
    private final String serverId;

    /** 构造仅包含单个作用域 ID 的列表查询缓存键。 */
    public ScopeListQuery(long revision, String id, String scopeId, String serverId) {
        super(revision, id);
        this.serverId = serverId;
        scopes = new HashSet<>();
        scopes.add(scopeId);
    }

    /** 构造包含完整作用域 ID 集合的列表查询缓存键。 */
    public ScopeListQuery(long revision, String id, Set<String> scopes, String serverId) {
        super(revision, id);
        this.serverId = serverId;
        this.scopes = scopes;
    }

    /** {@inheritDoc} */
    @Override
    public String getResourceServerId() {
        return serverId;
    }

    /** 返回缓存的作用域 ID 集合。 */
    public Set<String> getScopes() {
        return scopes;
    }
}
