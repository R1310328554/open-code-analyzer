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

package org.keycloak.protocol.oidc.endpoints.request;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.core.Response;

import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.constants.AdapterConstants;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.OIDCProviderConfig;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.utils.StringUtil;

import org.jboss.logging.Logger;

/**
 * 授权端点请求解析器抽象基类：解析 OIDC/OAuth2 授权请求参数并提取附加参数。
 * <p>默认最多接受 {@value org.keycloak.protocol.oidc.OIDCProviderConfig#DEFAULT_ADDITIONAL_REQ_PARAMS_MAX_NUMBER} 个附加参数，每个参数大小不超过 {@value org.keycloak.protocol.oidc.OIDCProviderConfig#DEFAULT_ADDITIONAL_REQ_PARAMS_MAX_SIZE}；超出限制时默认静默忽略。</p>
 * <p>可通过 {@code additionalReqParamsFailFast} 启用快速失败：违规参数将返回错误响应，例如 PAR 返回 JSON，openid/auth 返回带「返回应用」按钮的错误页（重定向至客户端 base URL）。</p>
 * <p>还可配置 {@code additionalReqParamMaxOverallSize} 限制所有附加参数的总大小；未配置时使用 {@link Integer#MAX_VALUE}。</p>
 *
 * @author <a href="mailto:manuel.schallar@prime-sign.com">Manuel Schallar</a>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AuthzEndpointRequestParser {

    private static final Logger logger = Logger.getLogger(AuthzEndpointRequestParser.class);

    /** OIDC 提供者配置 */
    protected final OIDCProviderConfig config;
    /** 允许的附加请求参数最大数量 */
    protected final int additionalReqParamsMaxNumber;
    /** 单个附加参数允许的最大长度 */
    protected final int additionalReqParamsMaxSize;
    /** 附加参数违规时是否快速失败 */
    protected final boolean additionalReqParamsFailFast;
    /** 所有附加参数合计允许的最大总长度 */
    protected final int additionalReqParamsMaxOverallSize;

    /** 会话属性键：已解析的请求对象 */
    public static final String AUTHZ_REQUEST_OBJECT = "ParsedRequestObject";
    /** 会话属性键：加密的请求对象 */
    public static final String AUTHZ_REQUEST_OBJECT_ENCRYPTED = "EncryptedRequestObject";

    /** 已知协议 GET 参数集合，不会写入 additionalReqParams */
    public static final Set<String> KNOWN_REQ_PARAMS = new HashSet<>();
    static {
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.CLIENT_ID_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.RESPONSE_TYPE_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.RESPONSE_MODE_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.REDIRECT_URI_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.STATE_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.SCOPE_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.RESOURCE_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.LOGIN_HINT_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.PROMPT_PARAM);
        KNOWN_REQ_PARAMS.add(AdapterConstants.KC_IDP_HINT);
        KNOWN_REQ_PARAMS.add(Constants.KC_ACTION);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.NONCE_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.MAX_AGE_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.UI_LOCALES_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.REQUEST_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.REQUEST_URI_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.CLAIMS_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.ACR_PARAM);

        // https://tools.ietf.org/html/rfc7636#section-6.1
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.CODE_CHALLENGE_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.CODE_CHALLENGE_METHOD_PARAM);

        // https://datatracker.ietf.org/doc/html/rfc9449#section-12.3
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.DPOP_JKT);

        // Those are not OAuth/OIDC parameters, but they should never be added to the additionalRequestParameters
        KNOWN_REQ_PARAMS.add(OAuth2Constants.CLIENT_ASSERTION_TYPE);
        KNOWN_REQ_PARAMS.add(OAuth2Constants.CLIENT_ASSERTION);
        KNOWN_REQ_PARAMS.add(OAuth2Constants.CLIENT_SECRET);
    }

    /**
     * 从 {@link OIDCLoginProtocol} 配置初始化解析器限制。
     * @param keycloakSession Keycloak 会话
     */
    protected AuthzEndpointRequestParser(KeycloakSession keycloakSession) {
        OIDCLoginProtocol loginProtocol = (OIDCLoginProtocol) keycloakSession.getProvider(LoginProtocol.class, OIDCLoginProtocol.LOGIN_PROTOCOL);
        this.config = loginProtocol.getConfig();
        this.additionalReqParamsMaxNumber = config.getAdditionalReqParamsMaxNumber();
        this.additionalReqParamsMaxSize = config.getAdditionalReqParamsMaxSize();
        this.additionalReqParamsFailFast = config.isAdditionalReqParamsFailFast(false);
        this.additionalReqParamsMaxOverallSize = config.getAdditionalReqParamsMaxOverallSize();
    }

    /**
     * 解析授权请求并填充 {@link AuthorizationEndpointRequest}。
     * @param request 待填充的授权端点请求对象
     */
    public void parseRequest(AuthorizationEndpointRequest request) {
        String clientId = getAndValidateParameter(OIDCLoginProtocol.CLIENT_ID_PARAM);
        if (clientId != null && request.clientId != null && !request.clientId.equals(clientId)) {
            throw new IllegalArgumentException("The client_id parameter doesn't match the one from OIDC 'request' or 'request_uri'");
        }
        if (clientId != null) {
            request.clientId = clientId;
        }

        String responseType = getAndValidateParameter(OIDCLoginProtocol.RESPONSE_TYPE_PARAM);
        validateResponseTypeParameter(responseType, request);
        if (responseType != null) {
            request.responseType = responseType;
        }

        request.responseMode = replaceIfNotNull(request.responseMode, getAndValidateParameter(OIDCLoginProtocol.RESPONSE_MODE_PARAM));
        request.redirectUri = replaceIfNotNull(request.redirectUri, getAndValidateParameter(OIDCLoginProtocol.REDIRECT_URI_PARAM));
        request.state = replaceIfNotNull(request.state, getAndValidateParameter(OIDCLoginProtocol.STATE_PARAM));
        request.scope = replaceIfNotNull(request.scope, getAndValidateParameter(OIDCLoginProtocol.SCOPE_PARAM));
        request.resource = replaceIfNotNull(request.resource, getAndValidateParameter(OIDCLoginProtocol.RESOURCE_PARAM));
        request.loginHint = replaceIfNotNull(request.loginHint, getAndValidateParameter(OIDCLoginProtocol.LOGIN_HINT_PARAM));
        request.prompt = replaceIfNotNull(request.prompt, getAndValidateParameter(OIDCLoginProtocol.PROMPT_PARAM));
        request.idpHint = replaceIfNotNull(request.idpHint, getAndValidateParameter(AdapterConstants.KC_IDP_HINT));
        request.action = replaceIfNotNull(request.action, getAndValidateParameter(Constants.KC_ACTION));
        request.nonce = replaceIfNotNull(request.nonce, getAndValidateParameter(OIDCLoginProtocol.NONCE_PARAM));
        request.maxAge = replaceIfNotNull(request.maxAge, getIntParameter(OIDCLoginProtocol.MAX_AGE_PARAM));
        request.claims = replaceIfNotNull(request.claims, getAndValidateParameter(OIDCLoginProtocol.CLAIMS_PARAM));
        request.acr = replaceIfNotNull(request.acr, getAndValidateParameter(OIDCLoginProtocol.ACR_PARAM));
        request.display = replaceIfNotNull(request.display, getAndValidateParameter(OAuth2Constants.DISPLAY));
        request.uiLocales = replaceIfNotNull(request.uiLocales, getAndValidateParameter(OAuth2Constants.UI_LOCALES_PARAM));

        // https://tools.ietf.org/html/rfc7636#section-6.1
        request.codeChallenge = replaceIfNotNull(request.codeChallenge, getAndValidateParameter(OIDCLoginProtocol.CODE_CHALLENGE_PARAM));
        request.codeChallengeMethod = replaceIfNotNull(request.codeChallengeMethod, getAndValidateParameter(OIDCLoginProtocol.CODE_CHALLENGE_METHOD_PARAM));

        request.dpopJkt = replaceIfNotNull(request.dpopJkt, getAndValidateParameter(OIDCLoginProtocol.DPOP_JKT));

        extractAdditionalReqParams(request.additionalReqParams);
    }

    /**
     * 校验 response_type 是否与 request/request_uri 中一致。
     * @param responseTypeParameter 当前解析到的 response_type
     * @param request 授权端点请求
     */
    protected void validateResponseTypeParameter(String responseTypeParameter, AuthorizationEndpointRequest request) {
        if (responseTypeParameter != null && request.responseType != null && !request.responseType.equals(responseTypeParameter)) {
            logger.warnf("The response_type parameter doesn't match the one from OIDC 'request' or 'request_uri'");
            request.setInvalidRequestMessage("Parameter response_type does not match");
        }
    }

    /**
     * 从未知参数中提取附加 OIDC 请求参数，并应用数量/长度限制。
     * @param additionalReqParams 存放附加参数的映射
     */
    protected void extractAdditionalReqParams(Map<String, String> additionalReqParams) {
        int currentAdditionalReqParamMaxOverallSize = 0;
        for (String paramName : keySet()) {

          if (KNOWN_REQ_PARAMS.contains(paramName)) {
            logger.debugv("The additional OIDC param ''{0}'' is well known. Continue with the other additional parameters.", paramName);
            continue;
          }

          final String value = getParameter(paramName);

          if (value == null || value.trim().isEmpty()) {
            logger.debugv("The additional OIDC param ''{0}'' ignored because it's value is null or blank.", paramName);
            continue;
          }

          final String sanitizedValue = StringUtil.removeControlCharacters(value);

          // Compare with ">=", as the currently processed parameter will be added at the END of this method.
          if (additionalReqParams.size() >= additionalReqParamsMaxNumber) {

            if (additionalReqParamsFailFast) {
              logger.debugv("The maximum number of allowed parameters ({0}) is exceeded.", additionalReqParamsMaxNumber);
              throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, "The maximum number of allowed parameters (" + additionalReqParamsMaxNumber + ") is exceeded.", Response.Status.BAD_REQUEST);
            } else {
              logger.debugv("The maximum number of allowed parameters ({0}) is exceeded.", additionalReqParamsMaxNumber);
              break;
            }

          }

          if (sanitizedValue.length() + currentAdditionalReqParamMaxOverallSize > additionalReqParamsMaxOverallSize) {

            if (additionalReqParamsFailFast) {
              logger.debugv("The OIDC additional parameter '{0}''s size ({1}) exceeds the maximum allowed size of all parameters ({2}).", paramName, sanitizedValue.length(), additionalReqParamsMaxOverallSize);
              throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, "The OIDC additional parameter '" + paramName + "'s size (" + sanitizedValue.length() + ") exceeds the maximum allowed size of all parameters (" + additionalReqParamsMaxOverallSize + ").", Response.Status.BAD_REQUEST);
            } else {
              logger.debugv("The OIDC additional parameter '{0}''s size exceeds ({1}) the maximum allowed size of all parameters ({2}).", paramName, sanitizedValue.length(), additionalReqParamsMaxOverallSize);
              break;
            }

          }

          if (sanitizedValue.length() > additionalReqParamsMaxSize) {

            if (additionalReqParamsFailFast) {
              logger.debugv("The OIDC additional parameter '{0}''s size is longer ({1}) than allowed ({2}).", paramName, sanitizedValue.length(), additionalReqParamsMaxSize);
              throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, "The OIDC additional parameter '" + paramName + "'s size is longer (" + sanitizedValue.length() + ") than allowed (" + additionalReqParamsMaxSize + ").", Response.Status.BAD_REQUEST);
            } else {
              logger.debugv("The OIDC additional parameter '{0}''s size is longer ({1}) than allowed ({2}).", paramName, sanitizedValue.length(), additionalReqParamsMaxSize);
              break;
            }

          }

          logger.debugv("Adding OIDC additional parameter ''{0}'' as additional parameter.", paramName);
          currentAdditionalReqParamMaxOverallSize += sanitizedValue.length();
          additionalReqParams.put(paramName, sanitizedValue);
        }
    }

    /** 若新值非 null 则替换，否则保留原值 */
    protected <T> T replaceIfNotNull(T previousVal, T newVal) {
        return newVal==null ? previousVal : newVal;
    }

    /**
     * 获取并校验参数长度（去除控制字符）。
     * @param paramName 参数名
     * @return 参数值，超长时按 failFast 策略处理
     */
    protected String getAndValidateParameter(String paramName) {
        String paramValue = getParameter(paramName);

        if (paramValue != null) {
            paramValue = StringUtil.removeControlCharacters(paramValue);
            int maxLength = config.getMaxLengthForTheParameter(paramName, false);
            if (paramValue.length() > maxLength) {
                logger.warnf("The size of OIDC parameter '%s' size is longer (%d) than allowed (%d). %s", paramName, paramValue.length(), maxLength, additionalReqParamsFailFast ? "Request not allowed." : "Ignoring the parameter.");
                if (additionalReqParamsFailFast) {
                    throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, "The size of OIDC parameter '" + paramName + "' is longer than allowed.", Response.Status.BAD_REQUEST);
                } else {
                    return null;
                }
            }
        }

        return paramValue;
    }

    /** @param paramName 参数名 @return 原始参数值 */
    protected abstract String getParameter(String paramName);

    /** @param paramName 参数名 @return 整型参数值 */
    protected abstract Integer getIntParameter(String paramName);

    /** @return 当前请求中所有参数名集合 */
    protected abstract Set<String> keySet();

}
