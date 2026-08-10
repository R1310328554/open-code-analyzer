/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.testsuite.federation;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.clientscope.ClientScopeLookupProvider;
import org.keycloak.storage.clientscope.ClientScopeStorageProvider;
import org.keycloak.storage.clientscope.ClientScopeStorageProviderModel;

/**
 * 硬编码客户端范围存储提供者，用于测试套件中仅暴露单一联邦客户端范围的查找。
 * 实现 {@link ClientScopeStorageProvider} 与 {@link ClientScopeLookupProvider}。
 */
public class HardcodedClientScopeStorageProvider implements ClientScopeStorageProvider, ClientScopeLookupProvider {

    /** 关联的客户端范围存储组件模型。 */
    private final ClientScopeStorageProviderModel component;
    /** 配置中指定的硬编码客户端范围名称。 */
    private final String clientScopeName;

    /**
     * @param session Keycloak 会话
     * @param component 客户端范围存储组件模型
     */
    public HardcodedClientScopeStorageProvider(KeycloakSession session, ClientScopeStorageProviderModel component) {
        this.component = component;
        this.clientScopeName = component.getConfig().getFirst(HardcodedClientScopeStorageProviderFactory.SCOPE_NAME);
    }

    /** {@inheritDoc} 按存储 ID 查找硬编码客户端范围。 */
    @Override
    public ClientScopeModel getClientScopeById(RealmModel realm, String id) {
        StorageId storageId = new StorageId(id);
        final String scopeName = storageId.getExternalId();
        if (this.clientScopeName.equals(scopeName)) return new HardcodedClientScopeAdapter(realm);
        return null;
    }

    /** {@inheritDoc} 无资源需释放。 */
    @Override
    public void close() {
    }

    /** 只读硬编码 {@link ClientScopeModel} 适配器。 */
    public class HardcodedClientScopeAdapter implements ClientScopeModel {

        private final RealmModel realm;
        private StorageId storageId;

        /** @param realm 所属领域 */
        public HardcodedClientScopeAdapter(RealmModel realm) {
            this.realm = realm;
        }

        /** {@inheritDoc} 基于组件 ID 与范围名生成存储 ID。 */
        @Override
        public String getId() {
            if (storageId == null) {
                storageId = new StorageId(component.getId(), getName());
            }
            return storageId.getId();
        }

        /** {@inheritDoc} 返回配置的硬编码范围名。 */
        @Override
        public String getName() {
            return clientScopeName;
        }

        @Override
        public RealmModel getRealm() {
            return realm;
        }

        @Override
        public void setName(String name) {
            throw new UnsupportedOperationException("Not supported.");
        }

        /** {@inheritDoc} 返回联邦客户端范围描述。 */
        @Override
        public String getDescription() {
            return "Federated client scope";
        }

        @Override
        public void setDescription(String description) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getProtocol() {
            return "openid-connect";
        }

        @Override
        public void setProtocol(String protocol) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setAttribute(String name, String value) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void removeAttribute(String name) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getAttribute(String name) {
            return null;
        }

        @Override
        public Map<String, String> getAttributes() {
            return Collections.EMPTY_MAP;
        }

        @Override
        public Stream<ProtocolMapperModel> getProtocolMappersStream() {
            return Stream.empty();
        }

        @Override
        public ProtocolMapperModel addProtocolMapper(ProtocolMapperModel model) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void removeProtocolMapper(ProtocolMapperModel mapping) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void updateProtocolMapper(ProtocolMapperModel mapping) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public ProtocolMapperModel getProtocolMapperById(String id) {
            return null;
        }

        @Override
        public ProtocolMapperModel getProtocolMapperByName(String protocol, String name) {
            return null;
        }

        @Override
        public Stream<RoleModel> getScopeMappingsStream() {
            return Stream.empty();
        }

        @Override
        public Stream<RoleModel> getRealmScopeMappingsStream() {
            return Stream.empty();
        }

        @Override
        public void addScopeMapping(RoleModel role) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void deleteScopeMapping(RoleModel role) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean hasScope(RoleModel role) {
            return false;
        }
    }
}
