package org.keycloak.protocol.oid4vc.issuance;

import java.util.List;
import java.util.Optional;

import jakarta.ws.rs.core.Response;

import org.keycloak.OAuthErrorException;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.protocol.oid4vc.clientpolicy.PredicateCredentialClientPolicy;
import org.keycloak.protocol.oid4vc.issuance.credentialoffer.CredentialOfferState;
import org.keycloak.protocol.oid4vc.issuance.credentialoffer.CredentialOfferStorage;
import org.keycloak.protocol.oid4vc.model.CredentialScopeRepresentation;
import org.keycloak.protocol.oid4vc.model.CredentialsOffer;
import org.keycloak.protocol.oid4vc.model.IssuerState;
import org.keycloak.protocol.oid4vc.utils.CredentialScopeUtils;
import org.keycloak.protocol.oidc.endpoints.AuthorizationEndpointCheckProvider;
import org.keycloak.protocol.oidc.endpoints.AuthorizationEndpointChecker;
import org.keycloak.protocol.oidc.endpoints.AuthorizationEndpointChecker.AuthorizationCheckException;
import org.keycloak.protocol.oidc.endpoints.request.AuthorizationEndpointRequest;

import static org.keycloak.OAuth2Constants.ISSUER_STATE;
import static org.keycloak.protocol.oid4vc.clientpolicy.CredentialClientPolicies.VC_POLICY_CREDENTIAL_OFFER_REQUIRED;

/**
 * OID4VCI 授权端点检查提供方：验证授权请求中的凭证范围是否与凭证发放（offer）一致。
 * <p>当客户端策略要求凭证发放时，确保请求的 {@code credential_configuration_id} 已在 offer 中声明。</p>
 */
public class OID4VCAuthorizationCheckProvider implements AuthorizationEndpointCheckProvider {

    private final KeycloakSession session;

    /** @param session Keycloak 会话 */
    public OID4VCAuthorizationCheckProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void check(AuthorizationEndpointChecker context) throws AuthorizationCheckException {
        ClientModel client = context.getClient();
        AuthorizationEndpointRequest request = context.getAuthorizationEndpointRequest();

        // 获取与此客户端关联、且在授权请求中请求的凭证范围列表
        List<CredentialScopeModel> credScopes = CredentialScopeUtils.getCredentialScopesForAuthorization(client, request);

        // 存在凭证范围请求时才执行后续校验
        if (!credScopes.isEmpty()) {

            PredicateCredentialClientPolicy offerRequiredPolicy = VC_POLICY_CREDENTIAL_OFFER_REQUIRED;

            // 从 issuer_state 解析潜在的凭证发放状态
            String issuerStateParam = request.getAdditionalReqParams().get(ISSUER_STATE);
            CredentialOfferStorage offerStorage = session.getProvider(CredentialOfferStorage.class);
            CredentialOfferState offerState = Optional.ofNullable(issuerStateParam)
                    .map(IssuerState::fromEncodedString)
                    .map(IssuerState::getCredentialsOfferId)
                    .map(offerStorage::getOfferStateById)
                    .orElse(null);

            List<String> offeredConfigurationIds = Optional.ofNullable(offerState)
                    .map(CredentialOfferState::getCredentialsOffer)
                    .map(CredentialsOffer::getCredentialConfigurationIds)
                    .orElse(List.of());

            // 校验每个请求的 credential_configuration_id 是否已在发放中提供
            for (CredentialScopeModel credScope : credScopes) {
                String credConfigId = credScope.getCredentialConfigurationId();

                boolean requiredByScope = offerRequiredPolicy.validate(new CredentialScopeRepresentation(credScope));
                if (requiredByScope && !offeredConfigurationIds.contains(credConfigId)) {
                    String errorDetail = "Authorization request rejected by policy " + offerRequiredPolicy.getName() + " for scope: " + credScope.getName();
                    throw new AuthorizationCheckException(Response.Status.BAD_REQUEST, OAuthErrorException.INVALID_REQUEST, errorDetail);
                }
            }
        }
    }

    @Override
    public void close() {
    }
}
