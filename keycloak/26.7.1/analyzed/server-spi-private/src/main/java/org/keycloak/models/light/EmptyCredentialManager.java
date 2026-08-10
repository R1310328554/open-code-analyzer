/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.models.light;

import java.util.List;
import java.util.stream.Stream;

import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.SubjectCredentialManager;

/**
 * 空实现的 {@link org.keycloak.models.SubjectCredentialManager}。
 * <p>供 {@link LightweightUserAdapter} 等无需持久化凭证的临时用户使用；所有操作均为 no-op 或返回空/false。</p>
 *
 * @author hmlnarik
 */
class EmptyCredentialManager implements SubjectCredentialManager {

    /** 单例实例，供轻量用户适配器复用。 */
    public static final EmptyCredentialManager INSTANCE = new EmptyCredentialManager();

    /** 轻量用户无存储凭证，始终返回 false。 */
    @Override
    public boolean isValid(List<CredentialInput> inputs) {
        return false;
    }

    @Override
    public boolean updateCredential(CredentialInput input) {
        // 无操作（轻量用户不支持凭证持久化）
        return false;
    }

    @Override
    public void updateStoredCredential(CredentialModel cred) {
        // 无操作（轻量用户不支持凭证持久化）
    }

    @Override
    public CredentialModel createStoredCredential(CredentialModel cred) {
        // 无操作（轻量用户不支持凭证持久化）
        return null;
    }

    @Override
    public boolean removeStoredCredentialById(String id) {
        // 无操作（轻量用户不支持凭证持久化）
        return false;
    }

    @Override
    public CredentialModel getStoredCredentialById(String id) {
        return null;
    }

    /** 返回空流：无已存储凭证。 */
    @Override
    public Stream<CredentialModel> getStoredCredentialsStream() {
        return Stream.empty();
    }

    @Override
    public Stream<CredentialModel> getStoredCredentialsByTypeStream(String type) {
        return Stream.empty();
    }

    @Override
    public CredentialModel getStoredCredentialByNameAndType(String name, String type) {
        return null;
    }

    @Override
    public boolean moveStoredCredentialTo(String id, String newPreviousCredentialId) {
        return false;
    }

    @Override
    public void updateCredentialLabel(String credentialId, String credentialLabel) {
        // 无操作（轻量用户不支持凭证持久化）
    }

    @Override
    public void disableCredentialType(String credentialType) {
        // 无操作（轻量用户不支持凭证持久化）
    }

    @Override
    public Stream<String> getDisableableCredentialTypesStream() {
        return Stream.empty();
    }

    @Override
    public boolean isConfiguredFor(String type) {
        return false;
    }

    @Override
    public boolean isConfiguredLocally(String type) {
        return false;
    }

    @Override
    public Stream<String> getConfiguredUserStorageCredentialTypesStream() {
        return Stream.empty();
    }

    @Override
    public CredentialModel createCredentialThroughProvider(CredentialModel model) {
        return null;
    }

}
