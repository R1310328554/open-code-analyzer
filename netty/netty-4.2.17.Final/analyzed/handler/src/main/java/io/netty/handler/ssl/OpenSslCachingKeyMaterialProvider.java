/*
 * Copyright 2018 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.ssl;

import io.netty.buffer.ByteBufAllocator;
import io.netty.util.IllegalReferenceCountException;

import javax.net.ssl.X509KeyManager;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link OpenSslKeyMaterialProvider} that will cache the {@link OpenSslKeyMaterial} to reduce the overhead
 * of parsing the chain and the key for generation of the material.
 * <p>
 * Cache reads are lock-free ({@link ConcurrentHashMap#get} + {@code retain()});
 * if a concurrent eviction releases the material in between, {@code retain()}
 * detects the dead reference count and the read falls back to a cache miss.
 * Mutations (insert, evict, destroy) use {@link ConcurrentHashMap#compute} /
 * {@link ConcurrentHashMap#computeIfPresent} for atomicity.
 *
 * <p>按证书别名缓存已解析的 {@link OpenSslKeyMaterial}，减少 SNI/客户端证书切换时的 PEM 解析开销。</p>
 */
final class OpenSslCachingKeyMaterialProvider extends OpenSslKeyMaterialProvider {

    /** 缓存条目上限；超出时先驱逐 KeyManager 中已不存在的 alias */
    private final int maxCachedEntries;
    /** alias -> 引用计数的 OpenSslKeyMaterial */
    private final ConcurrentHashMap<String, OpenSslKeyMaterial> cache =
            new ConcurrentHashMap<String, OpenSslKeyMaterial>();
    /** destroy 后置 true，防止 destroy 后仍插入新条目 */
    private volatile boolean destroyed;

    OpenSslCachingKeyMaterialProvider(X509KeyManager keyManager, String password, int maxEntries) {
        super(keyManager, password);
        maxCachedEntries = maxEntries;
    }

    /**
     * Lock-free cache lookup. If a concurrent eviction releases the material between
     * {@code get} and {@code retain}, the dead reference count is detected and treated
     * as a cache miss.
     *
     * <p>无锁读缓存；并发驱逐导致 refCnt 为 0 时视为未命中。</p>
     */
    private OpenSslKeyMaterial getAndRetain(String alias) {
        OpenSslKeyMaterial m = cache.get(alias);
        if (m != null) {
            try {
                return m.retain();
            } catch (IllegalReferenceCountException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Atomically inserts material if absent, or retains the existing entry.
     */
    private OpenSslKeyMaterial putIfAbsentAndRetain(String alias, OpenSslKeyMaterial material) {
        return cache.compute(alias, (k, existing) -> {
            if (existing != null) {
                existing.retain();
                return existing;
            }
            material.retain();
            return material;
        });
    }

    /**
     * Atomically removes and releases the entry for the given alias.
     */
    private void removeAndRelease(String alias) {
        cache.computeIfPresent(alias, (k, v) -> {
            v.release();
            return null;
        });
    }

    private void evictStaleEntries() {
        // 移除 KeyManager 已不再提供的 alias 对应缓存
        for (String alias : cache.keySet()) {
            if (keyManager().getCertificateChain(alias) == null) {
                removeAndRelease(alias);
            }
        }
    }

    @Override
    OpenSslKeyMaterial chooseKeyMaterial(ByteBufAllocator allocator, String alias) throws Exception {
        OpenSslKeyMaterial material = getAndRetain(alias);
        if (material == null) {
            material = super.chooseKeyMaterial(allocator, alias);
            if (material == null) {
                return null;
            }

            if (cache.size() >= maxCachedEntries) {
                evictStaleEntries();
                if (cache.size() >= maxCachedEntries) {
                    return material;
                }
            }
            // 返回新创建的 material，或另一线程先插入时 retain 已有条目
            OpenSslKeyMaterial old = putIfAbsentAndRetain(alias, material);
            if (old != material) {
                material.release();
                material = old;
            } else if (destroyed) {
                // destroy 与 insert 竞态：帮助清理刚插入的条目
                removeAndReleaseAllEntries();
            }
        }
        return material;
    }

    int cacheSize() {
        return cache.size();
    }

    @Override
    void destroy() {
        destroyed = true;
        try {
            removeAndReleaseAllEntries();
        } finally {
            super.destroy();
        }
    }

    private void removeAndReleaseAllEntries() {
        do  {
            for (String alias : cache.keySet()) {
                removeAndRelease(alias);
            }
        } while (!cache.isEmpty());
    }
}
