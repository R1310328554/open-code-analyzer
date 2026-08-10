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

package org.keycloak.models.sessions.infinispan.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.keycloak.common.util.SecretGenerator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.sessions.infinispan.changes.CacheHolder;
import org.keycloak.sessions.StickySessionEncoderProvider;

import org.infinispan.Cache;
import org.infinispan.affinity.KeyAffinityService;
import org.infinispan.affinity.KeyAffinityServiceFactory;
import org.infinispan.affinity.KeyGenerator;
import org.jboss.logging.Logger;

/**
 * 为 Infinispan 缓存生成会话键，集群模式下可优先生成本节点拥有的键以支持粘性会话。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @deprecated 不再支持，即将移除。请改用 {@link CacheHolder#keyGenerator()}
 */
@Deprecated(since = "26.4", forRemoval = true)
public class InfinispanKeyGenerator {

    private static final Logger log = Logger.getLogger(InfinispanKeyGenerator.class);

    /** 按缓存名缓存的 KeyAffinityService 实例。 */
    private final Map<String, KeyAffinityService> keyAffinityServices = new ConcurrentHashMap<>();


    /** 生成 String 类型的缓存键。 */
    public String generateKeyString(KeycloakSession session, Cache<String, ?> cache) {
        return generateKey(session, cache, new StringKeyGenerator());
    }


    /** 生成 UUID 类型的缓存键。 */
    public UUID generateKeyUUID(KeycloakSession session, Cache<UUID, ?> cache) {
        return generateKey(session, cache, new UUIDKeyGenerator());
    }


    /**
     * 根据粘性会话策略生成键：若需本地键且为集群缓存，则返回本节点地址对应的亲和键。
     */
    protected <K> K generateKey(KeycloakSession session, Cache<K, ?> cache, KeyGenerator<K> keyGenerator) {
        String cacheName = cache.getName();

        // wantsLocalKey 为 true 表示粘性会话 cookie 未附带路由；此时希望键由本节点持有，
        // 以便外部负载均衡器绑定到本节点路由后可在本地查找该键。
        boolean wantsLocalKey = !session.getProvider(StickySessionEncoderProvider.class).shouldAttachRoute();

        if (wantsLocalKey && cache.getCacheConfiguration().clustering().cacheMode().isClustered()) {
            KeyAffinityService<K> keyAffinityService = keyAffinityServices.computeIfAbsent(cacheName, s -> {
                KeyAffinityService<K> k = createKeyAffinityService(cache, keyGenerator);
                log.debugf("Registered key affinity service for cache '%s'", cacheName);
                return k;
            });
            return keyAffinityService.getKeyForAddress(cache.getCacheManager().getAddress());
        } else {
            return keyGenerator.getKey();
        }

    }


    /** 创建 KeyAffinityService；推荐使用单线程执行器以保持线程处于 WAITING 状态。 */
    private <K> KeyAffinityService<K> createKeyAffinityService(Cache<K, ?> cache, KeyGenerator<K> keyGenerator) {
        // SingleThreadExecutor is recommended due it needs the single thread and leave it in the WAITING state
        return KeyAffinityServiceFactory.newLocalKeyAffinityService(
                cache,
                keyGenerator,
                Executors.newSingleThreadExecutor(),
                16);
    }


    /** 基于 {@link SecretGenerator} 的 UUID 键生成器。 */
    private static class UUIDKeyGenerator implements KeyGenerator<UUID> {

        @Override
        public UUID getKey() {
            return SecretGenerator.getInstance().generateSecureUUID();
        }
    }


    /** 基于 {@link SecretGenerator} 的 String 键生成器。 */
    private static class StringKeyGenerator implements KeyGenerator<String> {

        @Override
        public String getKey() {
            return SecretGenerator.getInstance().generateSecureID();
        }
    }
}
