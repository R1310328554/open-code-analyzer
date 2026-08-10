package org.keycloak.authentication.authenticators.client;

import java.util.Map;

import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.ClientAuthenticationFlowContext;
import org.keycloak.broker.provider.ClientAssertionIdentityProviderFactory;
import org.keycloak.cache.AlternativeLookupProvider;
import org.keycloak.models.ClientModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.IdentityProviderType;

/**
 * 默认客户端断言策略：通过 JWT iss 查找 IdP，再按 federated subject 与 issuer 别名匹配本地客户端。
 */
public class DefaultClientAssertionStrategy implements ClientAssertionIdentityProviderFactory.ClientAssertionStrategy {

    @Override
    /** @param assertionType 断言类型
     * @return 是否为 JWT 客户端断言类型 */
    public boolean isSupportedAssertionType(String  assertionType) {
        return OAuth2Constants.CLIENT_ASSERTION_TYPE_JWT.equals(assertionType);
    }

    @Override
    /** 根据 JWT iss/sub 查找联邦 IdP 及对应的本地客户端。 */
    public ClientAssertionIdentityProviderFactory.LookupResult lookup(ClientAuthenticationFlowContext context) throws Exception {
        ClientAssertionState clientAssertionState = context.getState(ClientAssertionState.class, ClientAssertionState.supplier());
        AlternativeLookupProvider lookupProvider = context.getSession().getProvider(AlternativeLookupProvider.class);

        String issuer = clientAssertionState.getToken().getIssuer();
        String federatedClientId =  clientAssertionState.getToken().getSubject();

        IdentityProviderModel identityProvider = lookupProvider.lookupIdentityProviderFromIssuer(context.getSession(), IdentityProviderType.CLIENT_ASSERTION, issuer);
        if (identityProvider == null) {
            return null;
        }

        ClientModel client = lookupProvider.lookupClientFromClientAttributes(
                context.getSession(),
                Map.of(
                        FederatedJWTClientAuthenticator.JWT_CREDENTIAL_SUBJECT_KEY, federatedClientId,
                        FederatedJWTClientAuthenticator.JWT_CREDENTIAL_ISSUER_KEY, identityProvider.getAlias()
                )
        );

        return new ClientAssertionIdentityProviderFactory.LookupResult(client, identityProvider);
    }

}
