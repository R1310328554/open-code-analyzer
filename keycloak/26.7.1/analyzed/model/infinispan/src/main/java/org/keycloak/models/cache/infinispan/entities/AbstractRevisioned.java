package org.keycloak.models.cache.infinispan.entities;

import org.keycloak.common.util.Time;
import org.keycloak.models.cache.CachedObject;


/**
 * 带版本号的 Infinispan 缓存实体抽象基类。
 * <p>
 * 实现 {@link Revisioned} 与 {@link CachedObject}，持有实体 ID、revision 计数与写入缓存时的时间戳，
 * 供集群失效与乐观并发控制使用。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class AbstractRevisioned implements Revisioned, CachedObject {
    /** 实体唯一标识。 */
    private final String id;
    /** 当前 revision 版本号，用于缓存失效判定。 */
    private long revision;
    /** 写入缓存时的毫秒时间戳。 */
    private final long cacheTimestamp = Time.currentTimeMillis();

    /** 以指定 revision 与 ID 构造缓存实体。 */
    public AbstractRevisioned(long revision, String id) {
        this.revision = revision;
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getRevision() {
        return revision;
    }

    @Override
    public void setRevision(long revision) {
        this.revision = revision;
    }

    /**
     * 返回该条目写入缓存的时间戳。
     *
     * @return 缓存写入时的毫秒时间戳
     */
    @Override
    public long getCacheTimestamp() {
        return cacheTimestamp;
    }
}
