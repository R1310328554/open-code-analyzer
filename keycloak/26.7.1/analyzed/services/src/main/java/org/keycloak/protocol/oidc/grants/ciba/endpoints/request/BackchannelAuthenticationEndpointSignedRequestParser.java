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
import java.util.Set;

import org.keycloak.crypto.SignatureProvider;
import org.keycloak.jose.JOSE;
import org.keycloak.jose.JOSEParser;
import org.keycloak.jose.jwe.JWE;
import org.keycloak.jose.jws.Algorithm;
import org.keycloak.jose.jws.JWSHeader;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.models.CibaConfig;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 从 OIDC 签名 {@code request} 对象解析后台认证参数。
 * <p>校验 JWS 签名算法与客户端预注册算法一致，并将解析结果存入会话属性。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
class BackchannelAuthenticationEndpointSignedRequestParser extends BackchannelAuthenticationEndpointRequestParser {

    /** 已验证签名的 request JWT 载荷（JSON 节点） */
    private final JsonNode requestParams;

    /**
     * 解析并验证签名认证 request 对象。
     * @param session Keycloak 会话
     * @param signedAuthReq 签名 request 字符串（JWS）
     * @param client 客户端模型
     * @param config CIBA 策略配置
     * @throws Exception 签名验证或算法不匹配时抛出
     */
        JOSE jwt = JOSEParser.parse(signedAuthReq);

        if (jwt instanceof JWE) {
            throw new RuntimeException("Encrypted request object is not allowed");
        }

        JWSInput input = (JWSInput) jwt;
        JWSHeader header = input.getHeader();
        Algorithm headerAlgorithm = header.getAlgorithm();

        Algorithm requestedSignatureAlgorithm = config.getBackchannelAuthRequestSigningAlg(client);

        if (headerAlgorithm == null) {
            throw new RuntimeException("Signed algorithm not specified");
        }
        if (header.getAlgorithm() == Algorithm.none) {
            throw new RuntimeException("None signed algorithm is not allowed");
        }
        SignatureProvider signatureProvider = session.getProvider(SignatureProvider.class, headerAlgorithm.name());
        if (signatureProvider == null) {
            throw new RuntimeException("Not found provider for the algorithm " + headerAlgorithm.name());
        }
        if (!signatureProvider.isAsymmetricAlgorithm()) {
            throw new RuntimeException("Signed algorithm is not allowed");
        }
        if (requestedSignatureAlgorithm == null || requestedSignatureAlgorithm != headerAlgorithm) {
            throw new RuntimeException("Client requested algorithm not registered in advance or request signed with different algorithm other than client requested algorithm");
        }

        this.requestParams = session.tokens().decodeClientJWT(signedAuthReq, client, JsonNode.class);
        if (this.requestParams == null) {
            throw new RuntimeException("Failed to verify signature");
        }

        session.setAttribute(BackchannelAuthenticationEndpointRequestParser.CIBA_SIGNED_AUTHENTICATION_REQUEST, requestParams);
    }

    /** @param paramName JSON 字段名 @return 字段文本值 */
    @Override
    protected String getParameter(String paramName) {
        JsonNode val = this.requestParams.get(paramName);
        if (val == null) {
            return null;
        } else if (val.isValueNode()) {
            return val.asText();
        } else {
            return val.toString();
        }
    }

    /** @param paramName JSON 字段名 @return 整型字段值 */
    @Override
    protected Integer getIntParameter(String paramName) {
        Object val = this.requestParams.get(paramName);
        return val==null ? null : Integer.valueOf(getParameter(paramName));
    }

    /** @return request JSON 对象的所有字段名 */
    @Override
    protected Set<String> keySet() {
        HashSet<String> keys = new HashSet<>();
        requestParams.fieldNames().forEachRemaining(keys::add);
        return keys;
    }
}
