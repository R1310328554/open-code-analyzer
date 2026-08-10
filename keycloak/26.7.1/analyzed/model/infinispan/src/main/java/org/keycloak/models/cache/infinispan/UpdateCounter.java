package org.keycloak.models.cache.infinispan;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 本地缓存版本计数器，用于跟踪缓存修订号。
 * <p>
 * 每次缓存条目失效时递增，供 {@link CacheManager} 判断条目是否过期。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class UpdateCounter {

    /** 原子递增的本地版本计数器。 */
    private final AtomicLong counter = new AtomicLong();

    /** 返回当前版本号（不递增）。 */
    public long current() {
        return counter.get();
    }

    /** 递增并返回新版本号。 */
    public long next() {
        return counter.incrementAndGet();
    }

}
