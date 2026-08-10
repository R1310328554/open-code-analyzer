package org.keycloak.models.cache.infinispan.entities;

import java.util.Set;

/**
 * Realm 列表查询缓存条目的标记接口。
 * <p>
 * 提供查询命中的 realm ID 集合访问能力。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface RealmQuery {
    /** 返回查询命中的 realm ID 集合。 */
    Set<String> getRealms();
}
