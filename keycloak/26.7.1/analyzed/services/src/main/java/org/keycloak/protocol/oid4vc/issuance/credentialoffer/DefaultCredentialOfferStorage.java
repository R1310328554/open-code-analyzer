/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.oid4vc.issuance.credentialoffer;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.protocol.oid4vc.OID4VCLoginProtocolFactory;
import org.keycloak.util.JsonSerialization;

import org.jboss.logging.Logger;

/**
 * 基于 {@link org.keycloak.models.SingleUseObjectProvider} 的 {@link CredentialOfferStorage} 默认实现。
 * <p>通过 singleUseObjects API 利用 Infinispan 分布式缓存，支持集群与跨数据中心部署；
 * 过期由底层缓存 TTL 机制自动处理，避免内存泄漏。</p>
 */
class DefaultCredentialOfferStorage implements CredentialOfferStorage {

    /** 日志记录器。 */
    private static final Logger LOGGER = Logger.getLogger(OID4VCLoginProtocolFactory.class);

    /** singleUseObject 条目中 JSON 载荷的键名。 */
    private static final String ENTRY_KEY = "json";

    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;

    /** @param session Keycloak 会话 */
    DefaultCredentialOfferStorage(KeycloakSession session) {
        this.session = session;
    }

    /**
     * 计算从当前时间到过期时间的剩余存活秒数。
     * @param expiresAt 绝对过期时间（Unix 秒）
     * @return 剩余秒数；已过期返回 0
     */
    private long calculateLifespanSeconds(long expiresAt) {
        long currentTime = Time.currentTime();
        long lifespan = expiresAt - currentTime;
        
        // 已过期或即将过期则跳过存储
        // 避免写入不可用的条目
        return Math.max(0, lifespan);

    }

    @Override
    public void putOfferState(CredentialOfferState entry) {

        // 已过期则跳过存储（与 InfinispanSingleUseObjectProviderFactory 一致）
        long lifespanSeconds = calculateLifespanSeconds(entry.getExpiresAt());
        if (lifespanSeconds <= 0) {
            LOGGER.warnf("Credential offer state not stored - expired already");
            return;
        }
        
        SingleUseObjectProvider singleUseObjects = session.singleUseObjects();
        String offerId = entry.getCredentialsOfferId();
        String entryJson = JsonSerialization.valueAsString(entry);

        singleUseObjects.put(offerId, lifespanSeconds, Map.of(ENTRY_KEY, entryJson));
    }

    @Override
    public CredentialOfferState getOfferStateById(String offerId) {
        return Optional.ofNullable(session.singleUseObjects().get(offerId))
                .map(o -> o.get(ENTRY_KEY))
                .map(o -> JsonSerialization.valueFromString(o, CredentialOfferState.class))
                .orElse(null);
    }

    @Override
    public CredentialOfferState getOfferStateByNonce(String nonce) {
        String offerId = CredentialOfferLookupKey.extractOfferId(nonce);
        if (offerId == null) {
            return null;
        }
        CredentialOfferState offerState = getOfferStateById(offerId);
        if (offerState == null || !Objects.equals(nonce, offerState.getNonce())) {
            return null;
        }
        return offerState;
    }

    @Override
    public void removeOfferState(CredentialOfferState offerState) {
        session.singleUseObjects().remove(offerState.getCredentialsOfferId());
    }
}
