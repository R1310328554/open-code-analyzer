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

package org.keycloak.services.clientpolicy.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.OAuthErrorException;
import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.endpoints.request.AuthzEndpointRequestParser;
import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.services.Urls;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.AuthorizationRequestContext;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.logging.Logger;

import static org.keycloak.OAuthErrorException.INVALID_REQUEST_OBJECT;

/**
 * 安全请求对象（Request Object）执行器。
 * <p>在授权请求阶段校验 OIDC {@code request}/{@code request_uri} 对象：校验 exp/nbf/aud/scope 等声明、可用期、时钟偏差及查询参数与请求对象的一致性。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class SecureRequestObjectExecutor implements ClientPolicyExecutorProvider<SecureRequestObjectExecutor.Configuration> {

    private static final Logger logger = Logger.getLogger(SecureRequestObjectExecutor.class);

    /** 默认可用期（秒），源自 FAPI 1.0 Advanced 要求 */
    public static final Integer DEFAULT_AVAILABLE_PERIOD = 3600; // (sec) from FAPI 1.0 Advanced requirement
    /** 默认允许时钟偏差（秒），源自 FAPI 2.0 要求 */
    public static final Integer DEFAULT_ALLOWED_CLOCK_SKEW = 15; // (sec) from FAPI 2.0 requirement

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 执行器运行时配置 */
    private Configuration configuration;

    /** @param session Keycloak 会话 */
    public SecureRequestObjectExecutor(KeycloakSession session) {
        this.session = session;
    }

    /** 初始化配置并为空值填充 FAPI 默认值 */
    @Override
    public void setupConfiguration(SecureRequestObjectExecutor.Configuration config) {
        if (config == null) {
            configuration = new Configuration();
            configuration.setVerifyNbf(Boolean.TRUE);
            configuration.setAvailablePeriod(DEFAULT_AVAILABLE_PERIOD);
            configuration.setEncryptionRequired(Boolean.FALSE);
            configuration.setAllowedClockSkew(DEFAULT_ALLOWED_CLOCK_SKEW);
        } else {
            configuration = config;
            if (config.isVerifyNbf() == null) {
                configuration.setVerifyNbf(Boolean.TRUE);
            }
            if (config.getAvailablePeriod() == null) {
                configuration.setAvailablePeriod(DEFAULT_AVAILABLE_PERIOD);
            }
            if (config.isEncryptionRequired() == null) {
                configuration.setEncryptionRequired(Boolean.FALSE);
            }
            if (config.getAllowedClockSkew() == null) {
                configuration.setAllowedClockSkew(DEFAULT_ALLOWED_CLOCK_SKEW);
            }
        }
    }

    @Override
    public Class<Configuration> getExecutorConfigurationClass() {
        return Configuration.class;
    }

    /** 请求对象校验配置项 */
    public static class Configuration extends ClientPolicyExecutorConfigurationRepresentation {
        @JsonProperty(SecureRequestObjectExecutorFactory.AVAILABLE_PERIOD)
        protected Integer availablePeriod;
        /** 是否校验 nbf（Not Before）声明 */
        @JsonProperty(SecureRequestObjectExecutorFactory.VERIFY_NBF)
        protected Boolean verifyNbf;
        /** 是否要求请求对象加密 */
        @JsonProperty(SecureRequestObjectExecutorFactory.ENCRYPTION_REQUIRED)
        private Boolean encryptionRequired;
        @JsonProperty(SecureRequestObjectExecutorFactory.ALLOWED_CLOCK_SKEW)
        protected Integer allowedClockSkew;

        public Integer getAvailablePeriod() {
            return availablePeriod;
        }

        public void setAvailablePeriod(Integer availablePeriod) {
            this.availablePeriod = availablePeriod;
        }

        public Boolean isVerifyNbf() {
            return verifyNbf;
        }

        public void setVerifyNbf(Boolean verifyNbf) {
            this.verifyNbf = verifyNbf;
        }

        public void setEncryptionRequired(Boolean encryptionRequired) {
            this.encryptionRequired = encryptionRequired;
        }

        public Boolean isEncryptionRequired() {
            return encryptionRequired;
        }
    
        public Integer getAllowedClockSkew() {
            return allowedClockSkew;
        }

        public void setAllowedClockSkew(Integer allowedClockSkew) {
            this.allowedClockSkew = allowedClockSkew;
        }
    }

    /** @return 执行器 Provider 标识符 */
    @Override
    public String getProviderId() {
        return SecureRequestObjectExecutorFactory.PROVIDER_ID;
    }

    /** 按客户端策略事件触发校验逻辑 */
    @Override
    public void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
        switch (context.getEvent()) {
            case AUTHORIZATION_REQUEST:
                AuthorizationRequestContext authorizationRequestContext = (AuthorizationRequestContext)context;
                executeOnAuthorizationRequest(authorizationRequestContext);
                break;
            default:
                return;
        }
    }

    /** 在授权端点校验 request 对象内容与参数一致性 */
    private void executeOnAuthorizationRequest(AuthorizationRequestContext context) throws ClientPolicyException {
        logger.trace("Authz Endpoint - authz request");

        MultivaluedMap<String, String> params = context.getRequestParameters();

        if (params == null) {
            logger.trace("request parameter not exist.");
            throwClientPolicyException(OAuthErrorException.INVALID_REQUEST, "Missing parameters", context);
        }

        String requestParam = params.getFirst(OIDCLoginProtocol.REQUEST_PARAM);
        String requestUriParam = params.getFirst(OIDCLoginProtocol.REQUEST_URI_PARAM);

        // 校验 request 或 request_uri 参数至少存在一个
        if (requestParam == null && requestUriParam == null) {
            logger.trace("request object not exist.");
            throwClientPolicyException(OAuthErrorException.INVALID_REQUEST, "Missing parameter: 'request' or 'request_uri'",
                    context);
        }

        JsonNode requestObject = (JsonNode)session.getAttribute(AuthzEndpointRequestParser.AUTHZ_REQUEST_OBJECT);

        // check whether request object exists
        if (requestObject == null || requestObject.isEmpty()) {
            logger.trace("request object not exist.");
            throwClientPolicyException(OAuthErrorException.INVALID_REQUEST, "Invalid parameter: : 'request' or 'request_uri'",
                    context);
        }

        // 校验 scope 存在于查询参数或 request 对象中
        if (params.getFirst(OIDCLoginProtocol.SCOPE_PARAM) == null && requestObject.get(OIDCLoginProtocol.SCOPE_PARAM) == null) {
            logger.trace("scope object not exist.");
            throwClientPolicyException(OAuthErrorException.INVALID_REQUEST, "Parameter 'scope' missing in the request parameters or in 'request' object",
                    context);
        }

        // 校验 exp 声明存在
        if (requestObject.get("exp") == null) {
            logger.trace("exp claim not incuded.");
            throwClientPolicyException(INVALID_REQUEST_OBJECT, "Missing parameter in the 'request' object: exp", context);
        }

        // 校验 request 对象未过期
        long exp = requestObject.get("exp").asLong();
        if (Time.currentTime() > exp) { // TODO: Time.currentTime() is int while exp is long...
            logger.trace("request object expired.");
            throw new ClientPolicyException(INVALID_REQUEST_OBJECT, "Request Expired");
        }

        // FAPI-RW ID2 无需 nbf；FAPI 1.0 Advanced 与 FAPI 2.0 需要
        // while needed for FAPI 1.0 Advanced security profile
        // "nbf" check with clock skew is needed for FAPI 2.0
        if (Optional.ofNullable(configuration.isVerifyNbf()).orElse(Boolean.FALSE).booleanValue()) {
            // check whether "nbf" claim exists
            if (requestObject.get("nbf") == null) {
                logger.trace("nbf claim not incuded.");
                throwClientPolicyException(INVALID_REQUEST_OBJECT, "Missing parameter in the 'request' object: nbf", context);
            }

            // check whether request object not yet being processed
            long nbf = requestObject.get("nbf").asLong();
            if (Time.currentTime() < nbf - configuration.getAllowedClockSkew().intValue()) { // TODO: Time.currentTime() is int while nbf is long...
                logger.trace("request object not yet being processed.");
                throwClientPolicyException(INVALID_REQUEST_OBJECT, "Request not yet being processed", context);
            }

            // check whether request object's available period is short
            int availablePeriod = Optional.ofNullable(configuration.getAvailablePeriod()).orElse(DEFAULT_AVAILABLE_PERIOD).intValue();
            if (exp - nbf > availablePeriod) {
                logger.trace("request object's available period is long.");
                throwClientPolicyException(INVALID_REQUEST_OBJECT, "Request's available period is long", context);
            }
        }

        // 结合时钟偏差校验 iat
        if (requestObject.get("iat") != null) {
            long iat = requestObject.get("iat").asLong();
            if (Time.currentTime() < iat - configuration.getAllowedClockSkew().intValue()) { // TODO: Time.currentTime() is int while nbf is long...
                logger.trace("request object issed in the future.");
                throwClientPolicyException(INVALID_REQUEST_OBJECT, "Request issued in the future", context);
            }
        }

        // check whether "aud" claim exists
        List<String> aud = new ArrayList<String>();
        JsonNode audience = requestObject.get("aud");
        if (audience == null) {
            logger.trace("aud claim not incuded.");
            throwClientPolicyException(INVALID_REQUEST_OBJECT, "Missing parameter in the 'request' object: aud", context);
        }
        if (audience.isArray()) {
            for (JsonNode node : audience) aud.add(node.asText());
        } else {
            aud.add(audience.asText());
        }
        if (aud.isEmpty()) {
            logger.trace("aud claim not incuded.");
            throwClientPolicyException(INVALID_REQUEST_OBJECT, "Missing parameter value in the 'request' object: aud", context);
        }

        // 校验 aud 指向当前 realm 授权服务器
        String iss = Urls.realmIssuer(session.getContext().getUri().getBaseUri(), session.getContext().getRealm().getName());
        if (!aud.contains(iss)) {
            logger.trace("aud not points to the intended realm.");
            throwClientPolicyException(INVALID_REQUEST_OBJECT, "Invalid parameter in the 'request' object: aud", context);
        }

        // 确认查询参数均包含于 request 对象且值一致
        // argument "request" are parameters overridden by parameters in request object
        Optional<String> incorrectParam = AuthzEndpointRequestParser.KNOWN_REQ_PARAMS.stream()
                .filter(param -> params.containsKey(param))
                .filter(param -> !isSameParameterIncluded(param, params.getFirst(param), requestObject))
                .findFirst();
        if (incorrectParam.isPresent()) {
            logger.warnf("Parameter '%s' does not have same value in 'request' object and in request parameters", incorrectParam.get());
            throwClientPolicyException(OAuthErrorException.INVALID_REQUEST, "Invalid parameter. Parameters in 'request' object not matching with request parameters",
                    context);
        }

        Boolean encryptionRequired = Optional.ofNullable(configuration.isEncryptionRequired()).orElse(Boolean.FALSE);
        if (encryptionRequired && session.getAttribute(AuthzEndpointRequestParser.AUTHZ_REQUEST_OBJECT_ENCRYPTED) == null) {
            logger.trace("request object's not encrypted.");
            throw new ClientPolicyException(INVALID_REQUEST_OBJECT, "Request object not encrypted");
        }

        logger.trace("Passed.");
    }

    /** 判断查询参数值是否与 request 对象中对应字段一致 */
    private boolean isSameParameterIncluded(String param, String value, JsonNode requestObject) {
        if (param.equals(OIDCLoginProtocol.REQUEST_PARAM) || param.equals(OIDCLoginProtocol.REQUEST_URI_PARAM)) return true;
        if (requestObject.hasNonNull(param)) return requestObject.get(param).asText().equals(value);
        return false;
    }

    /** PAR 场景下将 invalid_request_object 映射为 invalid_request_uri */
    private void throwClientPolicyException(String error, String message,
            AuthorizationRequestContext context) throws ClientPolicyException {
        if (context.isParRequest() && INVALID_REQUEST_OBJECT.equals(error)) {
            throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST_URI, message);
        }

        throw new ClientPolicyException(error, message);
    }
}
