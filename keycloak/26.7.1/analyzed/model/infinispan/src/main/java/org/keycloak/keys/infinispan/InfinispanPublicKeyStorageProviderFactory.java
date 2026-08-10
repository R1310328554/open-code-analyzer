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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;

import org.keycloak.Config;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.keys.PublicKeyStorageProvider;
import org.keycloak.keys.PublicKeyStorageProviderFactory;
import org.keycloak.keys.PublicKeyStorageUtils;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.provider.ProviderEventListener;

import org.infinispan.Cache;
import org.jboss.logging.Logger;

/**
 * 基于 Infinispan 的公钥存储 SPI 工厂。
 * <p>
 * 管理分布式公钥缓存、并发拉取去重，并在客户端/身份提供者变更时触发缓存失效。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InfinispanPublicKeyStorageProviderFactory implements PublicKeyStorageProviderFactory {

    private static final Logger log = Logger.getLogger(InfinispanPublicKeyStorageProviderFactory.class);

    /** SPI 提供者 ID，对应 {@code infinispan} 实现。 */
    public static final String PROVIDER_ID = "infinispan";

    /** 公钥条目 Infinispan 缓存（延迟初始化）。 */
    private volatile Cache<String, PublicKeysEntry> keysCache;

    /** 按缓存键跟踪进行中的公钥拉取任务，避免并发重复下载。 */
    private final Map<String, FutureTask<PublicKeysEntry>> tasksInProgress = new ConcurrentHashMap<>();

    /** 两次公钥刷新请求之间的最小间隔（秒），用于缓解外部端点 DoS。 */
    private int minTimeBetweenRequests;
    /** 通过“获取全部公钥”方式拉取时的最大缓存时长（秒）。 */
    private int maxCacheTime;

    /** 创建公钥存储提供者实例。 */
    @Override
    public PublicKeyStorageProvider create(KeycloakSession session) {
        lazyInit(session);
        return new InfinispanPublicKeyStorageProvider(session, keysCache, tasksInProgress, minTimeBetweenRequests, maxCacheTime);
    }

    /** 返回 minTimeBetweenRequests 与 maxCacheTime 的可配置元数据。 */
    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                    .name("minTimeBetweenRequests")
                    .type("int")
                    .helpText("Minimum interval in seconds between two requests to retrieve the new public keys. "
                            + "The server will always try to download new public keys when a single key is requested and not found. "
                            + "However it will avoid the download if the previous refresh was done less than 10 seconds ago (by default). "
                            + "This behavior is used to avoid DoS attacks against the external keys endpoint.")
                    .defaultValue(10)
                    .add()
                .property()
                    .name("maxCacheTime")
                    .type("int")
                    .helpText("Maximum interval in seconds that keys are cached when they are retrieved via all keys methods. "
                            + "When all keys for the entry are retrieved there is no way to detect if a key is missing "
                            + "(different to the case when the key is retrieved via ID for example). "
                            + "In that situation this option forces a refresh from time to time. "
                            + "This time can be overriden by the protocol (for example using cacheDuration or validUntil in the SAML descriptor). "
                            + "Default 24 hours.")
                    .defaultValue(24*60*60)
                    .add()
                .build();
    }

    /** 双重检查锁定初始化 keys 缓存。 */
    private void lazyInit(KeycloakSession session) {
        if (keysCache == null) {
            synchronized (this) {
                if (keysCache == null) {
                    this.keysCache = session.getProvider(InfinispanConnectionProvider.class).getCache(InfinispanConnectionProvider.KEYS_CACHE_NAME);
                }
            }
        }
    }

    /** 从配置读取请求间隔与最大缓存时间。 */
    @Override
    public void init(Config.Scope config) {
        // 按名称或谓词查单个公钥时，缺失键场景下限制刷新频率
        minTimeBetweenRequests = config.getInt("minTimeBetweenRequests", 10);

        // 通过 getKeys 拉取全部公钥时，定期强制刷新以检测缺失键
        maxCacheTime = config.getInt("maxCacheTime", 24*60*60); // 24 小时

        log.debugf("minTimeBetweenRequests is %d maxCacheTime is %d", minTimeBetweenRequests, maxCacheTime);
    }

    /** 注册客户端/身份提供者变更监听，自动失效相关公钥缓存条目。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        factory.register(new ProviderEventListener() {

            @Override
            public void onEvent(ProviderEvent event) {
                if (keysCache == null) {
                    return;
                }

                SessionAndKeyHolder cacheKey = getCacheKeyToInvalidate(event);
                if (cacheKey != null) {
                    log.debugf("Invalidating %s from keysCache", cacheKey);
                    InfinispanPublicKeyStorageProvider provider = (InfinispanPublicKeyStorageProvider) cacheKey.session.getProvider(PublicKeyStorageProvider.class, getId());
                    for (String ck : cacheKey.cacheKeys) provider.addInvalidation(ck);
                }
            }

        });
    }

    /** 根据模型变更事件解析需要失效的公钥缓存键。 */
    private SessionAndKeyHolder getCacheKeyToInvalidate(ProviderEvent event) {
        ArrayList<String> cacheKeys = new ArrayList<>();
        String cacheKey = null;
        if (event instanceof ClientModel.ClientUpdatedEvent) {
            ClientModel.ClientUpdatedEvent eventt = (ClientModel.ClientUpdatedEvent) event;
            cacheKey = PublicKeyStorageUtils.getClientModelCacheKey(eventt.getUpdatedClient().getRealm().getId(), eventt.getUpdatedClient().getId(), JWK.Use.SIG);
            cacheKeys.add(cacheKey);
            cacheKey = PublicKeyStorageUtils.getClientModelCacheKey(eventt.getUpdatedClient().getRealm().getId(), eventt.getUpdatedClient().getId(), JWK.Use.ENCRYPTION);
            cacheKeys.add(cacheKey);
            return new SessionAndKeyHolder(eventt.getKeycloakSession(), cacheKeys);
        } else if (event instanceof ClientModel.ClientRemovedEvent) {
            ClientModel.ClientRemovedEvent eventt = (ClientModel.ClientRemovedEvent) event;
            cacheKey = PublicKeyStorageUtils.getClientModelCacheKey(eventt.getClient().getRealm().getId(), eventt.getClient().getId(), JWK.Use.SIG);
            cacheKeys.add(cacheKey);
            cacheKey = PublicKeyStorageUtils.getClientModelCacheKey(eventt.getClient().getRealm().getId(), eventt.getClient().getId(), JWK.Use.ENCRYPTION);
            cacheKeys.add(cacheKey);
            return new SessionAndKeyHolder(eventt.getKeycloakSession(), cacheKeys);
        } else if (event instanceof RealmModel.IdentityProviderUpdatedEvent) {
            RealmModel.IdentityProviderUpdatedEvent eventt = (RealmModel.IdentityProviderUpdatedEvent) event;
            cacheKey = PublicKeyStorageUtils.getIdpModelCacheKey(eventt.getRealm().getId(), eventt.getUpdatedIdentityProvider().getInternalId());
            cacheKeys.add(cacheKey);
            return new SessionAndKeyHolder(eventt.getKeycloakSession(), cacheKeys);
        } else if (event instanceof RealmModel.IdentityProviderRemovedEvent) {
            RealmModel.IdentityProviderRemovedEvent eventt = (RealmModel.IdentityProviderRemovedEvent) event;
            cacheKey = PublicKeyStorageUtils.getIdpModelCacheKey(eventt.getRealm().getId(), eventt.getRemovedIdentityProvider().getInternalId());
            cacheKeys.add(cacheKey);
            return new SessionAndKeyHolder(eventt.getKeycloakSession(), cacheKeys);
        } else {
            return null;
        }
    }

    /** 携带会话与待失效缓存键列表的辅助容器。 */
    private static class SessionAndKeyHolder {
        /** 触发失效的 Keycloak 会话。 */
        private final KeycloakSession session;
        /** 待失效的公钥缓存键集合。 */
        private final ArrayList<String> cacheKeys;

        public SessionAndKeyHolder(KeycloakSession session, ArrayList<String> cacheKeys) {
            this.session = session;
            this.cacheKeys = cacheKeys;
        }
    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
