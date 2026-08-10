package org.keycloak.broker.kubernetes;


import org.keycloak.broker.oidc.IssuerValidation;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.IdentityProviderType;
import org.keycloak.models.RealmModel;
import org.keycloak.util.Strings;

import static org.keycloak.broker.kubernetes.KubernetesConstants.DEFAULT_KUBERNETES_ISSUER_URL;


/**
 * Kubernetes 客户端断言 IdP 配置：默认 issuer 为集群内 API 服务地址。
 */
public class KubernetesIdentityProviderConfig extends IdentityProviderModel implements IssuerValidation {

    /** 默认构造。 */
    public KubernetesIdentityProviderConfig() {
    }

    /** 从已有模型复制配置。 */
    public KubernetesIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    /** @return issuer；未配置时使用 {@link KubernetesConstants#DEFAULT_KUBERNETES_ISSUER_URL} */
    public String getIssuer() {
        String issuer = getConfig().get(ISSUER);
        if (Strings.isEmpty(issuer)) {
            return DEFAULT_KUBERNETES_ISSUER_URL;
        }

        return issuer;
    }

    /** @return 时钟偏差秒数，解析失败时返回 0 */
    public int getAllowedClockSkew() {
        String allowedClockSkew = getConfig().get(ALLOWED_CLOCK_SKEW);
        if (allowedClockSkew == null || allowedClockSkew.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(getConfig().get(ALLOWED_CLOCK_SKEW));
        } catch (NumberFormatException e) {
            // 解析失败时使用默认值 0
            return 0;
        }
    }

    @Override
    /** Kubernetes IdP 不在登录页展示。 */
    public Boolean isHideOnLogin() {
        return true;
    }

    @Override
    /** 规范化 issuer 并执行 issuer 校验。 */
    public void validate(RealmModel realm) {
        super.validate(realm);
        getConfig().put(ISSUER, getIssuer());
        validateIssuer(realm, IdentityProviderType.CLIENT_ASSERTION);
    }
}
