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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户组（Group）的 REST 表示，支持层级结构、角色映射及细粒度访问权限。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class GroupRepresentation {
    // 单个组的最小字段集，足以标识并执行基本操作
    /** 组持久化 ID。 */
    protected String id;
    /** 组名称。 */
    protected String name;
    /** 组描述。 */
    protected String description;
    /** 组在层级中的路径（如 /parent/child）。 */
    protected String path;
    /** 父组 ID。 */
    protected String parentId;
    /** 直接子组数量。 */
    protected Long subGroupCount;
    // 导航组层级时可包含子组的最小表示；默认不填充，按需包含
    /** 子组列表（按需填充）。 */
    protected List<GroupRepresentation> subGroups;
    /** 组自定义属性。 */
    protected Map<String, List<String>>  attributes;
    /** 直接分配的 realm 角色名称列表。 */
    protected List<String> realmRoles;
    /** 客户端 ID 到客户端角色名称列表的映射。 */
    protected Map<String, List<String>> clientRoles;

    /** 当前用户对组各操作的访问权限映射。 */
    private Map<String, Boolean> access;

    /** @return 组 ID */
    public String getId() {
        return id;
    }

    /** @param id 组 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 组名称 */
    public String getName() {
        return name;
    }

    /** @param name 组名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 组描述 */
    public String getDescription() {
        return description;
    }

    /** @param description 组描述 */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return 组路径 */
    public String getPath() {
        return path;
    }

    /** @param path 组路径 */
    public void setPath(String path) {
        this.path = path;
    }

    /** @return 父组 ID */
    public String getParentId() {
        return parentId;
    }

    /** @param parentId 父组 ID */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /** @return 直接子组数量 */
    public Long getSubGroupCount() {
        return subGroupCount;
    }

    /** @param subGroupCount 直接子组数量 */
    public void setSubGroupCount(Long subGroupCount) {
        this.subGroupCount = subGroupCount;
    }

    /** @return realm 角色列表 */
    public List<String> getRealmRoles() {
        return realmRoles;
    }

    /** @param realmRoles realm 角色列表 */
    public void setRealmRoles(List<String> realmRoles) {
        this.realmRoles = realmRoles;
    }

    /** @return 客户端角色映射 */
    public Map<String, List<String>> getClientRoles() {
        return clientRoles;
    }

    /** @param clientRoles 客户端角色映射 */
    public void setClientRoles(Map<String, List<String>> clientRoles) {
        this.clientRoles = clientRoles;
    }


    /** @return 组属性 */
    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    /** @param attributes 组属性 */
    public void setAttributes(Map<String, List<String>>  attributes) {
        this.attributes = attributes;
    }

    /**
     * 便捷方法：设置单个属性键值对。
     *
     * @param name 属性名
     * @param value 属性值
     * @return 当前实例（链式调用）
     */
    public GroupRepresentation singleAttribute(String name, String value) {
        if (this.attributes == null) attributes = new HashMap<>();
        attributes.put(name, Arrays.asList(value));
        return this;
    }

    /** @return 子组列表（懒初始化） */
    public List<GroupRepresentation> getSubGroups() {
        if(subGroups == null) {
            subGroups = new ArrayList<>();
        }
        return subGroups;
    }

    /** @param subGroups 子组列表 */
    public void setSubGroups(List<GroupRepresentation> subGroups) {
        this.subGroups = subGroups;
    }

    /** @return 访问权限映射 */
    public Map<String, Boolean> getAccess() {
        return access;
    }

    /** @param access 访问权限映射 */
    public void setAccess(Map<String, Boolean> access) {
        this.access = access;
    }

    /** 将另一组表示的子组树合并到当前实例。 */
    public void merge(GroupRepresentation g) {
        merge(this, g);
    }

    /** 递归合并两个等效组的子组树。 */
    private void merge(GroupRepresentation g1, GroupRepresentation g2) {
        if(g1.equals(g2)) {
            Map<String, GroupRepresentation> g1Children = g1.getSubGroups().stream().collect(Collectors.toMap(GroupRepresentation::getId, g -> g));
            Map<String, GroupRepresentation> g2Children = g2.getSubGroups().stream().collect(Collectors.toMap(GroupRepresentation::getId, g -> g));

            g2Children.forEach((key, value) -> {
                if (g1Children.containsKey(key)) {
                    merge(g1Children.get(key), value);
                } else {
                    g1Children.put(key, value);
                }
            });
            g1.setSubGroups(new ArrayList<>(g1Children.values()));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupRepresentation that = (GroupRepresentation) o;
        boolean isEqual = Objects.equals(id, that.id) && Objects.equals(parentId, that.parentId);
        if(isEqual) {
            return true;
        } else {
            return Objects.equals(name, that.name) && Objects.equals(path, that.path);
        }
    }

    @Override
    public int hashCode() {
        if(id == null) {
            return Objects.hash(name, path);
        }
        return Objects.hash(id, parentId);
    }
}
