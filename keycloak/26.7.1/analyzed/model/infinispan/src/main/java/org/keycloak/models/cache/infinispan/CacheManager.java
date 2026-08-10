package org.keycloak.models.cache.infinispan;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.keycloak.cluster.ClusterEvent;
import org.keycloak.cluster.ClusterProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.cache.infinispan.entities.Revisioned;
import org.keycloak.models.cache.infinispan.events.InvalidationEvent;

import org.infinispan.Cache;
import org.jboss.logging.Logger;

/**
 * Realm 缓存管理器抽象基类，自行实现乐观锁与版本校验。
 * <p>
 * 采用失效（Invalidation）主缓存 + 本地 revision 计数缓存的双缓存结构。
 * 集群节点仅在本节点持有条目时才会收到失效事件；{@link #getCurrentRevision(String)}
 * 通过写入占位失效键解决跨节点版本不同步导致的脏读问题。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class CacheManager {

    /** 本地 revision 版本号缓存。 */
    protected final Cache<String, Long> revisions;
    /** 主 Invalidation 实体缓存。 */
    protected final Cache<String, Revisioned> cache;
    /** 全局递增计数器，用于生成新版本号。 */
    protected final UpdateCounter counter = new UpdateCounter();

    /** 绑定主缓存与 revision 缓存。 */
    public CacheManager(Cache<String, Revisioned> cache, Cache<String, Long> revisions) {
        this.cache = cache;
        this.revisions = revisions;
    }

    protected abstract Logger getLogger();

    public Cache<String, Revisioned> getCache() {
        return cache;
    }

    /** 返回当前全局计数器值。 */
    public long getCurrentCounter() {
        return counter.current();
    }

    /** 获取条目当前 revision；若不存在则初始化并写入占位失效键。 */
    public long getCurrentRevision(String id) {
        Long revision = revisions.get(id);
        if (revision == null) {
            revision = counter.current();
        }

        return revision;
    }

    /** 结束 revision 批处理（忽略异常）。 */
    public void endRevisionBatch() {
        try {
            revisions.endBatch(true);
        } catch (Exception e) {
        }

    }

    /** 按 ID 读取缓存实体，并校验 revision 是否仍有效。 */
    public <T extends Revisioned> T get(String id, Class<T> type) {
        Revisioned o = cache.get(id);
        if (o == null) {
            return null;
        }
        Long rev = revisions.get(id);
        if (rev == null) {
            if (getLogger().isTraceEnabled()) {
                getLogger().tracev("get() missing rev {0}", id);
            }
            /* revision 缓存中已无此 ID
             ** => 同步从主缓存移除，恢复一致状态以便重新缓存最新版本
             */
            cache.remove(id);
            return null;
        }
        long oRev = o.getRevision();
        if (rev > oRev) {
            if (getLogger().isTraceEnabled()) {
                getLogger().tracev("get() rev: {0} o.rev: {1}", rev, oRev);
            }
            // 主缓存条目 revision 落后，移除过期数据
            cache.remove(id);
            return null;
        }
        return type.isInstance(o) ? type.cast(o) : null;
    }

    /** 从主缓存移除条目并递增 revision 版本。 */
    public Object invalidateObject(String id) {
        Revisioned removed = cache.remove(id);

        if (getLogger().isTraceEnabled()) {
            getLogger().tracef("Removed key='%s', value='%s' from cache", id, removed);
        }

        bumpVersion(id);
        return removed;
    }

    /** 将指定 ID 的 revision 递增到下一个全局计数。 */
    protected void bumpVersion(String id) {
        long next = counter.next();
        revisions.put(id, next);
    }

    /** 在事务提交时将 revisioned 对象写入缓存（无 TTL）。 */
    public void addRevisioned(Revisioned object, long startupRevision) {
        addRevisioned(object, startupRevision, -1);
    }

    /** 在事务提交时将 revisioned 对象写入缓存，可选毫秒级存活时间。 */
    public void addRevisioned(Revisioned object, long startupRevision, long lifespan) {
        //startRevisionBatch();
        String id = object.getId();
        try {
            //revisions.getAdvancedCache().lock(id);
            Long rev = revisions.get(id);
            if (rev == null) {
                rev = counter.current();
                revisions.put(id, rev);
            }
            revisions.startBatch();
            if (!revisions.getAdvancedCache().lock(id)) {
                if (getLogger().isTraceEnabled()) {
                    getLogger().tracev("Could not obtain version lock: {0}", id);
                }
                return;
            }
            rev = revisions.get(id);
            if (rev == null) {
                return;
            }
            if (rev > startupRevision) { // revision 已超前于事务起点，期间有其他事务更新，跳过缓存
                if (getLogger().isTraceEnabled()) {
                    getLogger().tracev("Skipped cache. Current revision {0}, Transaction start revision {1}", object.getRevision(), startupRevision);
                }
                return;
            }
            if (rev.equals(object.getRevision())) {
                put(id, object, lifespan);
                return;
            }
            if (rev > object.getRevision()) { // revision 领先于对象版本，不缓存
                if (getLogger().isTraceEnabled()) getLogger().tracev("Skipped cache. Object revision {0}, Cache revision {1}", object.getRevision(), rev);
                return;
            }
            // revision 缓存值低于对象 revision，同步 revision 后写入缓存
            revisions.put(id, object.getRevision());
            put(id, object, lifespan);
        } finally {
            endRevisionBatch();
        }

    }

    /** 清空主缓存与 revision 缓存。 */
    public void clear() {
        cache.clear();
        revisions.clear();
    }

    /** 按谓词扫描缓存条目并将匹配键加入失效集合。 */
    public void addInvalidations(Predicate<Map.Entry<String, Revisioned>> predicate, Set<String> invalidations) {
        Iterator<Map.Entry<String, Revisioned>> it = getEntryIterator(predicate);
        while (it.hasNext()) {
            invalidations.add(it.next().getKey());
        }
    }

    private void put(String id, Revisioned object, long lifespan) {
        if (lifespan < 0) {
            cache.putForExternalRead(id, object);
        } else if (lifespan > 0) {
            cache.putForExternalRead(id, object, lifespan, TimeUnit.MILLISECONDS);
        }
    }

    private Iterator<Map.Entry<String, Revisioned>> getEntryIterator(Predicate<Map.Entry<String, Revisioned>> predicate) {
        return cache
                .entrySet()
                .stream()
                .filter(predicate).iterator();
    }


    /** 向集群广播失效事件列表。 */
    public void sendInvalidationEvents(KeycloakSession session, Collection<InvalidationEvent> invalidationEvents, String eventKey) {
        session.getProvider(ClusterProvider.class)
                .notify(eventKey, invalidationEvents, true);
    }


    /** 处理收到的集群失效事件，批量失效本地缓存条目。 */
    public void invalidationEventReceived(InvalidationEvent event) {
        Set<String> invalidations = new HashSet<>();

        addInvalidationsFromEvent(event, invalidations);

        getLogger().debugf("[%s] Invalidating %d cache items after received event %s", cache.getCacheManager().getAddress(), invalidations.size(), event);

        for (String invalidation : invalidations) {
            invalidateObject(invalidation);
        }
    }

    protected abstract void addInvalidationsFromEvent(InvalidationEvent event, Set<String> invalidations);

    /** 将单个缓存键加入待失效集合。 */
    public void invalidateCacheKey(String key, Set<String> invalidations) {
        invalidations.add(key);
    }

    /**
     * 处理集群级全量清缓存事件，清空本节点缓存管理器。
     *
     * @param ignored 集群事件（未使用）
     */
    public void onClearEvent(ClusterEvent ignored) {
        clear();
    }

    /**
     * 处理集群级失效事件，委托 {@link #invalidationEventReceived(InvalidationEvent)}。
     *
     * @param event 须为 {@link InvalidationEvent} 的集群事件
     */
    public void onInvalidateEvent(ClusterEvent event) {
        assert event instanceof InvalidationEvent;
        invalidationEventReceived((InvalidationEvent) event);
    }
}
