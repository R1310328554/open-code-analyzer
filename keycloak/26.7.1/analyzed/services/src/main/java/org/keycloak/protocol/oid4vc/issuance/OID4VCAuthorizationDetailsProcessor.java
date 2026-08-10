/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.protocol.oid4vc.issuance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.common.Profile;
import org.keycloak.common.util.Time;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.Constants;
import org.keycloak.models.IssuedVerifiableCredentialModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.UserVerifiableCredentialModel;
import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.protocol.oid4vc.issuance.credentialoffer.CredentialOfferState;
import org.keycloak.protocol.oid4vc.issuance.credentialoffer.CredentialOfferStorage;
import org.keycloak.protocol.oid4vc.model.Claim;
import org.keycloak.protocol.oid4vc.model.ClaimsDescription;
import org.keycloak.protocol.oid4vc.model.CredentialIssuer;
import org.keycloak.protocol.oid4vc.model.IssuerState;
import org.keycloak.protocol.oid4vc.model.OID4VCAuthorizationDetail;
import org.keycloak.protocol.oid4vc.model.SupportedCredentialConfiguration;
import org.keycloak.protocol.oid4vc.utils.ClaimsPathPointer;
import org.keycloak.protocol.oid4vc.utils.OID4VCUtil;
import org.keycloak.protocol.oidc.rar.AuthorizationDetailsProcessor;
import org.keycloak.protocol.oidc.rar.InvalidAuthorizationDetailsException;
import org.keycloak.representations.AuthorizationDetailsJSONRepresentation;
import org.keycloak.util.JsonSerialization;
import org.keycloak.util.Strings;

import org.jboss.logging.Logger;

import static org.keycloak.OAuth2Constants.ISSUER_STATE;
import static org.keycloak.OID4VCConstants.OPENID_CREDENTIAL;
import static org.keycloak.models.oid4vci.CredentialScopeModel.VC_CONFIGURATION_ID;
import static org.keycloak.protocol.oid4vc.issuance.OID4VCIssuerEndpoint.CREDENTIALS_OFFER_ID_ATTR;
import static org.keycloak.protocol.oid4vc.model.OID4VCAuthorizationDetail.ISSUED_CREDENTIAL_ID;
import static org.keycloak.protocol.oid4vc.model.PreAuthorizedCodeGrant.PRE_AUTH_GRANT_TYPE;
import static org.keycloak.protocol.oid4vc.utils.CredentialScopeUtils.findCredentialScopeModelByConfigurationId;
import static org.keycloak.protocol.oid4vc.utils.CredentialScopeUtils.findCredentialScopeModelByName;
import static org.keycloak.protocol.oidc.endpoints.AuthorizationEndpoint.LOGIN_SESSION_NOTE_ADDITIONAL_REQ_PARAMS_PREFIX;

/**
 * OID4VCI {@code openid_credential} 授权细节处理器：校验、构建令牌响应中的 authorization_details。
 * <p>覆盖授权请求、访问令牌请求及缺失授权细节时的 scope 回退逻辑。</p>
 */
public class OID4VCAuthorizationDetailsProcessor implements AuthorizationDetailsProcessor<OID4VCAuthorizationDetail> {
    private static final Logger logger = Logger.getLogger(OID4VCAuthorizationDetailsProcessor.class);

    private final KeycloakSession session;

