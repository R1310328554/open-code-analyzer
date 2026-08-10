package org.keycloak.models.cache.infinispan.stream;

import java.util.Map;
import java.util.function.Predicate;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.cache.infinispan.entities.CachedClient;
import org.keycloak.models.cache.infinispan.entities.CachedClientScope;
import org.keycloak.models.cache.infinispan.entities.CachedCompositeRoles;
import org.keycloak.models.cache.infinispan.entities.CachedGroup;
import org.keycloak.models.cache.infinispan.entities.CachedRole;
import org.keycloak.models.cache.infinispan.entities.Revisioned;
import org.keycloak.models.cache.infinispan.entities.RoleQuery;

import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 缓存流过滤谓词：判断缓存条目是否与指定角色存在关联（角色、复合角色、组映射、客户端 scope 等）。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@ProtoTypeId(Marshalling.HAS_ROLE_PREDICATE)
public class HasRolePredicate implements Predicate<Map.Entry<String, Revisioned>> {
    /** 待匹配的角色 ID。 */
    private String role;

    /** 创建可链式配置的谓词实例。 */
    public static HasRolePredicate create() {
        return new HasRolePredicate();
    }

    /** 设置要查找的角色 ID。 */
    public HasRolePredicate role(String role) {
        this.role = role;
        return this;
    }

    @ProtoField(1)
    String getRole() {
        return role;
    }

    void setRole(String role) {
        this.role = role;
    }

    /** 在多种缓存实体类型中检查是否引用给定角色。 */
    @Override
    public boolean test(Map.Entry<String, Revisioned> entry) {
        Object value = entry.getValue();
        return (value instanceof CachedRole cachedRole && cachedRole.getCachedComposites().ids().contains(role)) ||
                (value instanceof CachedCompositeRoles cachedCompositeRoles && (cachedCompositeRoles.getCompositeIds().contains(role) || cachedCompositeRoles.getParentIds().contains(role))) ||
                (value instanceof CachedGroup cachedGroup && cachedGroup.getCachedRoleMappings().contains(role)) ||
                (value instanceof RoleQuery roleQuery && roleQuery.getRoles().contains(role)) ||
                (value instanceof CachedClient cachedClient && cachedClient.getScope().contains(role)) ||
                (value instanceof CachedClientScope cachedClientScope && cachedClientScope.getScope().contains(role));
    }

}
