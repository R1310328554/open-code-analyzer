package org.keycloak.models.cache.infinispan.entities;

import java.util.Set;

/**
 * 客户端查询缓存条目的标记接口。
 * <p>
 * 继承 {@link InRealm}，提供查询命中的客户端 ID 集合访问能力。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ClientQuery extends InRealm {
    /** 返回查询命中的客户端 ID 集合。 */
    Set<String> getClients();
}
