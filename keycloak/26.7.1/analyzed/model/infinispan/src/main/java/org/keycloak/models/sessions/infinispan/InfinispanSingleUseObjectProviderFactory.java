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

package org.keycloak.models.sessions.infinispan;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.common.util.Time;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.infinispan.util.InfinispanUtils;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.models.SingleUseObjectProviderFactory;
import org.keycloak.models.session.RevokedTokenPersisterProvider;
import org.keycloak.models.sessions.infinispan.entities.SingleUseObjectValueEntity;
import org.keycloak.models.sessions.infinispan.transaction.InfinispanTransactionProvider;
import org.keycloak.models.utils.PostMigrationEvent;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.provider.ServerInfoAwareProviderFactory;

import org.infinispan.commons.api.BasicCache;

import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.ACTION_TOKEN_CACHE;

/**
 * Infinispan 单次使用对象 Provider 工厂。
 * <p>
 * 管理 action token 缓存，可选从 {@link RevokedTokenPersisterProvider} 预加载已撤销令牌。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InfinispanSingleUseObjectProviderFactory implements SingleUseObjectProviderFactory<InfinispanSingleUseObjectProvider>, EnvironmentDependentProviderFactory, ServerInfoAwareProviderFactory {

    /** 配置项：是否在重启后持久化撤销令牌。 */
    public static final String CONFIG_PERSIST_REVOKED_TOKENS = "persistRevokedTokens";
    public static final boolean DEFAULT_PERSIST_REVOKED_TOKENS = true;
    public static final String LOADED = "loaded" + SingleUseObjectProvider.REVOKED_KEY;

    protected BasicCache<String, SingleUseObjectValueEntity> singleUseObjectCache;

    private volatile boolean initialized;
    private boolean persistRevokedTokens;

    @Override
    public Set<Class<? extends Provider>> dependsOn() {
        return Set.of(InfinispanConnectionProvider.class, InfinispanTransactionProvider.class);
    }

    @Override
    public InfinispanSingleUseObjectProvider create(KeycloakSession session) {
        initialize(session);
        return new InfinispanSingleUseObjectProvider(session, singleUseObjectCache, persistRevokedTokens, createTransaction(session));
    }

    @Override
    public void init(Config.Scope config) {
        persistRevokedTokens = config.getBoolean(CONFIG_PERSIST_REVOKED_TOKENS, DEFAULT_PERSIST_REVOKED_TOKENS);
    }

    private void initialize(KeycloakSession session) {
        if (persistRevokedTokens && !initialized) {
            synchronized (this) {
                if (!initialized) {
                    RevokedTokenPersisterProvider provider = session.getProvider(RevokedTokenPersisterProvider.class);
                    if (singleUseObjectCache.get(LOADED) == null) {
                        // 集群中多实例并行加载相同数据可接受，重复写入无害
                        provider.getAllRevokedTokens().forEach(revokedToken -> {
                            long lifespanSeconds = revokedToken.expiry() - Time.currentTime();
                            if (lifespanSeconds > 0) {
                                singleUseObjectCache.put(revokedToken.tokenId() + SingleUseObjectProvider.REVOKED_KEY, new SingleUseObjectValueEntity(Collections.emptyMap()),
                                         Time.toMillis(lifespanSeconds), TimeUnit.MILLISECONDS);
                            }
                        });
                        singleUseObjectCache.put(LOADED, new SingleUseObjectValueEntity(Collections.emptyMap()));
                    }
                    initialized = true;
                }
            }
        }
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // 须在此 eagerly 初始化缓存，否则监听器要等到首次访问才注册，为时已晚
        if (singleUseObjectCache == null) {
            try (var session = factory.create()) {
                InfinispanConnectionProvider connections = session.getProvider(InfinispanConnectionProvider.class);
                singleUseObjectCache = connections.getCache(ACTION_TOKEN_CACHE);
            }
        }

        if (persistRevokedTokens) {
            factory.register(event -> {
                if (event instanceof PostMigrationEvent pme) {
                    KeycloakSessionFactory sessionFactory = pme.getFactory();
                    try (KeycloakSession session = sessionFactory.create()) {
                        // 启动时预加载撤销令牌，避免首请求拥塞
                        initialize(session);
                    }
                }
            });
        }
    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return InfinispanUtils.EMBEDDED_PROVIDER_ID;
    }

    @Override
    public int order() {
        return InfinispanUtils.PROVIDER_ORDER;
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return !Profile.isFeatureEnabled(Profile.Feature.STATELESS) && InfinispanUtils.isEmbeddedInfinispan();
    }

    @Override
    public Map<String, String> getOperationalInfo() {
        Map<String, String> info = new HashMap<>();
        info.put(CONFIG_PERSIST_REVOKED_TOKENS, Boolean.toString(persistRevokedTokens));
        return info;
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        ProviderConfigurationBuilder builder = ProviderConfigurationBuilder.create();

        builder.property()
                .name(CONFIG_PERSIST_REVOKED_TOKENS)
                .type("boolean")
                .helpText("If revoked tokens are stored persistently across restarts")
                .defaultValue(DEFAULT_PERSIST_REVOKED_TOKENS)
                .add();

        return builder.build();
    }

    private static InfinispanKeycloakTransaction createTransaction(KeycloakSession session) {
        InfinispanTransactionProvider transactionProvider = session.getProvider(InfinispanTransactionProvider.class);
        InfinispanKeycloakTransaction tx = new InfinispanKeycloakTransaction();
        transactionProvider.registerTransaction(tx);
        return tx;
    }

}
