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

package org.keycloak.representations.idm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 角色（Role）的 REST 表示，支持 realm 角色与客户端角色，可描述复合角色及其属性。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RoleRepresentation {
    /** 角色持久化 ID。 */
    protected String id;
    /** 角色名称。 */
    protected String name;
    /** 角色描述。 */
    protected String description;
    /** @deprecated 是否要求 scope 参数（已废弃）。 */
    @Deprecated
    protected Boolean scopeParamRequired;
    /** 是否为复合角色。 */
    protected boolean composite;
    /** 复合角色包含的子角色集合。 */
    protected Composites composites;
    /** 是否为客户端角色（否则为 realm 角色）。 */
    private Boolean clientRole;
    /** 所属容器 ID（realm 或客户端 ID）。 */
    private String containerId;
    /** 角色自定义属性。 */
    protected Map<String, List<String>> attributes;

    /** 复合角色的子角色结构。 */
    public static class Composites {
        /** 包含的 realm 角色名称集合。 */
        protected Set<String> realm;
        /** 客户端 ID 到客户端角色名称列表的映射。 */
        protected Map<String, List<String>> client;
        /** @deprecated 应用角色映射（已废弃，由 client 替代）。 */
        @Deprecated
        protected Map<String, List<String>> application;

        /** @return realm 子角色名称集合 */
        public Set<String> getRealm() {
            return realm;
        }

        /** @param realm realm 子角色名称集合 */
        public void setRealm(Set<String> realm) {
            this.realm = realm;
        }

        /** @return 客户端子角色映射 */
        public Map<String, List<String>> getClient() {
            return client;
        }

        /** @param client 客户端子角色映射 */
        public void setClient(Map<String, List<String>> client) {
            this.client = client;
        }

        /** @return 应用子角色映射（已废弃） */
        @Deprecated
        public Map<String, List<String>> getApplication() {
            return application;
        }
    }

    /** 默认构造函数。 */
    public RoleRepresentation() {
    }

    /**
     * 按名称、描述及 scope 参数要求构造角色。
     *
     * @param name 角色名称
     * @param description 角色描述
     * @param scopeParamRequired 是否要求 scope 参数
     */
    public RoleRepresentation(String name, String description, boolean scopeParamRequired) {
        this.name = name;
        this.description = description;
        this.scopeParamRequired = scopeParamRequired;
    }

    /** @return 角色 ID */
    public String getId() {
        return id;
    }

    /** @param id 角色 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 角色名称 */
    public String getName() {
        return name;
    }

    /** @param name 角色名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 角色描述 */
    public String getDescription() {
        return description;
    }

    /** @param description 角色描述 */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return 是否要求 scope 参数（已废弃） */
    @Deprecated
    public Boolean isScopeParamRequired() {
        return scopeParamRequired;
    }

    /** @return 复合角色子角色结构 */
    public Composites getComposites() {
        return composites;
    }

    /** @param composites 复合角色子角色结构 */
    public void setComposites(Composites composites) {
        this.composites = composites;
    }

    @Override
    public String toString() {
        return name;
    }

    /** @return 是否为复合角色 */
    public boolean isComposite() {
        return composite;
    }

    /** @param composite 是否为复合角色 */
    public void setComposite(boolean composite) {
        this.composite = composite;
    }

    /** @return 是否为客户端角色 */
    public Boolean getClientRole() {
        return clientRole;
    }

    /** @param clientRole 是否为客户端角色 */
    public void setClientRole(Boolean clientRole) {
        this.clientRole = clientRole;
    }

    /** @return 所属容器 ID */
    public String getContainerId() {
        return containerId;
    }

    /** @param containerId 所属容器 ID */
    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    /** @return 角色属性 */
    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    /** @param attributes 角色属性 */
    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }

    /**
     * 便捷方法：设置单个属性值并返回自身以支持链式调用。
     *
     * @param name 属性名
     * @param value 属性值
     * @return 当前角色表示
     */
    public RoleRepresentation singleAttribute(String name, String value) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }

        attributes.put(name, Arrays.asList(value));
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.id);
        hash = 29 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || (!(obj instanceof RoleRepresentation))) {
            return false;
        }
        final RoleRepresentation other = (RoleRepresentation) obj;
        return Objects.equals(this.id, other.id) && Objects.equals(this.name, other.name);
    }
}
