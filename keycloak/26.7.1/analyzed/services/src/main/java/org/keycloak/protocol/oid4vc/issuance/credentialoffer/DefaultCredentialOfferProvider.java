/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.oid4vc.issuance.credentialoffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.keycloak.common.Profile;
import org.keycloak.events.Errors;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.protocol.oid4vc.issuance.CredentialOfferException;
import org.keycloak.protocol.oid4vc.issuance.OID4VCAuthorizationDetailsProcessor;
import org.keycloak.protocol.oid4vc.issuance.OID4VCIssuerWellKnownProvider;
import org.keycloak.protocol.oid4vc.issuance.credentialoffer.preauth.PreAuthCodeHandler;
import org.keycloak.protocol.oid4vc.model.AuthorizationCodeGrant;
import org.keycloak.protocol.oid4vc.model.CredentialsOffer;
import org.keycloak.protocol.oid4vc.model.IssuerState;
import org.keycloak.protocol.oid4vc.model.OID4VCAuthorizationDetail;
import org.keycloak.protocol.oid4vc.model.PreAuthCodeCtx;
import org.keycloak.protocol.oid4vc.model.PreAuthorizedCodeGrant;
import org.keycloak.protocol.oid4vc.utils.OID4VCUtil;
import org.keycloak.util.Strings;

import static org.keycloak.OID4VCConstants.OID4VCI_ENABLED_ATTRIBUTE_KEY;
import static org.keycloak.constants.OID4VCIConstants.CREDENTIAL_OFFER_CREATE;
import static org.keycloak.protocol.oid4vc.model.PreAuthorizedCodeGrant.PRE_AUTH_GRANT_TYPE;
import static org.keycloak.protocol.oid4vc.utils.CredentialScopeUtils.findCredentialScopeModelByConfigurationId;

/**
 * {@link CredentialOfferProvider} 的默认实现。
 * <p>校验 grant 类型、目标用户/客户端与凭证配置，构建 {@link CredentialOfferState} 并填充
 * authorization_code 或 pre-authorized_code grant。</p>
 *
 * @author <a href="mailto:tdiesler@ibm.com">Thomas Diesler</a>
 */
class DefaultCredentialOfferProvider implements CredentialOfferProvider {

    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;

    /** @param session Keycloak 会话 */
    DefaultCredentialOfferProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public CredentialOfferState createCredentialOffer(
            UserModel user,
            String grantType,
            List<String> credentialConfigurationIds,
            String targetClientId,
            String targetUsername,
            Integer expireAt) {

        // 检查是否启用 --feature=oid4vc-vci-preauth-code
        //
        boolean preAuthorized = PRE_AUTH_GRANT_TYPE.equals(grantType);
        if (preAuthorized && !Profile.isFeatureEnabled(Profile.Feature.OID4VC_VCI_PREAUTH_CODE)) {
            throw new CredentialOfferException(Errors.INVALID_REQUEST,
                    "OID4VCI pre-authorized code grant offers not enabled. Requires --feature=oid4vc-vci-preauth-code");
        }

        // 至少需要一个 credential_configuration_id
        //
        if (credentialConfigurationIds == null || credentialConfigurationIds.isEmpty()) {
            throw new CredentialOfferException(Errors.INVALID_REQUEST, "No credentialConfigurationIds");
        }

        RealmModel realmModel = this.session.getContext().getRealm();

        // 校验目标用户
        //
        UserModel targetUser = Optional.ofNullable(targetUsername)
                .map(tu -> validateTargetUser(session, realmModel, user, tu))
                .orElse(null);
        String targetUserId = targetUser == null ? null : targetUser.getId();

        // 校验目标客户端
        if (targetClientId != null) {
            validateTargetClient(realmModel, targetClientId);
        }

        // 构建 CredentialsOffer 模型
        //
        CredentialsOffer credOffer = new CredentialsOffer()
                .setCredentialIssuer(OID4VCIssuerWellKnownProvider.getIssuer(session.getContext()))
                .setCredentialConfigurationIds(credentialConfigurationIds);

        // 创建 CredentialOfferState 并生成 authorization_details
        //
        CredentialOfferState offerState = new CredentialOfferState(credOffer, targetClientId, targetUserId, expireAt, credOffersId -> {
            List<OID4VCAuthorizationDetail> authDetails = new ArrayList<>();
            for (String credConfigId : credentialConfigurationIds) {
                CredentialScopeModel credScope = findCredentialScopeModelByConfigurationId(
                        realmModel, () -> session.clientScopes().getClientScopesStream(realmModel), credConfigId);
                if (credScope == null) {
                    throw new CredentialOfferException(Errors.INVALID_REQUEST, "No credential scope model for: " + credConfigId);
                }
                if (targetUser != null && !OID4VCUtil.hasVerifiableCredential(session, targetUser, credScope)) {
                    throw new CredentialOfferException(Errors.INVALID_REQUEST, "User '" + targetUser.getUsername() + "' does not have verifiable credential '" + credConfigId + "'.");
                }

                OID4VCAuthorizationDetailsProcessor authDetailsProcessor = new OID4VCAuthorizationDetailsProcessor(session);
                authDetails.add(authDetailsProcessor.generateResponseAuthorizationDetails(credScope, credOffersId));
            }
            return authDetails;
        });

        if (preAuthorized) {
            String code = createPreAuthorizedCode(offerState);
            credOffer.addGrant(new PreAuthorizedCodeGrant().setPreAuthorizedCode(code));
        } else {
            IssuerState issuerState = new IssuerState().setCredentialsOfferId(offerState.getCredentialsOfferId());
            credOffer.addGrant(new AuthorizationCodeGrant().setIssuerState(issuerState.encodeToString()));
        }

        return offerState;
    }

