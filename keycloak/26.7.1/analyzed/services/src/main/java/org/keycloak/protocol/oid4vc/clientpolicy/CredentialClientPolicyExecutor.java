package org.keycloak.protocol.oid4vc.clientpolicy;

import java.util.List;
import java.util.Optional;

import org.keycloak.OAuthErrorException;
import org.keycloak.events.Errors;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.protocol.oid4vc.issuance.credentialoffer.CredentialOfferState;
import org.keycloak.protocol.oid4vc.issuance.credentialoffer.CredentialOfferStorage;
import org.keycloak.protocol.oid4vc.model.CredentialsOffer;
import org.keycloak.protocol.oid4vc.model.IssuerState;
import org.keycloak.protocol.oid4vc.utils.CredentialScopeUtils;
import org.keycloak.protocol.oidc.endpoints.request.AuthorizationEndpointRequest;
import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.AuthorizationRequestContext;
import org.keycloak.services.clientpolicy.executor.ClientPolicyExecutorProvider;

import static org.keycloak.OAuth2Constants.ISSUER_STATE;
import static org.keycloak.protocol.oid4vc.clientpolicy.CredentialClientPolicies.VC_POLICY_CREDENTIAL_OFFER_REQUIRED;
import static org.keycloak.services.clientpolicy.ClientPolicyEvent.AUTHORIZATION_REQUEST;

/**
 * OID4VCI 凭证客户端策略执行器：在授权请求阶段校验 Credential Offer 约束。
 * <p>可在客户端 Profile 中引用（示例 JSON 如下，默认 Profile 尚未内置）：</p>
 * <pre>
 * {
 *   "name": "oid4vci-client-profile",
 *   "description": "Client profile, which enforces various policies on oid4vci clients.",
 *   "executors": [
 *     { "executor": "oid4vci-policy-executor", "configuration": {} }
 *   ]
 * }
 * </pre>
 */
public class CredentialClientPolicyExecutor implements ClientPolicyExecutorProvider<ClientPolicyExecutorConfigurationRepresentation> {

    protected final KeycloakSession session;

    /** @param session Keycloak 会话 */
    public CredentialClientPolicyExecutor(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public String getProviderId() {
        return CredentialClientPolicyExecutorFactory.PROVIDER_ID;
    }

    @Override
    public void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
        if (AUTHORIZATION_REQUEST.equals(context.getEvent())) {
            AuthorizationRequestContext authRequestContext = (AuthorizationRequestContext) context;
            checkCredentialPolicies(authRequestContext);
        }
    }

    private void checkCredentialPolicies(AuthorizationRequestContext context) throws ClientPolicyException {

        ClientModel client = context.getClient();
        if (client == null)
            throw new ClientPolicyException(OAuthErrorException.INVALID_CLIENT, "No issuing client");

        // 获取与本客户端关联且被请求的凭证 Scope 列表
        AuthorizationEndpointRequest request = context.getAuthorizationEndpointRequest();
        List<CredentialScopeModel> credScopes = CredentialScopeUtils.getCredentialScopesForAuthorization(client, request);

        // 存在凭证 Scope 请求时才执行策略检查
        if (!credScopes.isEmpty()) {

            PredicateCredentialClientPolicy offerRequiredPolicy = VC_POLICY_CREDENTIAL_OFFER_REQUIRED;

            // 从 issuer_state 解析 Credential Offer 状态
            String issuerStateParam = request.getAdditionalReqParams().get(ISSUER_STATE);
            CredentialOfferStorage offerStorage = session.getProvider(CredentialOfferStorage.class);
            CredentialOfferState offerState = Optional.ofNullable(issuerStateParam)
                    .map(IssuerState::fromEncodedString)
                    .map(IssuerState::getCredentialsOfferId)
                    .map(offerStorage::getOfferStateById)
                    .orElse(null);

            // 读取 Offer 中包含的 credential_configuration_id 列表
            List<String> offeredConfigurationIds = Optional.ofNullable(offerState)
                    .map(CredentialOfferState::getCredentialsOffer)
                    .map(CredentialsOffer::getCredentialConfigurationIds)
                    .orElse(List.of());

            // 校验每个请求的 configuration id 是否已在 Offer 中提供
            for (CredentialScopeModel credScope : credScopes) {
                String credConfigId = credScope.getCredentialConfigurationId();
                if (!offeredConfigurationIds.contains(credConfigId)) {
                    String errorDetail = "Authorization request rejected by policy " + offerRequiredPolicy.getName() + " for client: " + client.getClientId();
                    throw new ClientPolicyException(Errors.NOT_ALLOWED, errorDetail);
                }
            }
        }
    }
}
