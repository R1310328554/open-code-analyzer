package org.keycloak.organization.protocol.mappers.oidc;

import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.organization.OrganizationProvider;
import org.keycloak.organization.utils.Organizations;
import org.keycloak.protocol.oidc.token.TokenInterceptorException;
import org.keycloak.protocol.oidc.token.TokenPostProcessor;
import org.keycloak.protocol.oidc.token.TokenPostProcessorContext;
import org.keycloak.representations.RefreshToken;

/**
 * 组织令牌后处理器：在签发或刷新令牌时将组织别名写入 refresh token 的 otherClaims，并在刷新时校验组织仍有效且用户仍为成员。
 * <p>实现 {@link TokenPostProcessor}，无效组织或非成员时抛出 {@link TokenInterceptorException}。</p>
 */
public class OrganizationTokenPostProcessor implements TokenPostProcessor{

    private final KeycloakSession session;

    /** @param session Keycloak 会话 */
    public OrganizationTokenPostProcessor(KeycloakSession session) {
        this.session = session;
    }

    @Override
    /** 根据 grant 类型从上下文或旧 refresh token 提取组织别名并写入新 refresh token。 */
    public void process(TokenPostProcessorContext context) {
        String grantType = context.clientSessionCtx().getAttribute(Constants.GRANT_TYPE, String.class);

        if (OAuth2Constants.REFRESH_TOKEN.equals(grantType)) {
            RefreshToken refreshToken = context.requestRefreshToken();

            if (refreshToken != null) {
                Object orgAlias = refreshToken.getOtherClaims().get(OAuth2Constants.ORGANIZATION);
                if (orgAlias != null) {
                    addOrganizationRefreshTokenClaim(context, orgAlias.toString());
                }
            }
        } else {
            OrganizationModel organization = session.getContext().getOrganization();

            if (organization != null) {
                addOrganizationRefreshTokenClaim(context, organization.getAlias());
            }
        }
    }

    private void addOrganizationRefreshTokenClaim(TokenPostProcessorContext context, String orgAlias) {
        if (orgAlias == null || !Organizations.isEnabled(session)) {
            return;
        }

        OrganizationProvider provider = Organizations.getProvider(session);
        ClientSessionContext clientSessionContext = context.clientSessionCtx();
        AuthenticatedClientSessionModel clientSession = clientSessionContext.getClientSession();
        UserSessionModel userSession = clientSession.getUserSession();
        OrganizationModel organization = provider.getByAlias(orgAlias);

        if (organization == null || !organization.isEnabled()) {
            throw new TokenInterceptorException(OAuthErrorException.INVALID_REQUEST, OAuthErrorException.INVALID_TOKEN);
        }

        UserModel user = userSession.getUser();

        if (user != null && !organization.isMember(user)) {
            throw new TokenInterceptorException(OAuthErrorException.INVALID_REQUEST, OAuthErrorException.INVALID_TOKEN);
        }

        RefreshToken newRefreshToken = context.refreshToken();

        if (newRefreshToken != null) {
            newRefreshToken.getOtherClaims().put(OAuth2Constants.ORGANIZATION, orgAlias);
        }
    }
}
