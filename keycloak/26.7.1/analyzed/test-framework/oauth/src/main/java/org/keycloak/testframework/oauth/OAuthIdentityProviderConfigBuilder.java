package org.keycloak.testframework.oauth;

/**
 * 构建 {@link OAuthIdentityProvider} 所需配置的流式构建器。
 * <p>
 * 支持 SPIFFE、Kubernetes 等特殊模式，以及是否启用 JWK 端点等选项。
 */
public class OAuthIdentityProviderConfigBuilder {

    private Mode mode = Mode.DEFAULT;
    private boolean jwkUse = true;

    /** 启用 SPIFFE 身份模式。 */
    public OAuthIdentityProviderConfigBuilder spiffe() {
        mode = Mode.SPIFFE;
        return this;
    }

    /** 启用 Kubernetes 服务账户身份模式。 */
    public OAuthIdentityProviderConfigBuilder kubernetes() {
        mode = Mode.KUBERNETES;
        return this;
    }

    /**
     * 设置是否在 JWK 响应中包含 {@code use} 字段。
     *
     * @param jwkUse 是否输出 JWK use 属性
     * @return 当前构建器
     */
    public OAuthIdentityProviderConfigBuilder jwkUse(boolean jwkUse) {
        this.jwkUse = jwkUse;
        return this;
    }

    /** 根据当前选项生成不可变配置快照。 */
    public OAuthIdentityProviderConfiguration build() {
        return new OAuthIdentityProviderConfiguration(mode, jwkUse);
    }

    /** 身份提供者运行时配置：运行模式与 JWK use 开关。 */
    public record OAuthIdentityProviderConfiguration(Mode mode, boolean jwkUse) {
    }

    /** 模拟身份提供者的运行模式。 */
    public enum Mode {
        /** 标准 OAuth/OIDC 模拟 IdP。 */
        DEFAULT,
        /** SPIFFE 工作负载身份模式。 */
        SPIFFE,
        /** Kubernetes 服务账户 JWT 模式。 */
        KUBERNETES
    }

}
