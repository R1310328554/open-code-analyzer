package org.keycloak.protocol.oidc.mappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.keycloak.common.util.CollectionUtil;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;
import org.keycloak.utils.StringUtil;

/**
 * 参数化 Scope 用户属性映射器：用 scope 参数（用户名）解析用户，并将其属性/字段映射为令牌声明。
 * <p>扩展 {@link ParameterizedScopeMapper}，支持跨用户属性查询。</p>
 */
public class ParameterizedScopeUserPropertyMapper extends ParameterizedScopeMapper {

    /** SPI 提供者标识符 */
    public static final String PROVIDER_ID = "oidc-parameterized-scope-user-property-mapper";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName(ProtocolMapperUtils.USER_ATTRIBUTE);
        property.setLabel(ProtocolMapperUtils.USER_MODEL_ATTRIBUTE_LABEL);
        property.setHelpText("User profile attribute or built-in property (e.g. id, email, firstName) to map to the token claim.");
        property.setType(ProviderConfigProperty.USER_PROFILE_ATTRIBUTE_LIST_TYPE);
        configProperties.add(property);

        OIDCAttributeMapperHelper.addAttributeConfig(configProperties, ParameterizedScopeUserPropertyMapper.class);
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 控制台显示名：Parameterized Scope User Property */
    @Override
    public String getDisplayType() {
        return "Parameterized Scope User Property";
    }

    /** {@inheritDoc} 按 scope 参数解析用户并映射其属性到声明 */
    @Override
    public String getHelpText() {
        return "Resolves a user from a parameterized scope parameter (username) and maps a user attribute or property to a token claim.";
    }

    /** {@inheritDoc} 含用户属性选择与标准声明配置项 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    /** 按参数值查找用户，解析配置的用户属性或内置字段后写入声明 */
    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession,
                            KeycloakSession keycloakSession, List<String> parameterValues) {
        String attributeName = mappingModel.getConfig().get(ProtocolMapperUtils.USER_ATTRIBUTE);
        if (StringUtil.isBlank(attributeName)) {
            return;
        }

        List<Object> resolvedValues = new ArrayList<>();
        for (String parameterValue : parameterValues) {
            UserModel user = keycloakSession.users().getUserByUsername(userSession.getRealm(), parameterValue);
            if (user == null) {
                continue;
            }

            Collection<String> attributeValue = KeycloakModelUtils.resolveAttribute(user, attributeName, false);
            if (CollectionUtil.isNotEmpty(attributeValue)) {
                resolvedValues.addAll(attributeValue);
                continue;
            }

            String propertyValue = ProtocolMapperUtils.getUserModelValue(user, attributeName);
            if (propertyValue != null) {
                resolvedValues.add(propertyValue);
            }
        }

        if (!resolvedValues.isEmpty()) {
            OIDCAttributeMapperHelper.mapClaim(token, mappingModel, resolvedValues);
        }
    }

    /** 工厂方法：创建映射器（默认非多值） */
    public static ProtocolMapperModel create(String name, String userAttribute,
                                              String tokenClaimName, String claimType,
                                              boolean accessToken, boolean idToken, boolean introspectionEndpoint) {
        return create(name, userAttribute, tokenClaimName, claimType, accessToken, idToken, introspectionEndpoint, false);
    }

    /**
     * 工厂方法：创建带多值选项的参数化 scope 用户属性映射器。
     * @param multivalued 是否以数组形式写入多值
     */
        ProtocolMapperModel mapper = OIDCAttributeMapperHelper.createClaimMapper(
                name, userAttribute, tokenClaimName, claimType,
                accessToken, idToken, false, introspectionEndpoint,
                PROVIDER_ID);
        mapper.getConfig().put(ProtocolMapperUtils.MULTIVALUED, Boolean.toString(multivalued));
        return mapper;
    }
}
