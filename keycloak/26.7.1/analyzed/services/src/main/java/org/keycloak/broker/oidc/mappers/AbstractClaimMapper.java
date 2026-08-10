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

package org.keycloak.broker.oidc.mappers;

import java.util.List;
import java.util.Map;

import org.keycloak.broker.oidc.KeycloakOIDCIdentityProvider;
import org.keycloak.broker.oidc.OIDCIdentityProvider;
import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.databind.JsonNode;

import static org.keycloak.utils.JsonUtils.splitClaimPath;

/**
 * OIDC Claim 映射器基类：从 Access Token、ID Token 或 UserInfo 提取 claim 值。
 * <p>支持点分路径嵌套 claim 及多种类型的值相等比较。</p>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class AbstractClaimMapper extends AbstractIdentityProviderMapper {
    /** 映射器配置键：claim 名称。 */
    public static final String CLAIM = "claim";
    /** 映射器配置键：期望 claim 值。 */
    public static final String CLAIM_VALUE = "claim.value";

    /** 从 {@link JsonWebToken} 按路径提取 claim（sub 走快捷路径）。 */
    public static Object getClaimValue(JsonWebToken token, String claim) {

        switch (claim) {
            case "sub":
                return token.getSubject();
            default:
                // sub 未匹配时回退到 otherClaims 路径解析
        }

        List<String> split = splitClaimPath(claim);
        Map<String, Object> jsonObject = token.getOtherClaims();
        final int length = split.size();
        int i = 0;
        for (String component : split) {
            i++;
            if (i == length) {
                return jsonObject.get(component);
            } else {
                Object val = jsonObject.get(component);
                if (!(val instanceof Map)) return null;
                jsonObject = (Map<String, Object>)val;
            }
        }
        return null;
    }

    public static Object getClaimValue(IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        String claim = mapperModel.getConfig().get(CLAIM);
        return getClaimValue(context, claim);
    }

    /** 依次搜索已校验 access token、ID token 与 UserInfo。 */
    public static Object getClaimValue(BrokeredIdentityContext context, String claim) {
        {  // 搜索已校验的 access token
            JsonWebToken token = (JsonWebToken)context.getContextData().get(KeycloakOIDCIdentityProvider.VALIDATED_ACCESS_TOKEN);
            if (token != null) {
                Object value = getClaimValue(token, claim);
                if (value != null) return value;
            }

        }
        {  // 搜索 ID Token
            Object rawIdToken = context.getContextData().get(OIDCIdentityProvider.VALIDATED_ID_TOKEN);
            JsonWebToken idToken = null;

            if (rawIdToken instanceof String) {
                try {
                    idToken = new JWSInput(rawIdToken.toString()).readJsonContent(JsonWebToken.class);
                } catch (JWSInputException e) {
                    return null;
                }
            } else if (rawIdToken instanceof JsonWebToken) {
                idToken = (JsonWebToken) rawIdToken;
            }

            if (idToken != null) {
                Object value = getClaimValue(idToken, claim);
                if (value != null)
                    return value;
            }
        }
        {
            // 搜索 OIDC UserInfo claim 集合（若存在）
            JsonNode profileJsonNode = (JsonNode) context.getContextData().get(OIDCIdentityProvider.USER_INFO);
            Object value = AbstractJsonUserAttributeMapper.getJsonValue(profileJsonNode, claim);
            if (value != null) return value;
        }
        return null;
    }


    /** @return claim 值是否与配置的 claim.value 相等 */
    protected boolean hasClaimValue(IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        Object value = getClaimValue(mapperModel, context);
        String desiredValue = mapperModel.getConfig().get(CLAIM_VALUE);
        return valueEquals(desiredValue, value);
    }

    /** 比较期望字符串与 claim 值（支持 String/Number/Boolean/List/JsonNode）。 */
    public boolean valueEquals(String desiredValue, Object value) {
        if (value instanceof String) {
            if (desiredValue.equals(value)) return true;
        } else if (value instanceof Double) {
            try {
                if (Double.valueOf(desiredValue).equals(value)) return true;
            } catch (Exception e) {

            }
        } else if (value instanceof Integer) {
            try {
                if (Integer.valueOf(desiredValue).equals(value)) return true;
            } catch (Exception e) {

            }
        } else if (value instanceof Boolean) {
            try {
                if (Boolean.valueOf(desiredValue).equals(value)) return true;
            } catch (Exception e) {

            }
        } else if (value instanceof List) {
            List list = (List)value;
            for (Object val : list) {
                if (valueEquals(desiredValue, val)) return true;
            }
        } else if (value instanceof JsonNode) {
            try {
                if (JsonSerialization.readValue(desiredValue, JsonNode.class).equals(value)) return true;
            } catch (Exception ignore) {
            }
        }
        return false;
    }
}
