/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.authorization.policy.provider.js;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

import org.keycloak.scripting.EvaluatableScriptAdapter;

/**
 * JavaScript 策略脚本的 LRU 缓存，支持条目过期与并发写入互斥。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ScriptCache {

    /**
     * 哈希表负载因子。
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /** 底层 LRU 缓存（按访问顺序淘汰最久未用条目） */
    private final Map<String, CacheEntry> cache;

    /** 写入互斥标志，避免并发修改缓存结构 */
    private final AtomicBoolean writing = new AtomicBoolean(false);

    /** 条目最大存活时间（毫秒）；{@code -1} 表示永不过期 */
    private final long maxAge;

    /**
     * 创建缓存实例（条目永不过期）。
     *
     * @param maxEntries 缓存最大条目数
     */
    public ScriptCache(int maxEntries) {
        this(maxEntries, -1);
    }

    /**
     * 创建缓存实例。
     *
     * @param maxEntries 缓存最大条目数
     * @param maxAge 条目最大存活时间（毫秒）；{@code -1} 表示永不过期
     */
    public ScriptCache(final int maxEntries, long maxAge) {
        cache = Collections.synchronizedMap(new LinkedHashMap<String, CacheEntry>(16, DEFAULT_LOAD_FACTOR, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return cache.size()  > maxEntries;
            }
        });
        this.maxAge = maxAge;
    }

    /**
     * 获取或计算脚本适配器；过期条目会被移除并重新加载。
     *
     * @param id 缓存键（通常为策略 ID）
     * @param function 缓存未命中时的加载函数
     * @return 可执行脚本适配器
     */
    public EvaluatableScriptAdapter computeIfAbsent(String id, Function<String, EvaluatableScriptAdapter> function) {
        try {
            EvaluatableScriptAdapter adapter = removeIfExpired(cache.get(id));

            if (adapter != null) {
                return adapter;
            }

            if (parkForWriteAndCheckInterrupt()) {
                return null;
            }

            CacheEntry entry = cache.computeIfAbsent(id, key -> new CacheEntry(key, function.apply(id), maxAge));

            if (entry != null) {
                return entry.value();
            }

            return null;
        } finally {
            writing.lazySet(false);
        }
    }

    /**
     * 从缓存中移除指定键（如策略删除时调用）。
     *
     * @param key 缓存键
     */
    public void remove(String key) {
        try {
            if (parkForWriteAndCheckInterrupt()) {
                return;
            }

            cache.remove(key);
        } finally {
            writing.lazySet(false);
        }
    }

    /** 若条目已过期则删除并返回 {@code null} */
    private EvaluatableScriptAdapter removeIfExpired(CacheEntry cached) {
        if (cached == null) {
            return null;
        }

        if (cached.isExpired()) {
            remove(cached.key());
            return null;
        }

        return cached.value();
    }

    /** 自旋等待写入锁；若线程被中断则放弃写入 */
    private boolean parkForWriteAndCheckInterrupt() {
        while (!writing.compareAndSet(false, true)) {
            LockSupport.parkNanos(1L);
            if (Thread.interrupted()) {
                return true;
            }
        }
        return false;
    }

    /** 缓存条目：持有脚本适配器与过期时间戳 */
    private static final class CacheEntry {

        final String key;
        final EvaluatableScriptAdapter value;
        final long expiration;

        CacheEntry(String key, EvaluatableScriptAdapter value, long maxAge) {
            this.key = key;
            this.value = value;
            if(maxAge == -1) {
                expiration = -1;
            } else {
                expiration = System.currentTimeMillis() + maxAge;
            }
        }

        String key() {
            return key;
        }

        EvaluatableScriptAdapter value() {
            return value;
        }

        boolean isExpired() {
            return expiration != -1 ? System.currentTimeMillis() > expiration : false;
        }
    }
}
