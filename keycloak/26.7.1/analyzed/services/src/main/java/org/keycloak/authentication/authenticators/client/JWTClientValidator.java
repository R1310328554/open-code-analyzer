package org.keycloak.authentication.authenticators.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.keycloak.authentication.ClientAuthenticationFlowContext;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.OIDCLoginProtocolService;
import org.keycloak.protocol.oidc.OIDCProviderConfig;
import org.keycloak.protocol.oidc.grants.ciba.CibaGrantType;
import org.keycloak.protocol.oidc.par.endpoints.ParEndpoint;
import org.keycloak.services.Urls;

/**
 * 标准 JWT 客户端断言校验器：校验 issuer、audience、时钟偏差、过期时间与签名算法等。
 * <p>issuer 期望为 JWT subject（客户端 ID）；audience 包含 token、introspect、PAR 等 OIDC 端点。</p>
 */
public class JWTClientValidator extends AbstractJWTClientValidator {

    /**
     * 构造 JWT 客户端断言校验器。
     *
     * @param context 客户端认证流程上下文
     * @param signatureValidator 签名校验回调
     * @param clientAuthenticatorProviderId 认证器提供者 ID
     */
    public JWTClientValidator(ClientAuthenticationFlowContext context, SignatureValidator signatureValidator, String clientAuthenticatorProviderId) throws Exception {
        super(context, signatureValidator, clientAuthenticatorProviderId);
    }

    /** @return 期望 issuer，即 JWT subject（客户端 ID） */
    @Override
    protected String getExpectedTokenIssuer() {
        return clientAssertionState.getToken().getSubject();
    }

    /** @return OIDC 相关端点 URL 作为期望 audience 列表 */
    @Override
    protected List<String> getExpectedAudiences() {
        String issuerUrl = Urls.realmIssuer(context.getUriInfo().getBaseUri(), realm.getName());
        String tokenUrl = OIDCLoginProtocolService.tokenUrl(context.getUriInfo().getBaseUriBuilder()).build(realm.getName()).toString();
        String tokenIntrospectUrl = OIDCLoginProtocolService.tokenIntrospectionUrl(context.getUriInfo().getBaseUriBuilder()).build(realm.getName()).toString();
        String parEndpointUrl = ParEndpoint.parUrl(context.getUriInfo().getBaseUriBuilder()).build(realm.getName()).toString();
        List<String> expectedAudiences = new ArrayList<>(Arrays.asList(issuerUrl, tokenUrl, tokenIntrospectUrl, parEndpointUrl));
        String backchannelAuthenticationUrl = CibaGrantType.authorizationUrl(context.getUriInfo().getBaseUriBuilder()).build(realm.getName()).toString();
        expectedAudiences.add(backchannelAuthenticationUrl);

        return expectedAudiences;
    }

    /** @return 是否允许多 audience，取决于 OIDC 提供者配置 */
    @Override
    protected boolean isMultipleAudienceAllowed() {
        OIDCLoginProtocol loginProtocol = (OIDCLoginProtocol) context.getSession().getProvider(LoginProtocol.class, OIDCLoginProtocol.LOGIN_PROTOCOL);
        OIDCProviderConfig config = loginProtocol.getConfig();
        return config.isAllowMultipleAudiencesForJwtClientAuthentication();
    }

    /** @return 允许时钟偏差（秒），固定 15 */
    @Override
    protected int getAllowedClockSkew() {
        return 15;
    }

    /** @return JWT 最大有效时长，取自客户端 token-endpoint-auth-signing-max-exp 配置 */
    protected int getMaximumExpirationTime() {
        return OIDCAdvancedConfigWrapper.fromClientModel(clientAssertionState.getClient()).getTokenEndpointAuthSigningMaxExp();
    }

    /** @return 不允许重用同一 JWT */
    @Override
    protected boolean isReusePermitted() {
        return false;
    }

    /** @return 期望签名算法，取自客户端 token-endpoint-auth-signing-alg 配置 */
    @Override
    protected String getExpectedSignatureAlgorithm() {
        return OIDCAdvancedConfigWrapper.fromClientModel(clientAssertionState.getClient()).getTokenEndpointAuthSigningAlg();
    }

}
