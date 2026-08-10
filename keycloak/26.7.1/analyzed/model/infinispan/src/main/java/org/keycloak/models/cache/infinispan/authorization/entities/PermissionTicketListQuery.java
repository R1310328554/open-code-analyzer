package org.keycloak.models.cache.infinispan.authorization.entities;

import java.util.HashSet;
import java.util.Set;

import org.keycloak.models.cache.infinispan.entities.AbstractRevisioned;

/**
 * 权限票据列表查询结果的 Infinispan 缓存实体。
 * <p>
 * 以缓存键为 ID，存储匹配的权限票据 ID 集合，实现 {@link PermissionTicketQuery}。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PermissionTicketListQuery extends AbstractRevisioned implements PermissionTicketQuery {

    /** 查询命中的权限票据 ID 集合。 */
    private final Set<String> permissions;
    /** 所属资源服务器 ID。 */
    private final String serverId;

    /** 构造单条权限票据的列表查询缓存（如按名称+用户查询）。 */
    public PermissionTicketListQuery(long revision, String id, String permissionId, String serverId) {
        super(revision, id);
        this.serverId = serverId;
        permissions = new HashSet<>();
        permissions.add(permissionId);
    }
    /** 构造多条权限票据的列表查询缓存。 */
    public PermissionTicketListQuery(long revision, String id, Set<String> permissions, String serverId) {
        super(revision, id);
        this.serverId = serverId;
        this.permissions = permissions;
    }

    @Override
    public String getResourceServerId() {
        return serverId;
    }

    /** 返回查询命中的权限票据 ID 集合。 */
    public Set<String> getPermissions() {
        return permissions;
    }

    /** 当缓存键或资源服务器 ID 出现在失效集合中时返回 true。 */
    @Override
    public boolean isInvalid(Set<String> invalidations) {
        return invalidations.contains(getId()) || invalidations.contains(getResourceServerId());
    }
}
