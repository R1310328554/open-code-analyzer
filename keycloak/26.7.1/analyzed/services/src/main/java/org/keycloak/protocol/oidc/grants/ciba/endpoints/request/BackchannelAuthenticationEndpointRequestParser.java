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
 *
 */

package org.keycloak.protocol.oidc.grants.ciba.endpoints.request;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.keycloak.OAuth2Constants;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.grants.ciba.CibaGrantType;

import org.jboss.logging.Logger;

/**
 * 后台认证端点请求解析器抽象基类。
 * <p>定义已知参数集合，并将 scope、用户提示等标准参数映射到请求对象；额外参数受数量与长度限制以防 DoS。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public abstract class BackchannelAuthenticationEndpointRequestParser {

    private static final Logger logger = Logger.getLogger(BackchannelAuthenticationEndpointRequestParser.class);

    /** 附加请求参数最大数量（防 DoS） */
    public static final int ADDITIONAL_REQ_PARAMS_MAX_MUMBER = 5;

    /** 附加请求参数值最大长度（超长忽略，防 DoS） */
    public static final int ADDITIONAL_REQ_PARAMS_MAX_SIZE = 200;

    /** 会话属性键：已解析的签名认证 request 对象 */
    public static final String CIBA_SIGNED_AUTHENTICATION_REQUEST = "ParsedSignedAuthenticationRequest";

    /** 已知协议 POST 参数集合（不写入 additionalReqParams） */
    public static final Set<String> KNOWN_REQ_PARAMS = new HashSet<>();
    static {
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.REQUEST_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.REQUEST_URI_PARAM);

        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.SCOPE_PARAM);

        // CIBA 专用参数
        KNOWN_REQ_PARAMS.add(CibaGrantType.CLIENT_NOTIFICATION_TOKEN);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.ACR_PARAM);
        KNOWN_REQ_PARAMS.add(CibaGrantType.LOGIN_HINT_TOKEN);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.ID_TOKEN_HINT);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.LOGIN_HINT_PARAM);
        KNOWN_REQ_PARAMS.add(CibaGrantType.BINDING_MESSAGE);
        KNOWN_REQ_PARAMS.add(CibaGrantType.USER_CODE);
        KNOWN_REQ_PARAMS.add(CibaGrantType.REQUESTED_EXPIRY);

        // OIDC 标准参数
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.PROMPT_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.NONCE_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.MAX_AGE_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.UI_LOCALES_PARAM);
        KNOWN_REQ_PARAMS.add(OIDCLoginProtocol.CLAIMS_PARAM);

        // 以下参数不出现在认证通道请求中
        // 但在 client_secret_post 认证方式的请求体中可能出现
        KNOWN_REQ_PARAMS.add(OAuth2Constants.CLIENT_ID);
        KNOWN_REQ_PARAMS.add(OAuth2Constants.CLIENT_SECRET);
    }

    /**
     * 解析请求参数并填充 {@link BackchannelAuthenticationEndpointRequest}。
     * @param request 待填充的请求对象
     */
        request.scope = replaceIfNotNull(request.scope, getParameter(OIDCLoginProtocol.SCOPE_PARAM));

        request.clientNotificationToken = replaceIfNotNull(request.clientNotificationToken, getParameter(CibaGrantType.CLIENT_NOTIFICATION_TOKEN));
        request.acr = replaceIfNotNull(request.acr, getParameter(OIDCLoginProtocol.ACR_PARAM));
        request.loginHintToken = replaceIfNotNull(request.loginHintToken, getParameter(CibaGrantType.LOGIN_HINT_TOKEN));
        request.idTokenHint = replaceIfNotNull(request.idTokenHint, getParameter(OIDCLoginProtocol.ID_TOKEN_HINT));
        request.loginHint = replaceIfNotNull(request.loginHint, getParameter(OIDCLoginProtocol.LOGIN_HINT_PARAM));
        request.bindingMessage = replaceIfNotNull(request.bindingMessage, getParameter(CibaGrantType.BINDING_MESSAGE));
        request.userCode = replaceIfNotNull(request.userCode, getParameter(CibaGrantType.USER_CODE));
        request.requestedExpiry = replaceIfNotNull(request.requestedExpiry, getIntParameter(CibaGrantType.REQUESTED_EXPIRY));

        request.prompt = replaceIfNotNull(request.prompt, getParameter(OIDCLoginProtocol.PROMPT_PARAM));
        request.nonce = replaceIfNotNull(request.nonce, getParameter(OIDCLoginProtocol.NONCE_PARAM));
        request.maxAge = replaceIfNotNull(request.maxAge, getIntParameter(OIDCLoginProtocol.MAX_AGE_PARAM));
        request.uiLocales = replaceIfNotNull(request.uiLocales, getParameter(OIDCLoginProtocol.UI_LOCALES_PARAM));
        request.claims = replaceIfNotNull(request.claims, getParameter(OIDCLoginProtocol.CLAIMS_PARAM));

        extractAdditionalReqParams(request.additionalReqParams);
    }

    /**
     * 提取未知参数到附加参数映射（受数量与长度限制）。
     * @param additionalReqParams 附加参数目标映射
     */
        for (String paramName : keySet()) {
            if (!KNOWN_REQ_PARAMS.contains(paramName)) {
                String value = getParameter(paramName);
                if (value != null && value.trim().isEmpty()) {
                    value = null;
                }
                if (value != null && value.length() <= ADDITIONAL_REQ_PARAMS_MAX_SIZE) {
                    if (additionalReqParams.size() >= ADDITIONAL_REQ_PARAMS_MAX_MUMBER) {
                        logger.debug("Maximal number of additional OIDC CIBA params (" + ADDITIONAL_REQ_PARAMS_MAX_MUMBER + ") exceeded, ignoring rest of them!");
                        break;
                    }
                    additionalReqParams.put(paramName, value);
                } else {
                    logger.debug("OIDC CIBA Additional param " + paramName + " ignored because value is empty or longer than " + ADDITIONAL_REQ_PARAMS_MAX_SIZE);
                }
            }

        }
    }

    /** 若新值非 null 则替换，否则保留原值 */
    protected <T> T replaceIfNotNull(T previousVal, T newVal) {
        return newVal==null ? previousVal : newVal;
    }

    protected abstract String getParameter(String paramName);

    protected abstract Integer getIntParameter(String paramName);

    protected abstract Set<String> keySet();

}
