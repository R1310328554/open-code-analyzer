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

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

/**
 * 凭据提供者 SPI：管理特定类型凭据的创建、删除、展示元数据与类型描述。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface CredentialProvider<T extends CredentialModel> extends Provider {

    @Override
    default void close() {

    }

    /** @return 该提供者管理的凭据类型 ID */
    String getType();

    /** 为用户创建并持久化凭据。 */
    CredentialModel createCredential(RealmModel realm, UserModel user, T credentialModel);

    /** 删除用户指定 ID 的凭据。 */
    boolean deleteCredential(RealmModel realm, UserModel user, String credentialId);

    /** 将存储的 {@link CredentialModel} 转换为强类型凭据对象。 */
    T getCredentialFromModel(CredentialModel model);

    /**
     * 获取用于展示的凭据（可附加非持久化元数据，如 WebAuthn AAGUID 对应的认证器名称）。
     * Get the credential (usually stored credential retrieved from the DB) and decorates it with additional metadata
     * to be present for example in the admin console. Those additional metadata could be various metadata, which are not saved in the DB,
     * but can be retrieved from saved data to be presented to admins/users in the nice way (For example display "authenticator Provider"
     * for WebAuthn credential based on the AAGUID of WebAuthn credential)
     *
     * @param model stored credential retrieved from the DB
     * @return credential model useful for the presentation (not necessarily only stored data, but possibly some other metadata added)
     */
    default T getCredentialForPresentationFromModel(CredentialModel model) {
        T presentationModel = getCredentialFromModel(model);
        presentationModel.setFederationLink(model.getFederationLink());
        return presentationModel;
    }

    /** 获取用户该类型的默认（首条）凭据。 */
    default T getDefaultCredential(KeycloakSession session, RealmModel realm, UserModel user) {
        CredentialModel model = user.credentialManager().getStoredCredentialsByTypeStream(getType())
                .findFirst().orElse(null);
        return model != null ? getCredentialFromModel(model) : null;
    }

    /** 返回凭据类型的 UI 元数据（显示名、图标、必需操作等）。 */
    CredentialTypeMetadata getCredentialTypeMetadata(CredentialTypeMetadataContext metadataContext);

    /** 构建单条凭据的展示元数据。 */
    default CredentialMetadata getCredentialMetadata(T credentialModel, CredentialTypeMetadata credentialTypeMetadata) {
        CredentialMetadata credentialMetadata = new CredentialMetadata();
        credentialMetadata.setCredentialModel(credentialModel);
        return credentialMetadata;
    }

    /** 是否支持给定 {@link CredentialModel} 的类型。 */
    default boolean supportsCredentialType(CredentialModel credential) {
        return supportsCredentialType(credential.getType());
    }

    /** 是否支持指定类型字符串。 */
    default boolean supportsCredentialType(String type) {
        return getType().equals(type);
    }
}
