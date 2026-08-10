/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.role.RoleStorageProvider;
import org.keycloak.storage.role.RoleStorageProviderModel;

import org.jboss.logging.Logger;

/**
 * 硬编码角色存储提供者，用于测试套件中仅暴露单一联邦领域角色的查找与搜索。
 */
public class HardcodedRoleStorageProvider implements RoleStorageProvider {
    /** 角色存储组件模型。 */
    private final RoleStorageProviderModel component;
    /** 配置中指定的硬编码角色名。 */
    private final String roleName;

    /** @param component 角色存储组件模型 */
    public HardcodedRoleStorageProvider(RoleStorageProviderModel component) {
        this.component = component;
        this.roleName = component.getConfig().getFirst(HardcodedRoleStorageProviderFactory.ROLE_NAME);
    }

    @Override
    public void close() {
    }

    /** {@inheritDoc} 按名称查找硬编码领域角色。 */
    @Override
    public RoleModel getRealmRole(RealmModel realm, String name) {
        if (this.roleName.equals(name)) return new HardcodedRoleAdapter(realm);
        return null;
    }

    @Override
    public RoleModel getRoleById(RealmModel realm, String id) {
        StorageId storageId = new StorageId(id);
        final String roleName = storageId.getExternalId();
        if (this.roleName.equals(roleName)) return new HardcodedRoleAdapter(realm);
        return null;
    }

    /** {@inheritDoc} 按名称模糊搜索角色，可配置 5 秒延迟。 */
    @Override
    public Stream<RoleModel> searchForRolesStream(RealmModel realm, String search, Integer first, Integer max) {
        if (Boolean.parseBoolean(component.getConfig().getFirst(HardcodedRoleStorageProviderFactory.DELAYED_SEARCH))) try {
            Thread.sleep(5000l);
        } catch (InterruptedException ex) {
            Logger.getLogger(HardcodedClientStorageProvider.class).warn(ex.getCause());
            return Stream.empty();
        }
        if (search != null && this.roleName.toLowerCase().contains(search.toLowerCase())) {
            return Stream.of(new HardcodedRoleAdapter(realm));
        }
        return Stream.empty();
    }

    @Override
    public RoleModel getClientRole(ClientModel client, String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Stream<RoleModel> searchForClientRolesStream(ClientModel client, String search, Integer first, Integer max) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Stream<RoleModel> searchForClientRolesStream(RealmModel realm, String search, Stream<String> excludedIds, Integer first, Integer max) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Stream<RoleModel> searchForClientRolesStream(RealmModel realm, Stream<String> ids, String search, Integer first, Integer max) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** 只读硬编码 {@link RoleModel} 适配器。 */
    public class HardcodedRoleAdapter implements RoleModel {

        /** 所属领域。 */
        private final RealmModel realm;
        /** 延迟计算的存储 ID。 */
        private StorageId storageId;

        /** @param realm 所属领域 */
        public HardcodedRoleAdapter(RealmModel realm) {
            this.realm = realm;
        }

        @Override
        public String getId() {
            if (storageId == null) {
                storageId = new StorageId(component.getId(), getName());
            }
            return storageId.getId();
        }

        @Override
        public String getName() {
            return roleName;
        }

        @Override
        public String getDescription() {
            return "Federated Role";
        }

        @Override
        public boolean isComposite() {
            return false;
        }

        @Override
        public Stream<RoleModel> getCompositesStream(String search, Integer first, Integer max) {
            return Stream.empty();
        }

        @Override
        public boolean isClientRole() {
            return false;
        }

        @Override
        public String getContainerId() {
            return realm.getId();
        }

        @Override
        public RoleContainerModel getContainer() {
            return realm;
        }

        @Override
        public boolean hasRole(RoleModel role) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getFirstAttribute(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Stream<String> getAttributeStream(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Map<String, List<String>> getAttributes() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setDescription(String description) {
            throw new ReadOnlyException("role is read only");
        }

        @Override
        public void setName(String name) {
            throw new ReadOnlyException("role is read only");
        }

        @Override
        public void addCompositeRole(RoleModel role) {
            throw new ReadOnlyException("role is read only");
        }

        @Override
        public void removeCompositeRole(RoleModel role) {
            throw new ReadOnlyException("role is read only");
        }

        @Override
        public void setSingleAttribute(String name, String value) {
            throw new ReadOnlyException("role is read only");
        }

        @Override
        public void setAttribute(String name, List<String> values) {
            throw new ReadOnlyException("role is read only");
        }

        @Override
        public void removeAttribute(String name) {
            throw new ReadOnlyException("role is read only");
        }
    }


}