    // 私有 ---------------------------------------------------------------------------------------------------------

    private UserModel validateTargetUser(KeycloakSession session, RealmModel realmModel, UserModel loginUserModel, String targetUser) {
        UserModel targetUserModel = session.users().getUserByUsername(realmModel, targetUser);
        if (targetUserModel == null) {
            throw new CredentialOfferException(Errors.USER_NOT_FOUND, "User not found: " + targetUser);
        }

        // 确认目标用户已启用
        //
        if (!targetUserModel.isEnabled()) {
            throw new CredentialOfferException(Errors.USER_DISABLED, "User '" + targetUser + "' disabled");
        }

        // 当 loginUser != targetUser 时，签发用户须持有 credential_offer_create 角色
        // 自签发 Credential Offer 不需要该角色
        //
        //   - 定向或匿名的 authorization_code grant
        //   - 定向的 pre-authorized_code grant
        //
        if (Strings.isEmpty(targetUser) || !loginUserModel.getUsername().equals(targetUser)) {
            boolean hasCredentialOfferRole = loginUserModel.getRoleMappingsStream()
                    .anyMatch(rm -> rm.getName().equals(CREDENTIAL_OFFER_CREATE.getName()));
            if (!hasCredentialOfferRole) {
                throw new CredentialOfferException(Errors.NOT_ALLOWED, "Credential offer creation requires role: " + CREDENTIAL_OFFER_CREATE.getName());
            }
        }

        return targetUserModel;
    }

    private void validateTargetClient(RealmModel realm, String clientId) {
        ClientModel client = session.clients().getClientByClientId(realm, clientId);
        if (client == null) {
            throw new CredentialOfferException(Errors.CLIENT_NOT_FOUND, "Client '" + clientId + "' not found");
        }
        if (!client.isEnabled()) {
            throw new CredentialOfferException(Errors.CLIENT_DISABLED, "Client '" + clientId + "' disabled");
        }
        boolean oid4vciEnabled = Boolean.parseBoolean(client.getAttributes().get(OID4VCI_ENABLED_ATTRIBUTE_KEY));
        if (!oid4vciEnabled) {
            throw new CredentialOfferException(Errors.INVALID_CLIENT, "Client '" + clientId + "' is not enabled for OID4VCI features.");
        }
    }

    /** 为凭证发放状态创建预授权码。 */
    private String createPreAuthorizedCode(CredentialOfferState offerState) {
        PreAuthCodeHandler preAuthCodeHandler = session.getProvider(PreAuthCodeHandler.class);
        if (preAuthCodeHandler == null) {
            throw new IllegalStateException("No PreAuthCodeHandler provider available");
        }

        // PreAuthCodeCtx 防止敏感信息（如 tx_code）泄露进预授权码
        // 例如交易码绝不能嵌入预授权 JWT
        PreAuthCodeCtx ctx = new PreAuthCodeCtx(offerState);

        return preAuthCodeHandler.createPreAuthCode(ctx);
    }
}
