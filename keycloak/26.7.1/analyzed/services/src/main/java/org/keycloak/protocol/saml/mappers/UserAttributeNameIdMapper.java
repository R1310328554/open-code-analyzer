package org.keycloak.protocol.saml.mappers;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 用户属性 NameID 映射器：将指定用户自定义属性的首值作为 SAML NameID。
 * <p>实现 {@link SAMLNameIdMapper}，适用于 SP 要求 NameID 来自特定属性的场景。</p>
 */
public class UserAttributeNameIdMapper extends AbstractSAMLProtocolMapper implements SAMLNameIdMapper {

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        NameIdMapperHelper.setConfigProperties(configProperties);
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(ProtocolMapperUtils.USER_ATTRIBUTE);
        property.setLabel(ProtocolMapperUtils.USER_MODEL_ATTRIBUTE_LABEL);
        property.setHelpText(ProtocolMapperUtils.USER_MODEL_ATTRIBUTE_HELP_TEXT);
        property.setType(ProviderConfigProperty.USER_PROFILE_ATTRIBUTE_LIST_TYPE);
        property.setRequired(Boolean.TRUE);
        configProperties.add(property);
    }

    /** SPI 提供者标识符 */
    public static final String PROVIDER_ID = "saml-user-attribute-nameid-mapper";

    /** {@inheritDoc} 返回用户属性与 NameID 相关配置项 */
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }
    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 控制台显示名：User Attribute Mapper For NameID */
    @Override
    public String getDisplayType() {
        return "User Attribute Mapper For NameID";
    }

    /** {@inheritDoc} 归类为 NameID 映射器 */
    @Override
    public String getDisplayCategory() {
        return "NameID Mapper";
    }

    /** {@inheritDoc} 将用户属性映射为 SAML NameID 值 */
    @Override
    public String getHelpText() {
        return "Map user attribute to SAML NameID value.";
    }

    /** {@inheritDoc} 读取配置的用户属性首值作为 NameID */
    @Override
    public String mapperNameId(String nameIdFormat, ProtocolMapperModel mappingModel, KeycloakSession session,
            UserSessionModel userSession, AuthenticatedClientSessionModel clientSession) {
        return userSession.getUser().getFirstAttribute(mappingModel.getConfig().get(ProtocolMapperUtils.USER_ATTRIBUTE));
    }

}
