/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.authentication.requiredactions.util;

import java.util.Map;
import java.util.function.Supplier;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import org.keycloak.authentication.AuthenticatorUtil;
import org.keycloak.authentication.authenticators.util.LoAUtil;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialTypeMetadata;
import org.keycloak.credential.CredentialTypeMetadataContext;
import org.keycloak.models.AuthenticationFlowBindings;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.AuthenticationFlowResolver;

import org.jboss.logging.Logger;

import static org.keycloak.models.Constants.NO_LOA;

/**
 * 凭证删除辅助类：校验凭证可删除性及 LoA 是否满足阶梯认证要求。
 * <p>例如删除双因素凭证需以对应 LoA 级别认证。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CredentialDeleteHelper {

    private static final Logger logger = Logger.getLogger(CredentialDeleteHelper.class);

    /**
     * 删除指定用户的凭证并执行必要校验。
     * 启用阶梯认证时，须以对应 LoA 级别认证方可删除（如删除双因素凭证需双因素认证）。
     *
     * @param session Keycloak 会话
     * @param user 用户模型
     * @param credentialId 凭证 ID
     * @param currentLoAProvider 当前已认证 LoA 级别供应器
     * @return 已删除凭证；联邦占位 ID 或未找到时可能为 null
     */
    /** 删除凭证入口：处理存储凭证、联邦凭证与 legacy 占位 ID。 */
    public static CredentialModel removeCredential(KeycloakSession session, UserModel user, String credentialId, Supplier<Integer> currentLoAProvider) {
        CredentialModel credential = user.credentialManager().getStoredCredentialById(credentialId);
        if (credential == null) {
            if (user.isFederated()) {
                credential = user.credentialManager().getFederatedCredentialsStream().filter(c -> credentialId.equals(c.getId())).findAny().orElse(null);
                if (credential != null) {
                    String type = credential.getType();
                    checkIfCanBeRemoved(session, user, type, currentLoAProvider);
                    user.credentialManager().disableCredentialType(type);
                    return null;
                }
            }
            // 与账户控制台 1 向后兼容：未找到存储凭证时尝试 legacy 联邦占位 ID
            // In this case, it's ID needs to be something like "otp-id", which is returned by account REST GET endpoint as a placeholder
            // for federated credentials (See CredentialHelper.createUserStorageCredentialRepresentation )
            if (credentialId.endsWith("-id")) {
                String credentialType = credentialId.substring(0, credentialId.length() - 3);
                checkIfCanBeRemoved(session, user, credentialType, currentLoAProvider);
                user.credentialManager().disableCredentialType(credentialType);
                return null;
            }
            throw new NotFoundException("Credential not found");
        }
        checkIfCanBeRemoved(session, user, credential.getType(), currentLoAProvider);
        user.credentialManager().removeStoredCredentialById(credentialId);
        return credential;
    }

    /** 校验凭证类型可删除且当前 LoA 满足要求。 */
    private static void checkIfCanBeRemoved(KeycloakSession session, UserModel user, String credentialType, Supplier<Integer> currentLoAProvider) {
        CredentialProvider credentialProvider = AuthenticatorUtil.getCredentialProviders(session)
                .filter(credentialProvider1 -> credentialProvider1.supportsCredentialType(credentialType))
                .findAny().orElse(null);
        if (credentialProvider == null) {
            logger.warnf("Credential provider %s not found", credentialType);
            throw new NotFoundException("Credential provider not found");
        }
        CredentialTypeMetadataContext ctx = CredentialTypeMetadataContext.builder().user(user).build(session);
        CredentialTypeMetadata metadata = credentialProvider.getCredentialTypeMetadata(ctx);
        if (!metadata.isRemoveable()) {
            logger.warnf("Credential type %s cannot be removed", credentialType);
            throw new BadRequestException("Credential type cannot be removed");
        }

        // 阶梯认证场景下校验当前 LoA 是否允许删除该类型凭证
        checkAuthenticatedLoASufficientForCredentialRemove(session, credentialType, currentLoAProvider);
    }

    /** 比较当前 LoA 与删除该凭证类型所需的 LoA。 */
    private static void checkAuthenticatedLoASufficientForCredentialRemove(KeycloakSession session, String credentialType, Supplier<Integer> currentLoAProvider) {
        int requestedLoaForCredentialRemove = getRequestedLoaForCredential(session, credentialType);

        int currentAuthenticatedLevel = currentLoAProvider.get();
        if (currentAuthenticatedLevel < requestedLoaForCredentialRemove) {
            throw new ForbiddenException("Insufficient level of authentication for removing credential of type '" + credentialType + "'.");
        }
    }

    /** 从 browser 认证流程解析凭证类型对应的 LoA 级别。 */
    private static int getRequestedLoaForCredential(KeycloakSession session, String credentialType) {
        RealmModel realm = session.getContext().getRealm();
        ClientModel client = session.getContext().getClient();
        AuthenticationFlowModel authFlow = AuthenticationFlowResolver.resolveBindingOverrideFlowForClient(client, AuthenticationFlowBindings.BROWSER_BINDING);
        if (authFlow == null) {
            authFlow = realm.getBrowserFlow();
        }

        Map<String, Integer> credentialTypesToLoa = LoAUtil.getCredentialTypesToLoAMap(session, realm, authFlow);
        return credentialTypesToLoa.getOrDefault(credentialType, NO_LOA);
    }
}
