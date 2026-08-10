/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.keys.infinispan;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.keycloak.cluster.ClusterProvider;
import org.keycloak.common.util.Time;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.PublicKeysWrapper;
import org.keycloak.keys.PublicKeyLoader;
import org.keycloak.keys.PublicKeyStorageProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransaction;

import org.infinispan.Cache;
import org.jboss.logging.Logger;


/**
 * 基于 Infinispan 缓存的外部公钥存储提供者。
 * <p>
 * 缓存 IdP/JWKS 等外部公钥，支持按 kid/algorithm 查询、过期刷新、
 * 请求节流（minTimeBetweenRequests）及事务提交后集群级失效广播。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InfinispanPublicKeyStorageProvider implements PublicKeyStorageProvider {

    private static final Logger log = Logger.getLogger(InfinispanPublicKeyStorageProvider.class);

    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;

    /** 公钥条目 Infinispan 缓存（modelKey → PublicKeysEntry）。 */
    private final Cache<String, PublicKeysEntry> keys;

    /** 正在进行的公钥加载任务（防止并发重复请求）。 */
    private final Map<String, FutureTask<PublicKeysEntry>> tasksInProgress;

    /** 两次远程加载之间的最小间隔（秒）。 */
    private final int minTimeBetweenRequests ;
    /** 无过期时间密钥的最大缓存时长（秒）。 */
    private final int maxCacheTime;

    /** 事务提交/回滚后待广播失效的缓存键集合。 */
    private final Set<String> invalidations = new HashSet<>();

    /** 是否已注册事务完成回调。 */
    private boolean transactionEnlisted = false;

    public InfinispanPublicKeyStorageProvider(KeycloakSession session, Cache<String, PublicKeysEntry> keys, Map<String, FutureTask<PublicKeysEntry>> tasksInProgress,
            int minTimeBetweenRequests, int maxCacheTime) {
        this.session = session;
        this.keys = keys;
        this.tasksInProgress = tasksInProgress;
        this.minTimeBetweenRequests = minTimeBetweenRequests;
        this.maxCacheTime = maxCacheTime;
    }

    /** 登记缓存键失效，在事务完成时批量广播。 */
    void addInvalidation(String cacheKey) {
        if (!transactionEnlisted) {
            session.getTransactionManager().enlistAfterCompletion(getAfterTransaction());
            transactionEnlisted = true;
        }

        this.invalidations.add(cacheKey);
    }


    /** 返回事务完成时执行的失效回调（commit/rollback 均触发）。 */
    protected KeycloakTransaction getAfterTransaction() {
        return new KeycloakTransaction() {

            @Override
            public void begin() {
            }

            @Override
            public void commit() {
                runInvalidations();
            }

            @Override
            public void rollback() {
                runInvalidations();
            }

            @Override
            public void setRollbackOnly() {
            }

            @Override
            public boolean getRollbackOnly() {
                return false;
            }

            @Override
            public boolean isActive() {
                return true;
            }
        };
    }


    /** 移除本地缓存条目并广播集群级失效事件。 */
    protected void runInvalidations() {
        ClusterProvider cluster = session.getProvider(ClusterProvider.class);

        var events = invalidations.stream()
                .peek(keys::remove)
                .map(PublicKeyStorageInvalidationEvent::create)
                .toList();
        cluster.notify(InfinispanCachePublicKeyProviderFactory.PUBLIC_KEY_STORAGE_INVALIDATION_EVENT, events, true);
    }

    @Override
    public KeyWrapper getFirstPublicKey(String modelKey, String algorithm, PublicKeyLoader loader) {
        return getPublicKey(modelKey, null, algorithm, loader);
    }

    @Override
    public KeyWrapper getPublicKey(String modelKey, String kid, String algorithm, PublicKeyLoader loader) {
        PublicKeysEntry entry = keys.get(modelKey);
        int lastRequestTime = entry == null? 0 : entry.getLastRequestTime();
        int currentTime = Time.currentTime();
        boolean isSendingRequestAllowed = currentTime > lastRequestTime + minTimeBetweenRequests;

        // 缓存命中且未过期：有 kid 时直接查，无 kid 时仅在节流期内使用缓存
        if (!isExpired(entry, currentTime) && (kid != null || !isSendingRequestAllowed)) {
            KeyWrapper publicKey = entry.getCurrentKeys().getKeyByKidAndAlg(kid, algorithm);
            if (publicKey != null) {
                // 返回副本，避免调用方修改缓存中的密钥对象
                return publicKey.cloneKey();
            }
        }

        PublicKeysEntry updatedEntry = reloadKeys(modelKey, entry, currentTime, loader);
        entry = updatedEntry == null? entry : updatedEntry;
        KeyWrapper publicKey = entry == null? null : entry.getCurrentKeys().getKeyByKidAndAlg(kid, algorithm);
        if (publicKey != null) {
            // 返回副本，避免调用方修改缓存中的密钥对象
            return publicKey.cloneKey();
        }

        List<String> availableKids = entry == null? Collections.emptyList() : entry.getCurrentKeys().getKids();
        log.warnf("PublicKey wasn't found in the storage. Requested kid: '%s' . Available kids: '%s'", kid, availableKids);

        return null;
    }

    /**
     * 按谓词查找首个匹配的公钥。
     * <p>
     * 缓存未过期时先查缓存；未命中且允许重新加载时再次尝试。
     *
     * @param modelKey  模型键
     * @param predicate 密钥匹配谓词
     * @param loader    远程公钥加载器
     * @return 匹配的公钥，或 null
     */
    @Override
    public KeyWrapper getFirstPublicKey(String modelKey, Predicate<KeyWrapper> predicate, PublicKeyLoader loader) {
        PublicKeysEntry entry = keys.get(modelKey);
        int currentTime = Time.currentTime();
        if (!isExpired(entry, currentTime)) {
            // 缓存有效时直接尝试匹配
            KeyWrapper key = entry.getCurrentKeys().getKeyByPredicate(predicate);
            if (key != null) {
                return key.cloneKey();
            }
        }
        // 缓存未命中时，若节流允许则重新加载后再查
        entry = reloadKeys(modelKey, entry, currentTime, loader);
        if (entry != null) {
            KeyWrapper key = entry.getCurrentKeys().getKeyByPredicate(predicate);
            if (key != null) {
                return key.cloneKey();
            }
        }
        return null;
    }

    /**
     * 返回 modelKey 下的全部公钥列表。
     * <p>
     * 使用 maxCacheTime 在无过期时间的密钥上触发预刷新。
     *
     * @param modelKey 模型键
     * @param loader   远程公钥加载器
     * @return 公钥列表（副本）
     */
    @Override
    public List<KeyWrapper> getKeys(String modelKey, PublicKeyLoader loader) {
        PublicKeysEntry entry = keys.get(modelKey);
        int currentTime = Time.currentTime();

        if (isExpired(entry, currentTime) || (hasNoExpiration(entry) && currentTime > entry.getLastRequestTime() + maxCacheTime)) {
            // 过期或无 TTL 且超过 maxCacheTime 时主动刷新
            PublicKeysEntry updatedEntry = reloadKeys(modelKey, entry, currentTime, loader);
            if (updatedEntry != null) {
                entry = updatedEntry;
            }
        }

        return entry == null
                ? Collections.emptyList()
                : entry.getCurrentKeys().getKeys().stream().map(KeyWrapper::cloneKey).collect(Collectors.toList());
    }

    @Override
    public boolean reloadKeys(String modelKey, PublicKeyLoader loader) {
        PublicKeysEntry entry = keys.get(modelKey);
        int currentTime = Time.currentTime();
        return reloadKeys(modelKey, entry, currentTime, loader) != null;
    }

    /** 判断缓存条目是否无过期时间。 */
    private boolean hasNoExpiration(PublicKeysEntry entry) {
        return entry == null || entry.getCurrentKeys().getExpirationTime() == null;
    }

    /** 判断缓存条目是否已过期（基于 JWKS expirationTime）。 */
    private boolean isExpired(PublicKeysEntry entry, int currentTime) {
        if (entry == null) {
            return true;
        }

        if (entry.getCurrentKeys().getExpirationTime() != null) {
            return currentTime > TimeUnit.MILLISECONDS.toSeconds(entry.getCurrentKeys().getExpirationTime());
        }

        return false;
    }

    /**
     * 从远程加载公钥并写入缓存（含并发去重）。
     * <p>
     * 同一 modelKey 的并发请求共享单个 FutureTask，避免重复远程调用。
     */
    private PublicKeysEntry reloadKeys(String modelKey, PublicKeysEntry entry, int currentTime, PublicKeyLoader loader) {
        // 检查是否允许发起远程请求（节流）
        if (entry == null || currentTime > entry.getLastRequestTime() + minTimeBetweenRequests) {
            WrapperCallable wrapperCallable = new WrapperCallable(modelKey, loader);
            FutureTask<PublicKeysEntry> task = new FutureTask<>(wrapperCallable);
            FutureTask<PublicKeysEntry> existing = tasksInProgress.putIfAbsent(modelKey, task);

            if (existing == null) {
                log.debugf("Reloading keys for model key '%s'.", modelKey);
                task.run();
            } else {
                task = existing;
            }

            try {
                return task.get();
            } catch (ExecutionException ee) {
                throw new RuntimeException("Error when loading public keys: " + ee.getMessage(), ee);
            } catch (InterruptedException ie) {
                throw new RuntimeException("Error. Interrupted when loading public keys", ie);
            } finally {
                // 本线程插入的任务负责清理 tasksInProgress
                if (existing == null) {
                    tasksInProgress.remove(modelKey);
                }
            }
        } else {
            log.warnf("Won't load the keys for model '%s'. Last request time was %d", modelKey, entry.getLastRequestTime());
        }
        return null;
    }

    @Override
    public void close() {

    }

    /** 包装 {@link PublicKeyLoader} 的 Callable，负责实际远程加载与缓存写入。 */
    private class WrapperCallable implements Callable<PublicKeysEntry> {

        /** 公钥模型键。 */
        private final String modelKey;
        /** 实际公钥加载委托。 */
        private final PublicKeyLoader delegate;

        public WrapperCallable(String modelKey, PublicKeyLoader delegate) {
            this.modelKey = modelKey;
            this.delegate = delegate;
        }

        @Override
        public PublicKeysEntry call() throws Exception {
            PublicKeysEntry entry = keys.get(modelKey);

            int lastRequestTime = entry==null ? 0 : entry.getLastRequestTime();
            int currentTime = Time.currentTime();

            // 再次检查节流（并发场景下其他任务可能已完成加载）
            if (currentTime > lastRequestTime + minTimeBetweenRequests) {

                PublicKeysWrapper publicKeys = delegate.loadKeys();

                if (log.isDebugEnabled()) {
                    log.debugf("Public keys retrieved successfully for model %s. New kids: %s", modelKey, publicKeys.getKids());
                }

                entry = new PublicKeysEntry(currentTime, publicKeys);

                if (publicKeys.getExpirationTime() != null) {
                    keys.put(modelKey, entry, publicKeys.getExpirationTime(), TimeUnit.MILLISECONDS);
                } else {
                    keys.put(modelKey, entry);
                }
            }
            return entry;
        }
    }
}
