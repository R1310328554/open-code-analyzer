package org.keycloak.social.facebook;

import java.util.Optional;

import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

/**
 * Facebook IdP 扩展配置。
 * <p>支持配置 Graph API profile 请求的额外字段 {@code fetchedFields}。</p>
 */
public class FacebookIdentityProviderConfig extends OIDCIdentityProviderConfig {

    /** 从 realm 身份提供者模型构造配置。 */
    public FacebookIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    /** 创建空配置（管理端新建 IdP 时使用）。 */
    public FacebookIdentityProviderConfig() {
    }

    /** 获取追加到 profile 请求的额外字段（去除空白）。 */
    public String getFetchedFields() {
        return Optional.ofNullable(getConfig().get("fetchedFields"))
                .map(fieldsConfig -> fieldsConfig.replaceAll("\\s+",""))
                .orElse("");
    }

    /** 设置 Graph API profile 请求的额外字段。 */
    public void setFetchedFields(final String fetchedFields) {
        getConfig().put("fetchedFields", fetchedFields);
    }
}
