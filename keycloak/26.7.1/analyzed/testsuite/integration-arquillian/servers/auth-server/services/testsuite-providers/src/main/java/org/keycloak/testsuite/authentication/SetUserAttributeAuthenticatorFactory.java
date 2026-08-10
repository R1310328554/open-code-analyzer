package org.keycloak.testsuite.authentication;

import java.util.Arrays;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;


/**
 * 设置用户属性认证器工厂，注册可在认证流程中写入用户属性的测试认证器。
 */
public class SetUserAttributeAuthenticatorFactory implements AuthenticatorFactory {

    /** 提供者在 SPI 中的标识符。 */
    public static final String PROVIDER_ID = "set-attribute";

    /** 配置项键名：要设置的用户属性名称。 */
    public static final String CONF_ATTR_NAME = "attr_name";
    /** 配置项键名：要写入的用户属性值。 */
    public static final String CONF_ATTR_VALUE = "attr_value";
    /** 支持的认证执行要求：REQUIRED 或 DISABLED。 */
    protected static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED};

    @Override
    public String getReferenceCategory() {
        return null;
    }

    /** {@inheritDoc} 支持配置属性名与属性值。 */
    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    /** {@inheritDoc} 返回 {@link #REQUIREMENT_CHOICES}。 */
    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }


    /** {@inheritDoc} 说明该认证器用于设置用户属性。 */
    @Override
    public String getHelpText() {
        return "Set a user attribute";
    }

    @Override
    public void init(Config.Scope scope) {
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID}。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 创建新的 {@link SetUserAttributeAuthenticator} 实例。 */
    @Override
    public Authenticator create(KeycloakSession keycloakSession) {
        return new SetUserAttributeAuthenticator();
    }

    /** {@inheritDoc} 管理控制台展示名称。 */
    @Override
    public String getDisplayType() {
        return "Set user attribute";
    }

    /** {@inheritDoc} 返回属性名与属性值两项配置。 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty attributeName = new ProviderConfigProperty();
        attributeName.setType(ProviderConfigProperty.STRING_TYPE);
        attributeName.setName(CONF_ATTR_NAME);
        attributeName.setLabel("Attribute name");
        attributeName.setHelpText("Name of the user attribute to set");

        ProviderConfigProperty attributeValue = new ProviderConfigProperty();
        attributeValue.setType(ProviderConfigProperty.STRING_TYPE);
        attributeValue.setName(CONF_ATTR_VALUE);
        attributeValue.setLabel("Attribute value");
        attributeValue.setHelpText("Value to set in the user attribute");

        return Arrays.asList(attributeName, attributeValue);
    }
}
