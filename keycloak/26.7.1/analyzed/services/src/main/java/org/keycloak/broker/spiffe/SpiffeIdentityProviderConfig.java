package org.keycloak.broker.spiffe;

import java.util.regex.Pattern;

import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.RealmModel;

import static org.keycloak.common.util.UriUtils.checkUrl;

/**
 * SPIFFE 身份代理配置：trust domain 与 bundle 端点 URL。
 * <p>用于联邦 JWT 客户端认证场景下的 SPIFFE JWT SVID 校验。</p>
 */
public class SpiffeIdentityProviderConfig extends IdentityProviderModel {

    /** 配置键：SPIFFE trust bundle JWKS 端点 URL。 */
    public static final String BUNDLE_ENDPOINT_KEY = "bundleEndpoint";
    /** 配置键：SPIFFE trust domain（spiffe:// 前缀格式）。 */
    public static final String TRUST_DOMAIN_KEY = "trustDomain";

    private static final Pattern TRUST_DOMAIN_PATTERN = Pattern.compile("spiffe://[a-z0-9.\\-_]*");

    public SpiffeIdentityProviderConfig() {
    }

    public SpiffeIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    /** @return token 校验允许的时钟偏差（秒） */
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

    /** @return SPIFFE trust domain */
    public String getTrustDomain() {
        return getConfig().get(TRUST_DOMAIN_KEY);
    }

    /** @return SPIFFE bundle JWKS 端点 URL */
    public String getBundleEndpoint() {
        return getConfig().get(BUNDLE_ENDPOINT_KEY);
    }

    /** 校验 trust domain 格式与 bundle 端点 URL 的 SSL 要求。 */
    @Override
    public void validate(RealmModel realm) {
        super.validate(realm);

        String trustDomain = getTrustDomain();
        if (trustDomain == null || !TRUST_DOMAIN_PATTERN.matcher(trustDomain).matches()) {
            throw new IllegalArgumentException("Invalid trust domain name");
        }

        checkUrl(realm.getSslRequired(), getBundleEndpoint(), BUNDLE_ENDPOINT_KEY);
    }
}
