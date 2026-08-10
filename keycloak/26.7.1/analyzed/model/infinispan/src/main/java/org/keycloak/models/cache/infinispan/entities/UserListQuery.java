package org.keycloak.models.cache.infinispan.entities;

import java.util.Set;

import org.keycloak.models.RealmModel;

/**
 * 用户列表查询的 Infinispan 缓存实体。
 * <p>
 * 缓存单个用户 ID 及其所属领域，实现 {@link UserQuery}，
 * 供按用户查询时复用结果（旧版多用户集合构造函数已废弃）。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UserListQuery extends AbstractRevisioned implements UserQuery {
    /** 目标用户 ID。 */
    private final String userId;
    /** 所属领域 ID。 */
    private final String realm;

    /** @deprecated 自 26.5 起废弃，请改用单用户 ID 构造函数。 */
    @Deprecated(forRemoval = true, since = "26.5")
    public UserListQuery(long revisioned, String id, RealmModel realm, Set<String> users) {
        super(revisioned, id);
        this.realm = realm.getId();
        this.userId = users.stream().findAny().orElse(null);
    }

    /** 以单个用户 ID 构造用户列表查询缓存条目。 */
    public UserListQuery(long revisioned, String id, RealmModel realm, String userId) {
        super(revisioned, id);
        this.realm = realm.getId();
        this.userId = userId;
    }

    /** 返回包含单个用户 ID 的集合。 */
    @Override
    public Set<String> getUsers() {
        return Set.of(userId);
    }

    /** 返回所属领域 ID。 */
    @Override
    public String getRealm() {
        return realm;
    }

    /** 返回便于调试的字符串表示。 */
    @Override
    public String toString() {
        return "UserListQuery{" +
                "id='" + getId() + "'" +
                "realm='" + realm + '\'' +
                '}';
    }
}
