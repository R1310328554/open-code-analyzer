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
import org.keycloak.models.GroupModel;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.group.GroupStorageProvider;
import org.keycloak.storage.group.GroupStorageProviderModel;

import org.jboss.logging.Logger;

/**
 * 硬编码组存储提供者，用于测试套件中仅暴露单一联邦组的查找与搜索。
 */
public class HardcodedGroupStorageProvider implements GroupStorageProvider {
    /** 组存储组件模型。 */
    private final GroupStorageProviderModel component;
    /** 配置中指定的硬编码组名。 */
    private final String groupName;

    /** @param component 组存储组件模型 */
    public HardcodedGroupStorageProvider(GroupStorageProviderModel component) {
        this.component = component;
        this.groupName = component.getConfig().getFirst(HardcodedGroupStorageProviderFactory.GROUP_NAME);
    }

    @Override
    public void close() {
    }

    /** {@inheritDoc} 按存储 ID 查找硬编码组。 */
    @Override
    public GroupModel getGroupById(RealmModel realm, String id) {
        StorageId storageId = new StorageId(id);
        final String groupName = storageId.getExternalId();
        if (this.groupName.equals(groupName)) return new HardcodedGroupAdapter(realm);
        return null;
    }

    @Override
    public GroupModel getGroupByName(RealmModel realm, GroupModel parent, String name) {
        if (this.groupName.equals(name)) return new HardcodedGroupAdapter(realm);
        return null;
    }

    /** {@inheritDoc} 按名称搜索组，支持精确/模糊匹配及可选延迟。 */
    @Override
    public Stream<GroupModel> searchForGroupByNameStream(RealmModel realm, String search, Boolean exact, Integer firstResult, Integer maxResults) {
        if (Boolean.parseBoolean(component.getConfig().getFirst(HardcodedGroupStorageProviderFactory.DELAYED_SEARCH))) try {
            Thread.sleep(5000l);
        } catch (InterruptedException ex) {
            Logger.getLogger(HardcodedGroupStorageProvider.class).warn(ex.getCause());
            return Stream.empty();
        }
        if(exact != null && exact){
            if (search != null && this.groupName.equals(search)) {
                return Stream.of(new HardcodedGroupAdapter(realm));
            }
        }else {
            if (search != null && this.groupName.toLowerCase().contains(search.toLowerCase())) {
                return Stream.of(new HardcodedGroupAdapter(realm));
            }
        }

        return Stream.empty();
    }

    @Override
    public Stream<GroupModel> searchGroupsByAttributes(RealmModel realm, Map<String, String> attributes, Integer firstResult, Integer maxResults) {
        if (Boolean.parseBoolean(component.getConfig().getFirst(HardcodedGroupStorageProviderFactory.DELAYED_SEARCH))) try {
            Thread.sleep(5000l);
        } catch (InterruptedException ex) {
            Logger.getLogger(HardcodedGroupStorageProvider.class).warn(ex.getCause());
            return Stream.empty();
        }

        return Stream.empty();
    }


    /** 只读硬编码 {@link GroupModel} 适配器。 */
    public class HardcodedGroupAdapter implements GroupModel {

        /** 所属领域。 */
        private final RealmModel realm;
        /** 延迟计算的存储 ID。 */
        private StorageId storageId;

        /** @param realm 所属领域 */
        public HardcodedGroupAdapter(RealmModel realm) {
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
            return groupName;
        }

        @Override
        public String getDescription() {
            return null;
        }

        public void setDescription(String description) {
            throw new ReadOnlyException("group is read only");
        }

        @Override
        public Stream<RoleModel> getRealmRoleMappingsStream() {
            return Stream.empty();
        }

        @Override
        public Stream<RoleModel> getClientRoleMappingsStream(ClientModel app) {
            return Stream.empty();
        }

        @Override
        public boolean hasRole(RoleModel role) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Stream<RoleModel> getRoleMappingsStream() {
            return Stream.empty();
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
        public GroupModel getParent() {
            return null;
        }

        @Override
        public String getParentId() {
            return null;
        }

        @Override
        public OrganizationModel getOrganization() {
            return null;
        }

        @Override
        public Stream<GroupModel> getSubGroupsStream() {
            return Stream.empty();
        }

        @Override
        public void deleteRoleMapping(RoleModel role) {
            throw new ReadOnlyException("group is read only");
        }

        @Override
        public void grantRole(RoleModel role) {
            throw new ReadOnlyException("group is read only");
        }

        @Override
        public void setParent(GroupModel group) {
            throw new ReadOnlyException("group is read only");
        }

        @Override
        public void addChild(GroupModel subGroup) {
            throw new ReadOnlyException("group is read only");
        }

        @Override
        public void removeChild(GroupModel subGroup) {
            throw new ReadOnlyException("group is read only");
        }

        @Override
        public void setName(String name) {
            throw new ReadOnlyException("group is read only");
        }

        @Override
        public void setSingleAttribute(String name, String value) {
            throw new ReadOnlyException("group is read only");
        }

        @Override
        public void setAttribute(String name, List<String> values) {
            throw new ReadOnlyException("group is read only");
        }

        @Override
        public void removeAttribute(String name) {
            throw new ReadOnlyException("group is read only");
        }
    }


}
