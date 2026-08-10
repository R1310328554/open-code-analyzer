package org.keycloak.broker.spiffe;

import java.util.Map;

import org.keycloak.authentication.ClientAuthenticationFlowContext;
import org.keycloak.authentication.authenticators.client.ClientAssertionState;
import org.keycloak.authentication.authenticators.client.FederatedJWTClientAuthenticator;
import org.keycloak.broker.provider.ClientAssertionIdentityProviderFactory;
import org.keycloak.cache.AlternativeLookupProvider;
import org.keycloak.models.ClientModel;
import org.keycloak.models.IdentityProviderModel;

/**
 * SPIFFE 客户端断言查找策略：按 JWT sub（SPIFFE ID）匹配联邦客户端并解析关联 IdP。
 * <p>断言类型为 {@link SpiffeConstants#CLIENT_ASSERTION_TYPE}。</p>
 */
public class SpiffeClientAssertionStrategy implements ClientAssertionIdentityProviderFactory.ClientAssertionStrategy {

    /** @return 是否为 SPIFFE jwt-spiffe 客户端断言类型 */
    @Override
    public boolean isSupportedAssertionType(String assertionType) {
        return SpiffeConstants.CLIENT_ASSERTION_TYPE.equals(assertionType);
    }

    /** 按 token sub 查找客户端，再通过 issuer 属性解析 {@link IdentityProviderModel}。 */
    @Override
    public ClientAssertionIdentityProviderFactory.LookupResult lookup(ClientAuthenticationFlowContext context) throws Exception {
        ClientAssertionState clientAssertionState = context.getState(ClientAssertionState.class, ClientAssertionState.supplier());
        AlternativeLookupProvider lookupProvider = context.getSession().getProvider(AlternativeLookupProvider.class);

        String federatedClientId =  clientAssertionState.getToken().getSubject();

        ClientModel client = lookupProvider.lookupClientFromClientAttributes(
                context.getSession(),
                Map.of(FederatedJWTClientAuthenticator.JWT_CREDENTIAL_SUBJECT_KEY, federatedClientId));
        if (client == null) {
            return null;
        }

        IdentityProviderModel identityProvider = context.getSession().identityProviders().getByAlias(
                client.getAttribute(FederatedJWTClientAuthenticator.JWT_CREDENTIAL_ISSUER_KEY));

        return new ClientAssertionIdentityProviderFactory.LookupResult(client, identityProvider);
    }

}
