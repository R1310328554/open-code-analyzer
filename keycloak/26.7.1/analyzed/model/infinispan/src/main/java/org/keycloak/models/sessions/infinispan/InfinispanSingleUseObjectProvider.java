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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelException;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.models.session.RevokedTokenPersisterProvider;
import org.keycloak.models.sessions.infinispan.entities.SingleUseObjectValueEntity;

import org.infinispan.commons.api.BasicCache;

/**
 * 基于 Infinispan action token 缓存的单次使用对象 Provider。
 * <p>
 * 通过 {@link InfinispanKeycloakTransaction} 延迟写入，支持撤销令牌持久化到
 * {@link RevokedTokenPersisterProvider}。撤销令牌键不可读、不可删、不可替换。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InfinispanSingleUseObjectProvider implements SingleUseObjectProvider {

    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;
    /** action token 单次使用对象缓存。 */
    private final BasicCache<String, SingleUseObjectValueEntity> singleUseObjectCache;
    /** 是否将撤销令牌同步持久化到数据库。 */
    private final boolean persistRevokedTokens;
    /** 缓存写入事务。 */
    private final InfinispanKeycloakTransaction tx;

    public InfinispanSingleUseObjectProvider(KeycloakSession session, BasicCache<String, SingleUseObjectValueEntity> singleUseObjectCache, boolean persistRevokedTokens, InfinispanKeycloakTransaction tx) {
        this.session = session;
        this.singleUseObjectCache = singleUseObjectCache;
        this.persistRevokedTokens = persistRevokedTokens;
        this.tx = tx;
    }

    @Override
    public void put(String key, long lifespanSeconds, Map<String, String> notes) {
        Objects.requireNonNull(key);
        if (lifespanSeconds <= 0) {
            throw new IllegalArgumentException("lifespanSeconds must be positive");
        }
        SingleUseObjectValueEntity tokenValue = new SingleUseObjectValueEntity(notes);
        tx.put(singleUseObjectCache, key, tokenValue, lifespanSeconds, TimeUnit.SECONDS);
        if (persistRevokedTokens && key.endsWith(REVOKED_KEY)) {
            if (!notes.isEmpty()) {
                throw new ModelException("Notes are not supported for revoked tokens");
            }
            session.getProvider(RevokedTokenPersisterProvider.class).revokeToken(key.substring(0, key.length() - REVOKED_KEY.length()), lifespanSeconds);
        }
    }

    @Override
    public Map<String, String> get(String key) {
        Objects.requireNonNull(key);
        if (persistRevokedTokens && key.endsWith(REVOKED_KEY)) {
            throw new ModelException("Revoked tokens can't be retrieved");
        }

        SingleUseObjectValueEntity singleUseObjectValueEntity = tx.get(singleUseObjectCache, key);
        return singleUseObjectValueEntity != null ? singleUseObjectValueEntity.getNotes() : null;
    }

    @Override
    public Map<String, String> remove(String key) {
        Objects.requireNonNull(key);
        if (persistRevokedTokens && key.endsWith(REVOKED_KEY)) {
           throw new ModelException("Revoked tokens can't be removed");
        }

        // 先 get 再 remove，以便在 Infinispan 状态转移期间仍能返回被删值
        // where it might not return the value in all cases.
        // 待 Infinispan #16703 修复后可移除此变通方案
        var data = singleUseObjectCache.get(key);
        if (data == null) {
            return null;
        }
        return singleUseObjectCache.remove(key, data) ? data.getNotes() : null;
    }

    @Override
    public boolean replace(String key, Map<String, String> notes) {
        Objects.requireNonNull(key);
        if (persistRevokedTokens && key.endsWith(REVOKED_KEY)) {
            throw new ModelException("Revoked tokens can't be replaced");
        }

        return singleUseObjectCache.replace(key, new SingleUseObjectValueEntity(notes)) != null;
    }

    @Override
    public boolean putIfAbsent(String key, long lifespanInSeconds) {
        Objects.requireNonNull(key);
        if (lifespanInSeconds <= 0) {
            throw new IllegalArgumentException("lifespanInSeconds must be positive");
        }
        SingleUseObjectValueEntity tokenValue = new SingleUseObjectValueEntity(null);
        SingleUseObjectValueEntity existing = singleUseObjectCache.putIfAbsent(key, tokenValue, lifespanInSeconds, TimeUnit.SECONDS);
        if (persistRevokedTokens && key.endsWith(REVOKED_KEY)) {
            session.getProvider(RevokedTokenPersisterProvider.class).revokeToken(key.substring(0, key.length() - REVOKED_KEY.length()), lifespanInSeconds);
        }
        return existing == null;
    }

    @Override
    public boolean contains(String key) {
        Objects.requireNonNull(key);
        return singleUseObjectCache.containsKey(key);
    }

    @Override
    public void close() {

    }
}
