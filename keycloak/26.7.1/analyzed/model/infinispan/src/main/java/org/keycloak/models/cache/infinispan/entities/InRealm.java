package org.keycloak.models.cache.infinispan.entities;

/**
 * 标记缓存实体隶属于某 realm 的接口。
 * <p>
 * 继承 {@link Revisioned}，为 realm 域下的各类缓存条目提供统一的 realm 归属访问。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface InRealm extends Revisioned {
    /** 返回所属 realm 的唯一标识。 */
    String getRealm();
}
