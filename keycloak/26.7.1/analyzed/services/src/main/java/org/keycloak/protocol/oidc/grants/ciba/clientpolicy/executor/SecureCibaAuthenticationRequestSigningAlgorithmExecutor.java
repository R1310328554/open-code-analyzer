/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oidc.grants.ciba.clientpolicy.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.keycloak.OAuthErrorException;
import org.keycloak.crypto.Algorithm;
import org.keycloak.models.CibaConfig;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.AdminClientRegisterContext;
import org.keycloak.services.clientpolicy.context.AdminClientUpdateContext;
import org.keycloak.services.clientpolicy.context.DynamicClientRegisterContext;
import org.keycloak.services.clientpolicy.context.DynamicClientUpdateContext;
import org.keycloak.services.clientpolicy.executor.ClientPolicyExecutorProvider;
import org.keycloak.services.clientpolicy.executor.FapiConstant;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.logging.Logger;

/**
 * CIBA 客户端策略 Executor：强制后台认证请求使用 FAPI 认可的安全签名算法。
 * <p>在客户端注册/更新时校验或填充 {@code ciba.backchannel.auth.request.signing.alg} 属性。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class SecureCibaAuthenticationRequestSigningAlgorithmExecutor implements ClientPolicyExecutorProvider<SecureCibaAuthenticationRequestSigningAlgorithmExecutor.Configuration> {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(SecureCibaAuthenticationRequestSigningAlgorithmExecutor.class);

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** Executor 运行时配置 */
    private Configuration configuration;

    /** 待校验的客户端属性键：后台认证请求签名算法 */
    private static final String sigTarget = CibaConfig.CIBA_BACKCHANNEL_AUTH_REQUEST_SIGNING_ALG;

    /** 未指定或不安全时的默认算法 */
    private static final String DEFAULT_ALGORITHM_VALUE = Algorithm.PS256;

    /** @param session Keycloak 会话 */
    public SecureCibaAuthenticationRequestSigningAlgorithmExecutor(KeycloakSession session) {
        this.session = session;
    }

    /** @return Executor Provider ID */
    @Override
    public String getProviderId() {
        return SecureCibaAuthenticationRequestSigningAlgorithmExecutorFactory.PROVIDER_ID;
    }

    /** 加载配置并在缺省或不安全时回退至 PS256 */
    @Override
    public void setupConfiguration(SecureCibaAuthenticationRequestSigningAlgorithmExecutor.Configuration config) {
        this.configuration = Optional.ofNullable(config).orElse(createDefaultConfiguration());
        if (config.getDefaultAlgorithm() == null || !isSecureAlgorithm(config.getDefaultAlgorithm())) config.setDefaultAlgorithm(DEFAULT_ALGORITHM_VALUE);
    }

    /** @return 配置类类型 */
    @Override
    public Class<Configuration> getExecutorConfigurationClass() {
        return Configuration.class;
    }

    /** Executor 配置：默认签名算法 */
    public static class Configuration extends ClientPolicyExecutorConfigurationRepresentation {
        /** 客户端未指定算法时使用的默认签名算法 */
        @JsonProperty("default-algorithm")
        protected String defaultAlgorithm;

        /** @return 默认算法 */
        public String getDefaultAlgorithm() {
            return defaultAlgorithm;
        }

        /** 设置默认算法（非 FAPI 安全算法时回退 PS256） */
        public void setDefaultAlgorithm(String defaultAlgorithm) {
            if (isSecureAlgorithm(defaultAlgorithm)) {
                this.defaultAlgorithm = defaultAlgorithm;
            } else {
                logger.tracev("defaultAlgorithm = {0}, fall back to {1}.", defaultAlgorithm, DEFAULT_ALGORITHM_VALUE);
                this.defaultAlgorithm = DEFAULT_ALGORITHM_VALUE;
            }
        }
    }

    /** 在客户端注册/更新事件中校验并强制安全签名算法 */
    @Override
    public void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
        switch (context.getEvent()) {
        case REGISTER:
            if (context instanceof AdminClientRegisterContext) {
                verifyAndEnforceSecureSigningAlgorithm(((AdminClientRegisterContext)context).getProposedClientRepresentation());
            } else if (context instanceof DynamicClientRegisterContext) {
                verifyAndEnforceSecureSigningAlgorithm(((DynamicClientRegisterContext)context).getProposedClientRepresentation());
            } else {
                throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "not allowed input format.");
            }
            break;
        case UPDATE:
            if (context instanceof AdminClientUpdateContext) {
                verifyAndEnforceSecureSigningAlgorithm(((AdminClientUpdateContext)context).getProposedClientRepresentation());
            } else if (context instanceof DynamicClientUpdateContext) {
                verifyAndEnforceSecureSigningAlgorithm(((DynamicClientUpdateContext)context).getProposedClientRepresentation());
            } else {
                throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "not allowed input format.");
            }
            break;
        default:
            return;
        }
    }

    /** 构造默认 PS256 配置 */
    private Configuration createDefaultConfiguration() {
        Configuration conf = new Configuration();
        conf.setDefaultAlgorithm(DEFAULT_ALGORITHM_VALUE);
        return conf;
    }

    /** 校验客户端 CIBA 签名算法属性，缺失时写入默认值，不安全则拒绝 */
    private void verifyAndEnforceSecureSigningAlgorithm(ClientRepresentation clientRep) throws ClientPolicyException {
        Map<String, String> attributes = Optional.ofNullable(clientRep.getAttributes()).orElse(new HashMap<>());
        String sigAlg = attributes.get(sigTarget);
        if (sigAlg == null) {
            logger.tracev("Signing algorithm not specified explicitly, signature target = {0}. set default algorithm = {1}.", sigTarget, configuration.getDefaultAlgorithm());
            attributes.put(sigTarget, configuration.getDefaultAlgorithm());
            clientRep.setAttributes(attributes);
            return;
        }

        if (isSecureAlgorithm(sigAlg)) {
            logger.tracev("Passed. signature target = {0}, signature algorithm = {1}", sigTarget, sigAlg);
            return;
        }

        logger.tracev("NOT allowed signatureAlgorithm. signature target = {0}, signature algorithm = {1}", sigTarget, sigAlg);
        throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "not allowed signature algorithm.");
    }

    /** 判断算法是否在 FAPI 允许列表中 */
    private static boolean isSecureAlgorithm(String sigAlg) {
        return FapiConstant.ALLOWED_ALGORITHMS.contains(sigAlg);
    }

}
