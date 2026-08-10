package org.keycloak.social.openshift;

import java.util.List;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * OpenShift v4 身份提供者配置。
 * <p>支持通过 {@code baseUrl} 覆盖默认集群 API 基址。</p>
 *
 * @author David Festal and Sebastian Łaskawiec
 */
public class OpenshiftV4IdentityProviderConfig extends OAuth2IdentityProviderConfig {

    /** 配置项键名：集群 API 基址。 */
    private static final String BASE_URL = "baseUrl";

    /** 从领域 IdP 模型构造配置。 */
    public OpenshiftV4IdentityProviderConfig(IdentityProviderModel identityProviderModel) {
        super(identityProviderModel);
    }

    /** 创建空配置实例。 */
    public OpenshiftV4IdentityProviderConfig() {
    }

    /** 去除 baseUrl 末尾斜杠，避免拼接路径时出现双斜杠。 */
    private String trimTrailingSlash(String baseUrl) {
        if (baseUrl != null && baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    /** 获取集群 API 基址；未配置时由 IdP 使用默认值。 */
    public String getBaseUrl() {
        return getConfig().get(BASE_URL);
    }

    /** 设置集群 API 基址（自动去除末尾斜杠）。 */
    public void setBaseUrl(String baseUrl) {
        getConfig().put(BASE_URL, trimTrailingSlash(baseUrl));
    }

    /** 返回管理控制台可编辑的配置属性列表。 */
    public static List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property().name(BASE_URL)
                .label("Base URL")
                .helpText("Override the default Base URL for this identity provider.")
                .type(ProviderConfigProperty.STRING_TYPE)
                .add().build();
    }
}
