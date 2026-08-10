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

package org.keycloak.testsuite.rest;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;

import org.keycloak.Config.Scope;
import org.keycloak.crypto.KeyType;
import org.keycloak.crypto.KeyUse;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.protocol.oidc.grants.ciba.endpoints.ClientNotificationEndpointRequest;
import org.keycloak.representations.LogoutToken;
import org.keycloak.representations.adapters.action.LogoutAction;
import org.keycloak.representations.adapters.action.PushNotBeforeAction;
import org.keycloak.representations.adapters.action.TestAvailabilityAction;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;
import org.keycloak.testsuite.rest.representation.TestAuthenticationChannelRequest;

/**
 * 测试应用 {@link RealmResourceProvider} 工厂，维护共享队列与 OIDC 测试数据。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class TestApplicationResourceProviderFactory implements RealmResourceProviderFactory {

    /** 管理端登出动作队列。 */
    private BlockingQueue<LogoutAction> adminLogoutActions = new LinkedBlockingDeque<>();
    /** 后端通道登出令牌队列。 */
    private BlockingQueue<String> backChannelLogoutTokens = new LinkedBlockingDeque<>();
    /** 前端通道登出令牌队列。 */
    private BlockingQueue<LogoutToken> frontChannelLogoutTokens = new LinkedBlockingDeque<>();
    /** Push-not-before 动作队列。 */
    private BlockingQueue<PushNotBeforeAction> pushNotBeforeActions = new LinkedBlockingDeque<>();
    /** 可用性测试动作队列。 */
    private BlockingQueue<TestAvailabilityAction> testAvailabilityActions = new LinkedBlockingDeque<>();

    /** 共享 OIDC 客户端测试数据。 */
    private final OIDCClientData oidcClientData = new OIDCClientData();
    /** 认证通道请求映射。 */
    private ConcurrentMap<String, TestAuthenticationChannelRequest> authenticationChannelRequests = new ConcurrentHashMap<>();
    /** CIBA 客户端通知映射。 */
    private ConcurrentMap<String, ClientNotificationEndpointRequest> cibaClientNotifications = new ConcurrentHashMap<>();
    /** Intent 客户端绑定映射。 */
    private ConcurrentMap<String, String> intentClientBindings = new ConcurrentHashMap<>();

    /** {@inheritDoc} 创建带共享队列的 {@link TestApplicationResourceProvider}。 */
    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new TestApplicationResourceProvider(session, adminLogoutActions,
                backChannelLogoutTokens, frontChannelLogoutTokens, pushNotBeforeActions, testAvailabilityActions, oidcClientData, authenticationChannelRequests, cibaClientNotifications, intentClientBindings);
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    /** {@inheritDoc} 提供者 ID 为 {@code app}。 */
    @Override
    public String getId() {
        return "app";
    }


    /** OIDC 客户端测试用共享数据容器。 */
    public static class OIDCClientData {

        /** 签名密钥列表。 */
        private List<OIDCKeyData> keys = new ArrayList<>();

        /** OIDC 请求字符串（如 PAR/JAR 测试）。 */
        private String oidcRequest;
        /** Sector identifier 重定向 URI 列表。 */
        private List<String> sectorIdentifierRedirectUris;

        public List<OIDCKeyData> getKeys() {
            return keys;
        }

        /** 返回列表中第一个密钥，无则 null。 */
        public OIDCKeyData getFirstKey() {
            return keys.isEmpty() ? null : keys.get(0);
        }

        /**
         * 添加密钥到列表头部。
         *
         * @param key 密钥数据
         * @param keepExistingKeys 为 false 时清空现有密钥
         */
        public void addKey(OIDCKeyData key, boolean keepExistingKeys) {
            if (!keepExistingKeys) {
                this.keys = new ArrayList<>();
            }
            this.keys.add(0, key);
        }

        public String getOidcRequest() {
            return oidcRequest;
        }

        public void setOidcRequest(String oidcRequest) {
            this.oidcRequest = oidcRequest;
        }

        public List<String> getSectorIdentifierRedirectUris() {
            return sectorIdentifierRedirectUris;
        }

        public void setSectorIdentifierRedirectUris(List<String> sectorIdentifierRedirectUris) {
            this.sectorIdentifierRedirectUris = sectorIdentifierRedirectUris;
        }

    }

    /** 单个 OIDC 签名密钥的配置数据。 */
    public static class OIDCKeyData {

        /** 密钥对。 */
        private KeyPair keyPair;

        /** 密钥类型，默认 RSA。 */
        private String keyType = KeyType.RSA;
        /** 密钥算法。 */
        private String keyAlgorithm;
        /** 密钥用途，默认签名。 */
        private KeyUse keyUse = KeyUse.SIG;
        /** 椭圆曲线名称（EC 密钥）。 */
        private String curve;

        // 若未提供 kid，将基于密钥哈希随机生成
        /** 密钥 ID；未设置时由系统生成。 */
        private String kid;

        public KeyPair getSigningKeyPair() {
            return keyPair;
        }

        public void setSigningKeyPair(KeyPair signingKeyPair) {
            this.keyPair = signingKeyPair;
        }

        public String getSigningKeyType() {
            return keyType;
        }

        public void setSigningKeyType(String signingKeyType) {
            this.keyType = signingKeyType;
        }

        public String getSigningKeyAlgorithm() {
            return keyAlgorithm;
        }

        public void setSigningKeyAlgorithm(String signingKeyAlgorithm) {
            this.keyAlgorithm = signingKeyAlgorithm;
        }

        public KeyPair getKeyPair() {
            return keyPair;
        }

        public void setKeyPair(KeyPair keyPair) {
            this.keyPair = keyPair;
        }

        public String getKeyType() {
            return keyType;
        }

        public void setKeyType(String keyType) {
            this.keyType = keyType;
        }

        public String getKeyAlgorithm() {
            return keyAlgorithm;
        }

        public void setKeyAlgorithm(String keyAlgorithm) {
            this.keyAlgorithm = keyAlgorithm;
        }

        public KeyUse getKeyUse() {
            return keyUse;
        }

        public void setKeyUse(KeyUse keyUse) {
            this.keyUse = keyUse;
        }

        public String getKid() {
            return kid;
        }

        public void setKid(String kid) {
            this.kid = kid;
        }

        public String getCurve() {
            return curve;
        }

        public void setCurve(String curve) {
            this.curve = curve;
        }
    }
}
