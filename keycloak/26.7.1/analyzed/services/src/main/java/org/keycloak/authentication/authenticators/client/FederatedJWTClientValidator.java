package org.keycloak.authentication.authenticators.client;

import java.util.Collections;
import java.util.List;

import org.keycloak.authentication.ClientAuthenticationFlowContext;
import org.keycloak.services.Urls;

/**
 * 联合 JWT 客户端断言校验器：针对外部身份提供者签发的 JWT 执行 issuer、audience、时钟偏差与过期时间等校验。
 */
public class FederatedJWTClientValidator extends AbstractJWTClientValidator {

    /** 期望的 JWT 签发者（iss）。 */
    private final String expectedTokenIssuer;
    /** 允许的时钟偏差（秒）。 */
    private final int allowedClockSkew;
    /** 是否允许重用同一 JWT（jti）。 */
    private final boolean reusePermitted;
    /** JWT 最大有效时长（秒），默认 300。 */
    private int maximumExpirationTime = 300;
    /** 允许的 audience 列表；为空时使用 realm issuer。 */
    private final List<String> validAudiences;

    /**
     * 构造联合 JWT 校验器。
     *
     * @param context 客户端认证流程上下文
     * @param signatureValidator 签名校验回调
     * @param expectedTokenIssuer 期望签发者
     * @param allowedClockSkew 时钟偏差（秒）
     * @param reusePermitted 是否允许重用
     * @param validAudiences 允许的 audience
     */
    public FederatedJWTClientValidator(ClientAuthenticationFlowContext context, SignatureValidator signatureValidator,
            String expectedTokenIssuer, int allowedClockSkew, boolean reusePermitted, String... validAudiences) throws Exception {
        super(context, signatureValidator, null);
        this.expectedTokenIssuer = expectedTokenIssuer;
        this.allowedClockSkew = allowedClockSkew;
        this.reusePermitted = reusePermitted;
        this.validAudiences = validAudiences == null ? Collections.emptyList() : List.of(validAudiences);
    }

    /** @return 配置的期望 JWT 签发者 */
    @Override
    protected String getExpectedTokenIssuer() {
        return expectedTokenIssuer;
    }

    /** @return 期望 audience 列表；未配置时使用 realm issuer */
    @Override
    protected List<String> getExpectedAudiences() {
        return validAudiences.isEmpty()
                ? List.of(Urls.realmIssuer(context.getUriInfo().getBaseUri(), realm.getName()))
                : validAudiences;
    }

    /** @return 是否允许多个 audience（联合场景为 false） */
    @Override
    protected boolean isMultipleAudienceAllowed() {
        return false;
    }

    @Override
    protected int getAllowedClockSkew() {
        return allowedClockSkew;
    }

    @Override
    protected int getMaximumExpirationTime() {
        return maximumExpirationTime;
    }

    /** 设置 JWT 最大有效时长（秒）。 */
    public void setMaximumExpirationTime(int maximumExpirationTime) {
        this.maximumExpirationTime = maximumExpirationTime;
    }

    @Override
    protected boolean isReusePermitted() {
        return reusePermitted;
    }

    /** @return 期望签名算法（当前硬编码为 null，待可配置化） */
    @Override
    protected String getExpectedSignatureAlgorithm() {
        return null; // TODO Hard-coded to no expected signature algorithm for now, but should be configurable
    }

    /** 设置期望的 client_assertion_type。 */
    public void setExpectedClientAssertionType(String clientAssertionType) {
        this.expectedClientAssertionType = clientAssertionType;
    }
}
