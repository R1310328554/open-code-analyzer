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
package org.keycloak.credential;

import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * 凭据输入校验 SPI：验证 {@link CredentialInput}（如密码）。
 * <p>{@link org.keycloak.storage.UserStorageProvider} 与 {@link CredentialProvider} 可实现此接口。</p>
 * Implentations of this interface can validate CredentialInput, i.e. verify a password.
 * UserStorageProviders and CredentialProviders can implement this interface.
 *
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface CredentialInputValidator {
    /** 是否支持指定凭据类型。 */
    boolean supportsCredentialType(String credentialType);
    /** 用户是否已配置指定类型的凭据。 */
    boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType);

    /**
     * 校验凭据是否有效。
     * Tests whether a credential is valid
     * @param realm The realm in which to which the credential belongs to
     * @param user The user for which to test the credential
     * @param credentialInput the credential details to verify
     * @return true if the passed secret is correct
     */
    boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput);
}
