/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.representations.userprofile.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 单个 Realm 的声明式用户 Profile 配置，包含属性列表、UI 分组及未托管属性策略。
 *
 * @author Vlastimil Elias <velias@redhat.com>
 *
 */
public class UPConfig implements Cloneable {

    /**
     * 未在 Profile 配置中显式声明的属性（未托管属性）的处理策略。
     */
    public enum UnmanagedAttributePolicy {

        /**
         * 未托管属性已启用，可在任意上下文中访问。
         */
        ENABLED,

        /**
         * 未托管属性仅可通过管理接口以只读方式访问。
         */
        ADMIN_VIEW,

        /**
         * 未托管属性仅可通过管理接口以读写方式访问。
         */
        ADMIN_EDIT
    }

    /** 已声明的属性配置列表。 */
    private List<UPAttribute> attributes;
    /** UI 分组配置列表。 */
    private List<UPGroup> groups;

    /** 未托管属性的访问策略。 */
    private UnmanagedAttributePolicy unmanagedAttributePolicy;

    /** @return 属性配置列表 */
    public List<UPAttribute> getAttributes() {
        return attributes;
    }

    /** @param attributes 属性配置列表 */
    public void setAttributes(List<UPAttribute> attributes) {
        this.attributes = attributes;
    }

    /**
     * 添加或替换同名属性（先移除旧项再追加）。
     *
     * @param attribute 属性配置
     * @return 当前配置实例，支持链式调用
     */
    public UPConfig addOrReplaceAttribute(UPAttribute attribute) {
        if (attributes == null) {
            attributes = new ArrayList<>();
        }

        removeAttribute(attribute.getName());
        attributes.add(attribute);

        return this;
    }

    /**
     * 按名称移除属性。
     *
     * @param name 属性名称
     * @return 若找到并移除则返回 {@code true}
     */
    public boolean removeAttribute(String name) {
        return attributes != null && attributes.removeIf(attribute -> attribute.getName().equals(name));
    }

    /** @return 分组列表；未配置时返回空列表 */
    public List<UPGroup> getGroups() {
        if (groups == null) {
            return Collections.emptyList();
        }
        return groups;
    }

    /** @param groups 分组列表 */
    public void setGroups(List<UPGroup> groups) {
        this.groups = groups;
    }

    /**
     * 追加一个 UI 分组。
     *
     * @param group 分组配置
     * @return 当前配置实例，支持链式调用
     */
    public UPConfig addGroup(UPGroup group) {
        if (groups == null) {
            groups = new ArrayList<>();
        }

        groups.add(group);

        return this;
    }

    /**
     * 按名称查找属性配置。
     *
     * @param name 属性名称
     * @return 匹配的属性配置，未找到则返回 {@code null}
     */
    @JsonIgnore
    public UPAttribute getAttribute(String name) {
        for (UPAttribute attribute : getAttributes()) {
            if (attribute.getName().equals(name)) {
                return attribute;
            }
        }
        return null;
    }

    /** @return 未托管属性策略 */
    public UnmanagedAttributePolicy getUnmanagedAttributePolicy() {
        return unmanagedAttributePolicy;
    }

    /** @param unmanagedAttributePolicy 未托管属性策略 */
    public void setUnmanagedAttributePolicy(UnmanagedAttributePolicy unmanagedAttributePolicy) {
        this.unmanagedAttributePolicy = unmanagedAttributePolicy;
    }

    @Override
    public String toString() {
        return "UPConfig [attributes=" + attributes + ", groups=" + groups + "]";
    }

    /**
     * 深拷贝当前 Profile 配置（属性与分组均递归复制）。
     *
     * @return 独立的配置副本
     */
    @Override
    public UPConfig clone() {
        UPConfig cfg = new UPConfig();

        cfg.setUnmanagedAttributePolicy(this.unmanagedAttributePolicy);
        if (attributes != null) {
            cfg.setAttributes(attributes.stream().map(UPAttribute::clone).collect(Collectors.toList()));
        }
        if (groups != null) {
            cfg.setGroups(groups.stream().map(UPGroup::clone).collect(Collectors.toList()));
        }

        return cfg;
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributes, groups, unmanagedAttributePolicy);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final UPConfig other = (UPConfig) obj;
        return Objects.equals(this.attributes, other.attributes)
                && Objects.equals(this.groups, other.groups)
                && this.unmanagedAttributePolicy == other.unmanagedAttributePolicy;
    }
}
