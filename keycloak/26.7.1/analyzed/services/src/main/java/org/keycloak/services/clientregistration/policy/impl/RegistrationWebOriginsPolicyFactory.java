package org.keycloak.services.clientregistration.policy.impl;

import java.util.List;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.clientregistration.policy.AbstractClientRegistrationPolicyFactory;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy;

/**
 * 注册 Web Origins 策略工厂。
 * <p>创建 {@link RegistrationWebOriginsPolicy}，用于配置客户端注册请求允许的 Web Origin 白名单。</p>
 */
public class RegistrationWebOriginsPolicyFactory extends AbstractClientRegistrationPolicyFactory {

    /** Provider 唯一标识符 */
    public static final String PROVIDER_ID = "registration-web-origins";

    /** 配置项键：允许的 Web Origin 列表 */
    public static final String WEB_ORIGINS = "web-origins";

    /** Web Origins 多值字符串配置属性定义 */
    private static final ProviderConfigProperty WEB_ORIGINS_PROPERTY = new ProviderConfigProperty(WEB_ORIGINS, "registration-web-origins.label", "registration-web-origins.tooltip", ProviderConfigProperty.MULTIVALUED_STRING_TYPE, null);

    /** {@inheritDoc} 创建 {@link RegistrationWebOriginsPolicy} 实例 */
    @Override
    public ClientRegistrationPolicy create(KeycloakSession session, ComponentModel model) {
        return new RegistrationWebOriginsPolicy(session, model);
    }

    /** {@inheritDoc} 返回策略帮助说明 */
    @Override
    public String getHelpText() {
        return "Allowed web origins for client registration requests";
    }

    /** {@inheritDoc} 返回 Web Origins 配置属性列表 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of(WEB_ORIGINS_PROPERTY);
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
