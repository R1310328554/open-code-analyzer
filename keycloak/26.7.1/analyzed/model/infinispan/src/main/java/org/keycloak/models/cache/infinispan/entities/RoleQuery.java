package org.keycloak.models.cache.infinispan.entities;

import java.util.Set;

/**
 * 角色查询缓存实体的公共接口。
 * <p>
 * 继承 {@link InRealm}，声明返回查询涉及的角色名称集合。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface RoleQuery extends InRealm {
    /** 返回查询涉及的角色名称集合。 */
    Set<String> getRoles();
}
