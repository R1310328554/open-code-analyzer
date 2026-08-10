package org.keycloak.models.cache.infinispan.entities;

/**
 * Infinispan 缓存实体的 revision 版本控制接口。
 * <p>
 * 所有可缓存条目均持有 ID 与 revision 计数，
 * 供集群失效与乐观并发控制使用。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface Revisioned {
    /** 返回实体唯一标识。 */
    String getId();
    /** 返回当前 revision 版本号。 */
    long getRevision();
    /** 更新 revision 版本号。 */
    void setRevision(long revision);
}