    /** @param session Keycloak 会话 */
    public OID4VCAuthorizationDetailsProcessor(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public boolean isSupported() {
        return session.getContext().getRealm().isVerifiableCredentialsEnabled();
    }

    @Override
    public String getSupportedType() {
        return OPENID_CREDENTIAL;
    }

    @Override
    public Class<OID4VCAuthorizationDetail> getSupportedResponseJavaType() {
        return OID4VCAuthorizationDetail.class;
    }

    @Override
    public OID4VCAuthorizationDetail process(UserSessionModel userSession, ClientSessionContext clientSessionCtx, AuthorizationDetailsJSONRepresentation authzDetail) {
        OID4VCAuthorizationDetail requestAuthDetail = authzDetail.asSubtype(OID4VCAuthorizationDetail.class);
        validateAuthorizationDetail(requestAuthDetail);
        return buildAuthorizationDetailResponse(clientSessionCtx, requestAuthDetail);
    }

    @Override
    public OID4VCAuthorizationDetail validateAuthorizationDetail(AuthorizationDetailsJSONRepresentation authzDetail) throws InvalidAuthorizationDetailsException {

        OID4VCAuthorizationDetail requestAuthDetail = authzDetail.asSubtype(OID4VCAuthorizationDetail.class);

        CredentialIssuer issuerMetadata = new OID4VCIssuerWellKnownProvider(session).getIssuerMetadata();
        Map<String, SupportedCredentialConfiguration> supportedCredentials = issuerMetadata.getCredentialsSupported();
        List<String> authorizationServers = issuerMetadata.getAuthorizationServers();
        String issuerIdentifier = issuerMetadata.getCredentialIssuer();

        String type = requestAuthDetail.getType();
        String credentialConfigurationId = requestAuthDetail.getCredentialConfigurationId();
        List<String> credentialIdentifiers = requestAuthDetail.getCredentialIdentifiers();
        List<ClaimsDescription> claims = requestAuthDetail.getClaims();

        // 首先校验 type 字段
        if (!OPENID_CREDENTIAL.equals(type)) {
            logger.warnf("Invalid authorization_details type: %s", type);
            throw getInvalidRequestException("type: " + type + ", expected=" + OPENID_CREDENTIAL);
        }

        // 若元数据声明 authorization_servers，locations 必须等于签发者标识符
        if (authorizationServers != null && !authorizationServers.isEmpty()) {
            List<String> locations = requestAuthDetail.getLocations();
            if (locations == null || locations.size() != 1 || !issuerIdentifier.equals(locations.get(0))) {
                logger.warnf("Invalid locations field in authorization_details: %s, expected: %s", locations, issuerIdentifier);
                throw getInvalidRequestException("locations=" + locations + ", expected=" + issuerIdentifier);
            }
        }

        // credential_configuration_id 为必填
        if (Strings.isEmpty(credentialConfigurationId)) {
            logger.warnf("Missing credential_configuration_id in authorization_details");
            throw getInvalidRequestException("credential_configuration_id is required");
        }

        // 授权请求中不允许 credential_identifiers
        if (credentialIdentifiers != null) {
            // we also reject an empty array of credential identifiers
            logger.warnf("Property credential_identifiers not allowed in authorization_details");
            throw getInvalidRequestException("credential_identifiers not allowed");
        }

        // 授权请求中不允许 issued_credential_id
        if (requestAuthDetail.getIssuedCredentialId() != null) {
            logger.warnf("Property '%s' not allowed in authorization_details", ISSUED_CREDENTIAL_ID);
            throw getInvalidRequestException("Issued credential ID not allowed in authorization details");
        }

        // 校验 credential_configuration_id 是否在签发者元数据中支持
        SupportedCredentialConfiguration credConfig = supportedCredentials.get(credentialConfigurationId);
        if (credConfig == null) {
            logger.warnf("Unsupported credential_configuration_id: %s", credentialConfigurationId);
            throw getInvalidRequestException("Invalid credential configuration: unsupported credential_configuration_id: " + credentialConfigurationId);
        }

        // 若请求声明 claims，执行语义校验
        if (claims != null && !claims.isEmpty()) {
            validateClaims(claims, credConfig);
        }

        return requestAuthDetail;
    }

    @Override
    public OID4VCAuthorizationDetail sanitizeBeforeSendingTokenResponse(OID4VCAuthorizationDetail authzDetail) {
        // 发送令牌响应前移除非标准属性（issued_credential_id、credentials_offer_id）
        // https://github.com/keycloak/keycloak/pull/49958
        OID4VCAuthorizationDetail cloned = authzDetail.clone();
        cloned.setIssuedCredentialId(null);
        cloned.setCredentialsOfferId(null);
        return cloned;
    }

    // 私有辅助方法 ---------------------------------------------------------------------------------------------------

    private InvalidAuthorizationDetailsException getInvalidRequestException(String errorDescription) {
        return new InvalidAuthorizationDetailsException("Invalid authorization_details: " + errorDescription);
    }

    /**
     * 校验请求的 claims 是否被凭证配置支持（语义级校验）。
     *
     * @param claims 待校验的声明描述列表
     * @param config 凭证配置元数据
     */
    private void validateClaims(List<ClaimsDescription> claims, SupportedCredentialConfiguration config) {

        // 从凭证元数据获取对外暴露的 claims
        List<Claim> exposedClaims = null;
        if (config.getCredentialMetadata() != null && config.getCredentialMetadata().getClaims() != null && !config.getCredentialMetadata().getClaims().isEmpty()) {
            exposedClaims = config.getCredentialMetadata().getClaims();
        }

        if (exposedClaims == null || exposedClaims.isEmpty()) {
            throw getInvalidRequestException("Credential configuration does not expose any claims metadata");
        }

        // 将暴露 claims 转为路径集合便于比对
        Set<String> exposedClaimPaths = exposedClaims.stream()
                .filter(claim -> claim.getPath() != null && !claim.getPath().isEmpty())
                .map(claim -> claim.getPath().toString())
                .collect(Collectors.toSet());

        // 逐条校验请求 claim 是否在元数据中声明
        for (ClaimsDescription requestedClaim : claims) {
            if (requestedClaim.getPath() == null || requestedClaim.getPath().isEmpty()) {
                throw getInvalidRequestException("Invalid claims description: path is required");
            }

            // 按 OID4VCI 规范校验 claims 路径指针格式
            if (!ClaimsPathPointer.isValidPath(requestedClaim.getPath())) {
                throw getInvalidRequestException("Invalid claims path pointer: " + requestedClaim.getPath() +
                        ". Path must contain only strings, non-negative integers, and null values.");
            }

            String requestedPath = requestedClaim.getPath().toString();

            // 确认请求路径存在于元数据暴露集合中
            if (!exposedClaimPaths.contains(requestedPath)) {
                throw getInvalidRequestException("Unsupported claim: " + requestedPath +
                        ". This claim is not supported by the credential configuration.");
            }
        }

        // 使用 ClaimsPathPointer 检测冲突或矛盾声明
        if (!ClaimsPathPointer.validateClaimsDescriptions(claims)) {
            throw getInvalidRequestException("Invalid claims descriptions: conflicting or contradictory claims found");
        }
    }

    private OID4VCAuthorizationDetail buildAuthorizationDetailResponse(ClientSessionContext clientSessionCtx, OID4VCAuthorizationDetail requestAuthDetail) {

        String requestedCredentialConfigurationId = requestAuthDetail.getCredentialConfigurationId();
        if (requestedCredentialConfigurationId == null) {
            throw getInvalidRequestException("No credential_configuration_id in access token request.");
        }

        // 处理带凭证发放的访问令牌请求（预授权或授权码）
        // Should work for pre-auth and auth-code grants
        //
        CredentialOfferState offerState = getCredentialOfferState(clientSessionCtx);
        if (offerState != null) {
            OID4VCAuthorizationDetail offeredAuthDetail = offerState.getAuthorizationDetails(requestedCredentialConfigurationId);
            if (offeredAuthDetail == null) {
                throw getInvalidRequestException("Unauthorized credential_configuration_id: " + requestedCredentialConfigurationId);
            }
            OID4VCAuthorizationDetail responseAuthDetail = offeredAuthDetail.clone();
            responseAuthDetail.setClaims(requestAuthDetail.getClaims());
            return responseAuthDetail;
        }

        // 处理无凭证发放的访问令牌请求
        //
        RealmModel realmModel = clientSessionCtx.getClientSession().getRealm();
        CredentialScopeModel credScope = findCredentialScopeModelByConfigurationId(realmModel, clientSessionCtx::getClientScopesStream, requestedCredentialConfigurationId);
        if (credScope == null)
            throw getInvalidRequestException("Cannot find or access client scope for credential_configuration_id: " + requestedCredentialConfigurationId);

        UserModel user = clientSessionCtx.getClientSession().getUserSession().getUser();
        if (!OID4VCUtil.hasVerifiableCredential(session, user, credScope)) {
            throw getInvalidRequestException("User '" + user.getUsername() + "' does not have verifiable credential '" + credScope.getCredentialConfigurationId() + "'.");
        }

        OID4VCAuthorizationDetail responseAuthDetail = generateResponseAuthorizationDetails(credScope, null);
        responseAuthDetail.setClaims(requestAuthDetail.getClaims());

        return responseAuthDetail;
    }

    @Override
    public List<OID4VCAuthorizationDetail> handleMissingAuthorizationDetails(UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        RealmModel realmModel = userSession.getRealm();

        // 带凭证发放的访问令牌请求
        // Works for pre-auth and auth-code grants
        CredentialOfferState offerState = getCredentialOfferState(clientSessionCtx);
        if (offerState != null) {
            return offerState.getAuthorizationDetails();
        }

        // 无发放且无 authorization_details 时，按 scope 参数生成
        // This is likely a "scope only" request
        String scopeParam = clientSessionCtx.getScopeString();
        if (scopeParam == null) {
            throw getInvalidRequestException("No 'scope' parameter in client session");
        }

        List<OID4VCAuthorizationDetail> authorizationDetails = new ArrayList<>();
        for (String scope : scopeParam.split(" ")) {
            CredentialScopeModel credScope = findCredentialScopeModelByName(realmModel, clientSessionCtx::getClientScopesStream, scope);
            if (credScope != null) {
                if (!OID4VCUtil.hasVerifiableCredential(session, userSession.getUser(), credScope)) {
                    throw getInvalidRequestException("User '" + userSession.getUser().getUsername() + "' does not have verifiable credential '" + credScope.getCredentialConfigurationId() + "'.");
                }

                // 为访问令牌响应生成 authorization_details（与创建 offer 时逻辑一致）
                // This is the same logic as we use when a credential offer is created
                //
                OID4VCAuthorizationDetail authDetail = generateResponseAuthorizationDetails(credScope, null);
                authorizationDetails.add(authDetail);
            }
        }

        if (!authorizationDetails.isEmpty()) {
            logger.debugf("Generated authorization_details: %s", JsonSerialization.valueAsString(authorizationDetails));
        } else {
            logger.debug("No generated authorization_details");
        }
        return authorizationDetails;
    }

    @Override
    public OID4VCAuthorizationDetail processStoredAuthorizationDetails(UserSessionModel userSession, ClientSessionContext clientSessionCtx, AuthorizationDetailsJSONRepresentation storedAuthDetails)
            throws InvalidAuthorizationDetailsException {
        if (storedAuthDetails == null) {
            return null;
        }

        logger.debugf("Processing stored authorization_details from authorization request: %s", storedAuthDetails);

        try {
            return process(userSession, clientSessionCtx, storedAuthDetails);
        } catch (InvalidAuthorizationDetailsException e) {
            // 按 OID4VCI 规范：授权请求使用了 authorization_details 则必须在令牌响应中返回
            // it is required to be returned in token response. If it cannot be processed, return invalid_request error
            throw new InvalidAuthorizationDetailsException("authorization_details was used in authorization request but cannot be processed for token response: " + e.getMessage());
        }
    }

    @Override
    public void afterAuthorizationDetailsProcessed(UserSessionModel userSession, ClientSessionContext clientSessionCtx, OID4VCAuthorizationDetail oid4vcAuthzDetailResponse) {
        String credentialConfigId = oid4vcAuthzDetailResponse.getCredentialConfigurationId();
        CredentialScopeModel credentialScope = findCredentialScopeModelByConfigurationId(session.getContext().getRealm(), clientSessionCtx::getClientScopesStream, credentialConfigId);

        if (credentialScope == null && Profile.isFeatureEnabled(Profile.Feature.OID4VC_VCI_PREAUTH_CODE) && PRE_AUTH_GRANT_TYPE.equals(clientSessionCtx.getAttribute(Constants.GRANT_TYPE, String.class))) {
            // For now, for pre-authorized grant we allow fallback to all client scopes of the realm, but this needs to be double-check before pre-authorization grant
            // is going to be supported feature. Details: https://github.com/keycloak/keycloak/issues/49965
            credentialScope = findCredentialScopeModelByConfigurationId(session.getContext().getRealm(), session.getContext().getRealm()::getClientScopesStream, credentialConfigId);
        }

        if (credentialScope == null) {
            throw new InvalidAuthorizationDetailsException("Cannot find credential scope for credential configuration ID: " + credentialConfigId);
        }

        // 创建已签发凭证记录并在 authorization_details 中写入其 ID
        IssuedVerifiableCredentialModel issuedCredential = createIssuedVerifiableCredential(userSession.getUser(), clientSessionCtx.getClientSession().getClient(), credentialScope);
        oid4vcAuthzDetailResponse.setIssuedCredentialId(issuedCredential.getId());
    }

    @Override
    public void close() {
        // No cleanup needed
    }

    public OID4VCAuthorizationDetail generateResponseAuthorizationDetails(CredentialScopeModel credScope, String credOffersId) {

        OID4VCAuthorizationDetail authDetail = new OID4VCAuthorizationDetail();
        authDetail.setCredentialsOfferId(credOffersId);
        authDetail.setType(OPENID_CREDENTIAL);

        String credConfigId = Optional.ofNullable(credScope.getCredentialConfigurationId())
                .orElseThrow(() -> new IllegalStateException("No " + VC_CONFIGURATION_ID + " in client scope: " + credScope.getName()));

        authDetail.setCredentialConfigurationId(credConfigId);

        // 访问令牌响应在以下情况应包含 authorization_details：
        //
        //  * provided in Authorization Request
        //  * provided in AccessToken Request
        //  * defined credential identifiers
        //
        // https://gitlab.com/openid/conformance-suite/-/work_items/1724

        String credIdentifier = credScope.getCredentialIdentifier();
        if (Strings.isEmpty(credIdentifier)) {
            credIdentifier = credConfigId + "_0000";
        }
        authDetail.setCredentialIdentifiers(List.of(credIdentifier));

        return authDetail;
    }

    protected IssuedVerifiableCredentialModel createIssuedVerifiableCredential(UserModel userModel, ClientModel clientModel, CredentialScopeModel credentialScope) {
        String credentialScopeName = credentialScope.getName();
        try {
            // 按客户端范围 ID 查找用户可验证凭证以获取其标识
            UserVerifiableCredentialModel verifiableCredential = session.users()
                    .getVerifiableCredentialByClientScope(userModel.getId(), credentialScope.getId());
            if (verifiableCredential == null) {
                throw new ModelException("User verifiable credential not found for scope: " + credentialScopeName);
            }

            IssuedVerifiableCredentialModel model = new IssuedVerifiableCredentialModel(userModel.getId(), verifiableCredential.getId(), clientModel.getId());

            long issuedAt = Time.currentTimeMillis();
            model.setIssuedAt(issuedAt);
            model.setExpiresAt(issuedAt + (credentialScope.getExpiryInSeconds() * 1000L));

            logger.debugf("Created VC issuance: user=%s, client=%s, type=%s", userModel.getUsername(), clientModel.getClientId(), credentialScopeName);

            return session.users().addIssuedVerifiableCredential(model);
        } catch (Exception e) {
            throw new ModelException(String.format("Failed to create VC issuance for user=%s, client=%s, type=%s", userModel.getUsername(), clientModel.getClientId(), credentialScopeName), e);
        }
    }

    // Private ---------------------------------------------------------------------------------------------------------

    private CredentialOfferState getCredentialOfferState(ClientSessionContext clientSessionCtx) {

        CredentialOfferState offerState = null;

        // 检查是否存在凭证发放（预授权流程）
        //
        String credOfferId = clientSessionCtx.getAttribute(CREDENTIALS_OFFER_ID_ATTR, String.class);

        // 检查 issuer_state（授权码流程）
        //
        String issuerStateNote = clientSessionCtx.getClientSession().getNote(LOGIN_SESSION_NOTE_ADDITIONAL_REQ_PARAMS_PREFIX + ISSUER_STATE);
        if (credOfferId == null && issuerStateNote != null) {
            IssuerState issuerState = IssuerState.fromEncodedString(issuerStateNote);
            credOfferId = issuerState.getCredentialsOfferId();
        }

        if (credOfferId != null) {
            String auxCredOfferId = credOfferId;
            CredentialOfferStorage offerStorage = session.getProvider(CredentialOfferStorage.class);
            offerState = Optional.ofNullable(offerStorage.getOfferStateById(credOfferId))
                    .orElseThrow(() -> new IllegalStateException("No credential offer state for: " + auxCredOfferId));

            // 校验登录客户端与发放目标客户端一致（针对特定客户端的发放）
            String offerClientId = offerState.getTargetClientId();
            String loginClientId = clientSessionCtx.getClientSession().getClient().getClientId();
            if (offerClientId != null && !offerClientId.equals(loginClientId)) {
                throw new IllegalStateException("Credential offer target client '" + offerClientId + "' different from login client '" + loginClientId + "'");
            }
        }

        return offerState;
    }
}
