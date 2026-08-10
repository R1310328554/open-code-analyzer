package org.keycloak.social.openshift;

import java.util.List;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * OpenShift v4 社交身份提供者工厂。
 * <p>注册 provider id {@code openshift-v4} 并创建 {@link OpenshiftV4IdentityProvider} 实例。</p>
 *
 * @author David Festal and Sebastian Łaskawiec
 */
public class OpenshiftV4IdentityProviderFactory extends AbstractIdentityProviderFactory<OpenshiftV4IdentityProvider> implements SocialIdentityProviderFactory<OpenshiftV4IdentityProvider> {

    /** OpenShift v4 IdP 的 provider id。 */
    public static final String PROVIDER_ID = "openshift-v4";
    /** 管理控制台显示名称。 */
    public static final String NAME = "Openshift v4";

    /** 返回 {@link #NAME}。 */
    @Override
    public String getName() {
        return NAME;
    }

    /** 根据领域模型创建 {@link OpenshiftV4IdentityProvider} 实例。 */
    @Override
    public OpenshiftV4IdentityProvider create(KeycloakSession keycloakSession, IdentityProviderModel identityProviderModel) {
        return new OpenshiftV4IdentityProvider(keycloakSession, new OpenshiftV4IdentityProviderConfig(identityProviderModel));
    }

    /** 返回 {@link #PROVIDER_ID}。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** 创建默认 OpenShift v4 配置对象。 */
    @Override
    public OpenshiftV4IdentityProviderConfig createConfig() {
        return new OpenshiftV4IdentityProviderConfig();
    }

    /** 委托 {@link OpenshiftV4IdentityProviderConfig#getConfigProperties()} 返回配置项。 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return OpenshiftV4IdentityProviderConfig.getConfigProperties();
    }
}
