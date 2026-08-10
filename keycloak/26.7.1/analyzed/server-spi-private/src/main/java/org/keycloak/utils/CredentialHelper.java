/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.utils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ClientAuthenticator;
import org.keycloak.authentication.ClientAuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.models.credential.RecoveryAuthnCodesCredentialModel;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.util.JsonSerialization;

import org.jboss.logging.Logger;

/**
 * 凭证辅助工具：按类型设置认证执行状态，并创建 OTP/恢复码等凭证。
 * used to set an execution a state based on type.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class CredentialHelper {

    private static final Logger logger = Logger.getLogger(CredentialHelper.class);

    /** 将指定类型认证执行的要求状态设为 {@code requirement}（可选过滤当前状态）。 */
    public static void setOrReplaceAuthenticationRequirement(KeycloakSession session, RealmModel realm, String type, AuthenticationExecutionModel.Requirement requirement, AuthenticationExecutionModel.Requirement currentRequirement) {
        realm.getAuthenticationFlowsStream().forEach(flow -> realm.getAuthenticationExecutionsStream(flow.getId())
                .filter(exe -> {
                    ConfigurableAuthenticatorFactory factory = getConfigurableAuthenticatorFactory(session, exe.getAuthenticator());
                    return Objects.nonNull(factory) && Objects.equals(type, factory.getReferenceCategory());
                })
                .filter(exe -> {
                    if (Objects.isNull(currentRequirement) || Objects.equals(exe.getRequirement(), currentRequirement))
                        return true;
                    else {
                        logger.debugf("Skip switch authenticator execution '%s' to '%s' as it's in state %s",
                                exe.getAuthenticator(), requirement.toString(), exe.getRequirement());
                        return false;
                    }
                })
                .forEachOrdered(exe -> {
                    exe.setRequirement(requirement);
                    realm.updateAuthenticatorExecution(exe);
                    logger.debugf("Authenticator execution '%s' switched to '%s'", exe.getAuthenticator(), requirement.toString());
                }));
    }

    /** 按 {@code providerId} 查找可配置的认证器/表单/客户端认证器工厂。 */
    public static ConfigurableAuthenticatorFactory getConfigurableAuthenticatorFactory(KeycloakSession session, String providerId) {
        ConfigurableAuthenticatorFactory factory = (AuthenticatorFactory)session.getKeycloakSessionFactory().getProviderFactory(Authenticator.class, providerId);
        if (factory == null) {
            factory = (FormActionFactory)session.getKeycloakSessionFactory().getProviderFactory(FormAction.class, providerId);
        }
        if (factory == null) {
            factory = (ClientAuthenticatorFactory)session.getKeycloakSessionFactory().getProviderFactory(ClientAuthenticator.class, providerId);
        }
        return factory;
    }

    /**
     * 在用户存储或 Keycloak 本地库中创建 OTP 凭证。
     *
     * @return 成功创建且校验通过时返回 {@code true}；HOTP 校验失败等错误时返回 {@code false}
     */
    public static boolean createOTPCredential(KeycloakSession session, RealmModel realm, UserModel user, String totpCode, OTPCredentialModel credentialModel) {
        CredentialProvider otpCredentialProvider = session.getProvider(CredentialProvider.class, "keycloak-otp");
        String totpSecret = credentialModel.getOTPSecretData().getValue();

        UserCredentialModel otpUserCredential = new UserCredentialModel("", realm.getOTPPolicy().getType(), totpSecret);
        boolean userStorageCreated = user.credentialManager().updateCredential(otpUserCredential);

        String credentialId = null;
        if (userStorageCreated) {
            logger.debugf("Created OTP credential for user '%s' in the user storage", user.getUsername());
        } else {
            CredentialModel createdCredential = otpCredentialProvider.createCredential(realm, user, credentialModel);
            credentialId = createdCredential.getId();
        }

        // HOTP 类型需验证一次以消耗注册用 OTP 并递增计数器
        //If the type is HOTP, call verify once to consume the OTP used for registration and increase the counter.
        UserCredentialModel credential = new UserCredentialModel(credentialId, otpCredentialProvider.getType(), totpCode);
        return user.credentialManager().isValid(credential);
    }

    /**
     * 在用户存储或 Keycloak 本地库中创建恢复认证码凭证。
     * Create RecoveryCodes credential either in userStorage or local storage (Keycloak DB)
     */
    public static void createRecoveryCodesCredential(KeycloakSession session, RealmModel realm, UserModel user, RecoveryAuthnCodesCredentialModel credentialModel, List<String> generatedCodes) {
        var recoveryCodeCredentialProvider = session.getProvider(CredentialProvider.class, "keycloak-recovery-authn-codes");
        String recoveryCodesJson;
        try {
            recoveryCodesJson =  JsonSerialization.writeValueAsString(generatedCodes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UserCredentialModel recoveryCodesCredential = new UserCredentialModel("", credentialModel.getType(), recoveryCodesJson);

        boolean userStorageCreated = user.credentialManager().updateCredential(recoveryCodesCredential);
        if (userStorageCreated) {
            logger.debugf("Created RecoveryCodes credential for user '%s' in the user storage", user.getUsername());
        } else {
            recoveryCodeCredentialProvider.createCredential(realm, user, credentialModel);
        }
    }

    /**
     * 创建凭证的占位表示，通常用于用户存储仅提供类型而无更多细节的场景。
     *
     * @param credentialProviderType 凭证提供者类型
     * @return 占位凭证表示
     */
    public static CredentialRepresentation createUserStorageCredentialRepresentation(String credentialProviderType) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setId(credentialProviderType + "-id");
        credential.setType(credentialProviderType);
        credential.setCreatedDate(-1L);
        credential.setPriority(0);
        return credential;
    }
}
