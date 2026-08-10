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

package org.keycloak.protocol.oidc.mappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.IDToken;
import org.keycloak.services.ServicesLogger;
import org.keycloak.util.JsonSerialization;
import org.keycloak.utils.JsonUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jboss.logging.Logger;

import static org.keycloak.utils.JsonUtils.splitClaimPath;


/**
 * OIDC 属性映射器辅助工具：声明写入、类型转换、配置项构建及包含目标判断。
 * <p>被多数 OIDC 协议映射器复用以统一处理嵌套声明路径与 JSON 类型。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class OIDCAttributeMapperHelper {

    /** 配置键：目标令牌声明名（支持点分路径） */
    public static final String TOKEN_CLAIM_NAME = "claim.name";
    /** 控制台标签键：令牌声明名 */
    public static final String TOKEN_CLAIM_NAME_LABEL = "tokenClaimName.label";
    /** 帮助文本键：令牌声明名 */
    public static final String TOKEN_CLAIM_NAME_TOOLTIP = "tokenClaimName.tooltip";
    /** 配置键：声明 JSON 类型（String/long/int/boolean/JSON） */
    public static final String JSON_TYPE = "jsonType.label";
    public static final String JSON_TYPE_TOOLTIP = "jsonType.tooltip";
    /** 配置键：是否包含于 Access Token */
    public static final String INCLUDE_IN_ACCESS_TOKEN = "access.token.claim";
    public static final String INCLUDE_IN_ACCESS_TOKEN_LABEL = "includeInAccessToken.label";
    public static final String INCLUDE_IN_ACCESS_TOKEN_HELP_TEXT = "includeInAccessToken.tooltip";
    /** 配置键：是否包含于 ID Token */
    public static final String INCLUDE_IN_ID_TOKEN = "id.token.claim";
    public static final String INCLUDE_IN_ID_TOKEN_LABEL = "includeInIdToken.label";
    public static final String INCLUDE_IN_ID_TOKEN_HELP_TEXT = "includeInIdToken.tooltip";
    /** 配置键：是否包含于访问令牌响应 */
    public static final String INCLUDE_IN_ACCESS_TOKEN_RESPONSE = "access.tokenResponse.claim";
    public static final String INCLUDE_IN_ACCESS_TOKEN_RESPONSE_LABEL = "includeInAccessTokenResponse.label";
    public static final String INCLUDE_IN_ACCESS_TOKEN_RESPONSE_HELP_TEXT = "includeInAccessTokenResponse.tooltip";

    /** 配置键：是否包含于 UserInfo 响应 */
    public static final String INCLUDE_IN_USERINFO = "userinfo.token.claim";
    public static final String INCLUDE_IN_USERINFO_LABEL = "includeInUserInfo.label";
    public static final String INCLUDE_IN_USERINFO_HELP_TEXT = "includeInUserInfo.tooltip";

    /** 配置键：是否包含于令牌内省响应 */
    public static final String INCLUDE_IN_INTROSPECTION = "introspection.token.claim";
    public static final String INCLUDE_IN_INTROSPECTION_LABEL = "includeInIntrospection.label";
    public static final String INCLUDE_IN_INTROSPECTION_HELP_TEXT = "includeInIntrospection.tooltip";

    /** 配置键：是否包含于轻量 Access Token */
    public static final String INCLUDE_IN_LIGHTWEIGHT_ACCESS_TOKEN = "lightweight.claim";

    public static final String INCLUDE_IN_LIGHTWEIGHT_ACCESS_TOKEN_LABEL = "includeInLightweight.label";

    public static final String INCLUDE_IN_LIGHTWEIGHT_ACCESS_TOKEN_HELP_TEXT = "includeInLightweight.tooltip";

    private static final Logger logger = Logger.getLogger(OIDCAttributeMapperHelper.class);

    /**
     * 令牌属性 setter 函数式接口：将声明写入令牌对象的标准字段。
     * @param <T> 接受声明的令牌类型
     */
    private static interface PropertySetter<T> {
        void set(String claim, String mapperName, T token, Object value);
    }

    /** ID Token/Access Token 标准字段 setter 表（不写入 otherClaims） */

    private static final Map<String, PropertySetter<IDToken>> tokenPropertySetters;

    /** AccessTokenResponse 标准字段 setter 表（不写入 otherClaims） */

    private static final Map<String, PropertySetter<AccessTokenResponse>> responsePropertySetters;

    static {
        // 允许通过 setter 直接写入 IDToken/AccessToken 的声明
        Map<String, PropertySetter<IDToken>> tmpToken = new HashMap<>();
        tmpToken.put("sub", (claim, mapperName, token, value) -> {
            token.setSubject(value.toString());
        });
        tmpToken.put("azp", (claim, mapperName, token, value) -> {
            token.issuedFor(value.toString());
        });
        tmpToken.put(IDToken.ACR, (claim, mapperName, token, value) -> {
            token.setAcr(value.toString());
        });
        tmpToken.put(IDToken.AUTH_TIME, (claim, mapperName, token, value) -> {
            try {
                token.setAuth_time(Long.parseLong(value.toString()));
            } catch (NumberFormatException ignored){

            }
        });
        tmpToken.put("aud", (claim, mapperName, token, value) -> {
            if (value instanceof Collection) {
                String[] audiences = ((Collection<?>) value).stream().map(Object::toString).toArray(String[]::new);
                token.audience(audiences);
            } else {
                token.audience(value.toString());
            }
        });
        // 服务端已设置、禁止映射器修改的声明
        PropertySetter<IDToken> notAllowedInToken = (claim, mapperName, token, value) -> {
            logger.warnf("Claim '%s' is non-modifiable in IDToken. Ignoring the assignment for mapper '%s'.", claim, mapperName);
        };
        tmpToken.put("jti", notAllowedInToken);
        tmpToken.put("typ", notAllowedInToken);
        tmpToken.put("iat", notAllowedInToken);
        tmpToken.put("exp", notAllowedInToken);
        tmpToken.put("iss", notAllowedInToken);
        tmpToken.put("scope", notAllowedInToken);
        tmpToken.put(IDToken.NONCE, notAllowedInToken);
        tmpToken.put(IDToken.SESSION_STATE, notAllowedInToken);
        tokenPropertySetters = Collections.unmodifiableMap(tmpToken);

        // AccessTokenResponse 中禁止修改的服务端固定字段
        Map<String, PropertySetter<AccessTokenResponse>> tmpResponse = new HashMap<>();
        PropertySetter<AccessTokenResponse> notAllowedInResponse = (claim, mapperName, token, value) -> {
            logger.warnf("Claim '%s' is non-modifiable in AccessTokenResponse. Ignoring the assignment for mapper '%s'.", claim, mapperName);
        };
        tmpResponse.put("access_token", notAllowedInResponse);
        tmpResponse.put("token_type", notAllowedInResponse);
        tmpResponse.put("session_state", notAllowedInResponse);
        tmpResponse.put("expires_in", notAllowedInResponse);
        tmpResponse.put("id_token", notAllowedInResponse);
        tmpResponse.put("refresh_token", notAllowedInResponse);
        tmpResponse.put("refresh_expires_in", notAllowedInResponse);
        tmpResponse.put("not-before-policy", notAllowedInResponse);
        tmpResponse.put("scope", notAllowedInResponse);
        responsePropertySetters = Collections.unmodifiableMap(tmpResponse);
    }

    /**
     * 将原始属性值按映射器 JSON 类型与多值配置转换为令牌可用值。
     * @param mappingModel 映射器配置
     * @param attributeValue 原始属性值
     * @return 转换后的值，无法映射时返回 null
     */
        if (attributeValue == null) return null;

        if (attributeValue instanceof Collection) {
            Collection<?> valueAsList = (Collection<?>) attributeValue;
            if (valueAsList.isEmpty()) return null;

            if (isMultivalued(mappingModel)) {
                List<Object> result = new ArrayList<>();
                for (Object valueItem : valueAsList) {
                    result.add(mapAttributeValue(mappingModel, valueItem));
                }
                return result;
            } else {
                if (valueAsList.size() > 1) {
                    ServicesLogger.LOGGER.multipleValuesForMapper(attributeValue.toString(), mappingModel.getName());
                }

                attributeValue = valueAsList.iterator().next();
            }
        }

        String type = mappingModel.getConfig().get(JSON_TYPE);
        return convertToType(type, attributeValue);
    }

    private static <X, T> List<T> transform(List<X> attributeValue, Function<X, T> mapper) {
        return attributeValue.stream()
                .filter(Objects::nonNull)
                .map(mapper)
                .collect(Collectors.toList());
    }

    private static Object convertToType(String type, Object attributeValue) {
        if (type == null || attributeValue == null) return attributeValue;
        switch (type) {
            case "boolean":
                Boolean booleanObject = getBoolean(attributeValue);
                if (booleanObject != null) return booleanObject;
                if (attributeValue instanceof List) {
                    return transform((List<?>) attributeValue, OIDCAttributeMapperHelper::getBoolean);
                }
                return null;
            case "String":
                if (attributeValue instanceof String) return attributeValue;
                if (attributeValue instanceof List) {
                    return transform((List<?>) attributeValue, OIDCAttributeMapperHelper::getString);
                }
                return attributeValue.toString();
            case "long":
                Long longObject = getLong(attributeValue);
                if (longObject != null) return longObject;
                if (attributeValue instanceof List) {
                    return transform((List<?>) attributeValue, OIDCAttributeMapperHelper::getLong);
                }
                return null;
            case "int":
                Integer intObject = getInteger(attributeValue);
                if (intObject != null) return intObject;
                if (attributeValue instanceof List) {
                    return transform((List<?>) attributeValue, OIDCAttributeMapperHelper::getInteger);
                }
                return null;
            case "JSON":
                JsonNode jsonNodeObject = getJsonNode(attributeValue);
                if (jsonNodeObject != null) return jsonNodeObject;
                if (attributeValue instanceof List) {
                    return transform((List<?>) attributeValue, OIDCAttributeMapperHelper::getJsonNode);
                }
                return null;
            default:
                return attributeValue;
        }
    }

    private static String getString(Object attributeValue) {
        return attributeValue.toString();
    }


    private static Long getLong(Object attributeValue) {
        if (attributeValue instanceof Long) return (Long) attributeValue;
        if (attributeValue instanceof String) {
            try {
                return Long.valueOf((String) attributeValue);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static Integer getInteger(Object attributeValue) {
        if (attributeValue instanceof Integer) return (Integer) attributeValue;
        if (attributeValue instanceof String) {
            try {
                return Integer.valueOf((String) attributeValue);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static Boolean getBoolean(Object attributeValue) {
        if (attributeValue instanceof Boolean) return (Boolean) attributeValue;
        if (attributeValue instanceof String) return Boolean.valueOf((String) attributeValue);
        return null;
    }

    private static JsonNode getJsonNode(Object attributeValue) {
        if (attributeValue instanceof JsonNode){
            return (JsonNode) attributeValue;
        }
        if (attributeValue instanceof Map) {
            try {
                return JsonSerialization.createObjectNode(attributeValue);
            } catch (Exception ignore) {
            }
        }
        if (attributeValue instanceof String) {
            try {
                return JsonSerialization.readValue(attributeValue.toString(), JsonNode.class);
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    /**
     * 获取或初始化组织声明为可变 Map，供多个组织映射器组合写入。
     * <p>兼容 ObjectNode、Collection、String 或已有 Map，必要时创建空 Map。</p>
     * @param token 目标令牌
     * @param effectiveModel 有效映射器模型（用于解析组织声明路径）
     * @return 可变的组织声明 Map，修改会反映到令牌
     */
    public static Map<String, Object> getOrInitializeOrganizationClaimAsMap(IDToken token, ProtocolMapperModel effectiveModel) {
        List<String> claimPath = splitClaimPath(effectiveModel.getConfig().get(TOKEN_CLAIM_NAME));
        Object existingClaim = getNestedClaimValue(token.getOtherClaims(), claimPath);
        Map<String, Object> result;

        if (existingClaim instanceof ObjectNode) {
            // OrganizationMembershipMapper 输出为 JSON ObjectNode
            result = JsonSerialization.mapper.convertValue(existingClaim, Map.class);
        } else if (existingClaim instanceof Collection || existingClaim instanceof String) {
            // OrganizationMembershipMapper 输出为 String 或 Collection
            result = new HashMap<>();
            Stream<?> items = existingClaim instanceof Collection ? ((Collection<?>) existingClaim).stream() : Stream.of(existingClaim);
            items.filter(Objects::nonNull).forEach(item -> result.put(item.toString(), new HashMap<>()));
        } else if (existingClaim instanceof Map) {
            // 已是 Map，直接使用
            result = (Map<String, Object>) existingClaim;
        } else {
            result = new HashMap<>();
        }

        OIDCAttributeMapperHelper.mapClaim(token, effectiveModel, result);
        return result;
    }

    private static Object getNestedClaimValue(Map<String, Object> claims, List<String> path) {
        if (path.isEmpty()) return null;
        Map<String, Object> current = claims;
        for (int i = 0; i < path.size() - 1; i++) {
            Object next = current.get(path.get(i));
            if (!(next instanceof Map)) {
                return null;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> nested = (Map<String, Object>) next;
            current = nested;
        }
        return current.get(path.get(path.size() - 1));
    }

    /** 将属性值映射到 ID Token（或 Access Token）声明 */
    public static void mapClaim(IDToken token, ProtocolMapperModel mappingModel, Object attributeValue) {
        mapClaim(token, mappingModel, attributeValue, tokenPropertySetters, token.getOtherClaims());
    }

    /** 将属性值映射到访问令牌响应附加声明 */
    public static void mapClaim(AccessTokenResponse token, ProtocolMapperModel mappingModel, Object attributeValue) {
        mapClaim(token, mappingModel, attributeValue, responsePropertySetters, token.getOtherClaims());
    }

    private static <T> void mapClaim(T token, ProtocolMapperModel mappingModel, Object attributeValue,
                                     Map<String, PropertySetter<T>> setters, Map<String, Object> jsonObject) {
        attributeValue = mapAttributeValue(mappingModel, attributeValue);
        if (attributeValue == null) {
            return;
        }

        String protocolClaim = mappingModel.getConfig().get(TOKEN_CLAIM_NAME);
        if (protocolClaim == null) {
            return;
        }

        List<String> split = splitClaimPath(protocolClaim);
        if (split.isEmpty()) {
            return;
        }

        String firstClaim = split.iterator().next();
        PropertySetter<T> setter = setters.get(firstClaim);
        if (setter != null) {
            // 通过令牌标准字段 setter 写入
            if (split.size() > 1) {
                logger.warnf("Claim '%s' contains more than one level in a setter. Ignoring the assignment for mapper '%s'.",
                        protocolClaim, mappingModel.getName());
                return;
            }

            setter.set(protocolClaim, mappingModel.getName(), token, attributeValue);
            return;
        }

        // 写入 otherClaims 嵌套结构
        JsonUtils.mapClaim(split, attributeValue, jsonObject, isMultivalued(mappingModel));
    }

    /** 工厂方法：创建映射器（默认包含 UserInfo） */
    public static ProtocolMapperModel createClaimMapper(String name,
                                                        String userAttribute,
                                                        String tokenClaimName, String claimType,
                                                        boolean accessToken, boolean idToken, boolean introspectionEndpoint,
                                                        String mapperId) {
        return createClaimMapper(name, userAttribute, tokenClaimName, claimType, accessToken, idToken, true, introspectionEndpoint, mapperId);
    }

    /**
     * 工厂方法：创建完整 OIDC 声明映射器配置模型。
     * @param userAttribute 源用户属性（可为 null）
     * @param tokenClaimName 目标声明名
     * @param claimType JSON 类型
     * @param mapperId 映射器 provider id
     */
        ProtocolMapperModel mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(mapperId);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<>();
        config.put(ProtocolMapperUtils.USER_ATTRIBUTE, userAttribute);
        config.put(TOKEN_CLAIM_NAME, tokenClaimName);
        config.put(JSON_TYPE, claimType);
        if (accessToken) config.put(INCLUDE_IN_ACCESS_TOKEN, "true");
        if (idToken) config.put(INCLUDE_IN_ID_TOKEN, "true");
        if (userinfo) config.put(INCLUDE_IN_USERINFO, "true");
        if (introspectionEndpoint) config.put(INCLUDE_IN_INTROSPECTION, "true");
        mapper.setConfig(config);
        return mapper;
    }

    /** 是否配置为包含于 ID Token */
    public static boolean includeInIDToken(ProtocolMapperModel mappingModel) {
        return "true".equals(mappingModel.getConfig().get(INCLUDE_IN_ID_TOKEN));
    }

    /** 是否配置为包含于 Access Token */
    public static boolean includeInAccessToken(ProtocolMapperModel mappingModel) {
        return "true".equals(mappingModel.getConfig().get(INCLUDE_IN_ACCESS_TOKEN));
    }

    /** 是否配置为包含于访问令牌响应 */
    public static boolean includeInAccessTokenResponse(ProtocolMapperModel mappingModel) {
        return "true".equals(mappingModel.getConfig().get(INCLUDE_IN_ACCESS_TOKEN_RESPONSE));
    }

    /** 是否以多值（JSON 数组）形式写入声明 */
    public static boolean isMultivalued(ProtocolMapperModel mappingModel) {
        return "true".equals(mappingModel.getConfig().get(ProtocolMapperUtils.MULTIVALUED));
    }

    /** 是否包含于 UserInfo（未显式配置时与 ID Token 设置兼容） */
    public static boolean includeInUserInfo(ProtocolMapperModel mappingModel){
        String includeInUserInfo = mappingModel.getConfig().get(INCLUDE_IN_USERINFO);

        // 向后兼容：未配置时沿用 ID Token / Access Token 默认值
        if (includeInUserInfo == null && includeInIDToken(mappingModel)) {
            return true;
        }

        return "true".equals(includeInUserInfo);
    }

    /** 是否包含于内省响应（未显式配置时与 Access Token 设置兼容） */
    public static boolean includeInIntrospection(ProtocolMapperModel mappingModel) {
        String includeInIntrospection = mappingModel.getConfig().get(INCLUDE_IN_INTROSPECTION);

        // Backwards compatibility
        if (includeInIntrospection == null && includeInAccessToken(mappingModel)) {
            return true;
        }

        return "true".equals(includeInIntrospection);
    }

    /** 是否包含于轻量 Access Token */
    public static boolean includeInLightweightAccessToken(ProtocolMapperModel mappingModel) {
        return "true".equals(mappingModel.getConfig().get(INCLUDE_IN_LIGHTWEIGHT_ACCESS_TOKEN));
    }

    /** 追加声明名、JSON 类型及各令牌包含开关等标准配置项 */
    public static void addAttributeConfig(List<ProviderConfigProperty> configProperties, Class<? extends ProtocolMapper> protocolMapperClass) {
        addTokenClaimNameConfig(configProperties);
        addJsonTypeConfig(configProperties);

        addIncludeInTokensConfig(configProperties, protocolMapperClass);
    }

    /** 追加「令牌声明名」配置项 */
    public static void addTokenClaimNameConfig(List<ProviderConfigProperty> configProperties) {
        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName(TOKEN_CLAIM_NAME);
        property.setLabel(TOKEN_CLAIM_NAME_LABEL);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText(TOKEN_CLAIM_NAME_TOOLTIP);
        property.setRequired(true);
        configProperties.add(property);
    }

    /** 追加默认 JSON 类型列表配置项 */
    public static void addJsonTypeConfig(List<ProviderConfigProperty> configProperties) {
        addJsonTypeConfig(configProperties, List.of("String", "long", "int", "boolean", "JSON"), null);
    }

    /** 追加可定制类型列表的 JSON 类型配置项 */
    public static void addJsonTypeConfig(List<ProviderConfigProperty> configProperties, List<String> supportedTypes, String defaultValue) {
        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName(JSON_TYPE);
        property.setLabel(JSON_TYPE);
        property.setType(ProviderConfigProperty.LIST_TYPE);
        property.setOptions(supportedTypes);
        property.setHelpText(JSON_TYPE_TOOLTIP);
        property.setDefaultValue(defaultValue);
        configProperties.add(property);
    }

    /** 按映射器实现的接口追加 ID Token、Access Token、UserInfo 等包含开关 */
    public static void addIncludeInTokensConfig(List<ProviderConfigProperty> configProperties, Class<? extends ProtocolMapper> protocolMapperClass) {
        if (OIDCIDTokenMapper.class.isAssignableFrom(protocolMapperClass)) {
            ProviderConfigProperty property = new ProviderConfigProperty();
            property.setName(INCLUDE_IN_ID_TOKEN);
            property.setLabel(INCLUDE_IN_ID_TOKEN_LABEL);
            property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
            property.setDefaultValue("true");
            property.setHelpText(INCLUDE_IN_ID_TOKEN_HELP_TEXT);
            configProperties.add(property);
        }

        if (OIDCAccessTokenMapper.class.isAssignableFrom(protocolMapperClass)) {
            ProviderConfigProperty property = new ProviderConfigProperty();
            property.setName(INCLUDE_IN_ACCESS_TOKEN);
            property.setLabel(INCLUDE_IN_ACCESS_TOKEN_LABEL);
            property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
            property.setDefaultValue("true");
            property.setHelpText(INCLUDE_IN_ACCESS_TOKEN_HELP_TEXT);
            configProperties.add(property);

            ProviderConfigProperty property2 = new ProviderConfigProperty();
            property2.setName(INCLUDE_IN_LIGHTWEIGHT_ACCESS_TOKEN);
            property2.setLabel(INCLUDE_IN_LIGHTWEIGHT_ACCESS_TOKEN_LABEL);
            property2.setType(ProviderConfigProperty.BOOLEAN_TYPE);
            property2.setDefaultValue("false");
            property2.setHelpText(INCLUDE_IN_LIGHTWEIGHT_ACCESS_TOKEN_HELP_TEXT);
            configProperties.add(property2);
        }

        if (UserInfoTokenMapper.class.isAssignableFrom(protocolMapperClass)) {
            ProviderConfigProperty property = new ProviderConfigProperty();
            property.setName(INCLUDE_IN_USERINFO);
            property.setLabel(INCLUDE_IN_USERINFO_LABEL);
            property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
            property.setDefaultValue("true");
            property.setHelpText(INCLUDE_IN_USERINFO_HELP_TEXT);
            configProperties.add(property);
        }

        if (OIDCAccessTokenResponseMapper.class.isAssignableFrom(protocolMapperClass)) {
            ProviderConfigProperty property = new ProviderConfigProperty();
            property.setName(INCLUDE_IN_ACCESS_TOKEN_RESPONSE);
            property.setLabel(INCLUDE_IN_ACCESS_TOKEN_RESPONSE_LABEL);
            property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
            property.setDefaultValue("false");
            property.setHelpText(INCLUDE_IN_ACCESS_TOKEN_RESPONSE_HELP_TEXT);
            configProperties.add(property);
        }

        if (TokenIntrospectionTokenMapper.class.isAssignableFrom(protocolMapperClass)) {
            ProviderConfigProperty property = new ProviderConfigProperty();
            property.setName(INCLUDE_IN_INTROSPECTION);
            property.setLabel(INCLUDE_IN_INTROSPECTION_LABEL);
            property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
            property.setDefaultValue("true");
            property.setHelpText(INCLUDE_IN_INTROSPECTION_HELP_TEXT);
            configProperties.add(property);
        }
    }
}
