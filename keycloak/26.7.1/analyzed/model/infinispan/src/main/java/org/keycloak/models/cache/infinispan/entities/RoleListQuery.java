package org.keycloak.models.cache.infinispan.entities;

import java.util.HashSet;
import java.util.Set;

import org.keycloak.models.RealmModel;

/**
 * 角色列表查询的 Infinispan 缓存实体。
 * <p>
 * 缓存一组角色名称及其所属领域与客户端上下文，实现 {@link RoleQuery} 与 {@link InClient}，
 * 供批量角色查询结果复用。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RoleListQuery extends AbstractRevisioned implements RoleQuery, InClient {
    /** 查询结果中的角色名称集合。 */
    private final Set<String> roles;
    /** 所属领域 ID。 */
    private final String realm;
    /** 所属客户端 UUID（领域角色时为 null）。 */
    private String client;

    /** 以角色名称集合构造列表查询缓存条目。 */
    public RoleListQuery(long revisioned, String id, RealmModel realm, Set<String> roles) {
        super(revisioned, id);
        this.realm = realm.getId();
        this.roles = roles;
    }

    /** 以单个角色名称构造列表查询缓存条目。 */
    public RoleListQuery(long revisioned, String id, RealmModel realm, String role) {
        super(revisioned, id);
        this.realm = realm.getId();
        this.roles = new HashSet<>();
        this.roles.add(role);
    }

    /** 以角色集合与客户端 UUID 构造列表查询缓存条目。 */
    public RoleListQuery(long revision, String id, RealmModel realm, Set<String> roles, String client) {
        this(revision, id, realm, roles);
        this.client = client;
    }

    /** 以单个角色名称与客户端 UUID 构造列表查询缓存条目。 */
    public RoleListQuery(long revision, String id, RealmModel realm, String role, String client) {
        this(revision, id, realm, role);
        this.client = client;
    }

    /** 返回缓存的角色名称集合。 */
    @Override
    public Set<String> getRoles() {
        return roles;
    }

    /** 返回所属领域 ID。 */
    @Override
    public String getRealm() {
        return realm;
    }

    /** 返回所属客户端 UUID。 */
    @Override
    public String getClientId() {
        return client;
    }

    /** 返回便于调试的字符串表示。 */
    @Override
    public String toString() {
        return "RoleListQuery{" +
                "id='" + getId() + "'" +
                ", realm='" + realm + '\'' +
                ", clientUuid='" + client + '\'' +
                '}';
    }
}
