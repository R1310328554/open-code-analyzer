package org.keycloak.models.cache.infinispan.authorization.entities;

import java.util.HashSet;
import java.util.Set;

import org.keycloak.models.cache.infinispan.entities.AbstractRevisioned;

/**
 * 策略列表查询缓存键，缓存某资源服务器下匹配查询的策略 ID 集合。
 *
 * <p>实现 {@link PolicyQuery}，以 revision 跟踪缓存版本，并在策略或资源服务器变更时判定失效。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PolicyListQuery extends AbstractRevisioned implements PolicyQuery {
    /** 查询结果包含的策略 ID 集合。 */
    private final Set<String> policies;
    /** 所属资源服务器 ID。 */
    private final String serverId;

    /** 构造仅包含单个策略 ID 的列表查询缓存键。 */
    public PolicyListQuery(long revision, String id, String policyId, String serverId) {
        super(revision, id);
        this.serverId = serverId;
        policies = new HashSet<>();
        policies.add(policyId);
    }

    /** 构造包含完整策略 ID 集合的列表查询缓存键。 */
    public PolicyListQuery(long revision, String id, Set<String> policies, String serverId) {
        super(revision, id);
        this.serverId = serverId;
        this.policies = policies;
    }

    /** {@inheritDoc} */
    @Override
    public String getResourceServerId() {
        return serverId;
    }

    /** 返回缓存的策略 ID 集合。 */
    public Set<String> getPolicies() {
        return policies;
    }

    /** {@inheritDoc} 查询键本身或资源服务器失效时返回 true。 */
    @Override
    public boolean isInvalid(Set<String> invalidations) {
        return invalidations.contains(getId()) || invalidations.contains(getResourceServerId());
    }
}
