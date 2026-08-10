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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 声明式用户 Profile 中单个属性的配置表示，定义属性名称、校验规则、权限、必填条件及 UI 分组等。
 *
 * @author Vlastimil Elias <velias@redhat.com>
 *
 */
public class UPAttribute implements Cloneable {

    /** 属性内部名称（存储键）。 */
    private String name;
    /** 面向 UI 的显示名称。 */
    private String displayName;
    /** 校验器映射：键为校验器名称，值为该校验器的配置参数。 */
    private Map<String, Map<String, Object>> validations;
    /** 自定义注解键值对，供 UI 或扩展逻辑使用。 */
    private Map<String, Object> annotations;
    /** 必填规则；{@code null} 表示非必填。 */
    private UPAttributeRequired required;
    /** 读写权限；{@code null} 表示所有人可查看与编辑。 */
    private UPAttributePermissions permissions;
    /** 可见性选择器；{@code null} 表示始终展示。 */
    private UPAttributeSelector selector;
    /** 所属 UI 分组名称。 */
    private String group;
    /** 是否允许多值。 */
    private boolean multivalued;
    /** 属性默认值。 */
    private String defaultValue;

    /** 默认构造函数，供 JSON 反序列化使用。 */
    public UPAttribute() {
    }

    /**
     * 以属性名构造配置；名称会自动 trim。
     *
     * @param name 属性名称
     */
    public UPAttribute(String name) {
        this.name = name != null ? name.trim() : null;
    }

    /**
     * 构造带分组的属性配置。
     *
     * @param name  属性名称
     * @param group 分组对象
     */
    public UPAttribute(String name, UPGroup group) {
        this(name);
        this.group = group.getName();
    }

    /**
     * 构造带权限、必填与可见性规则的属性配置。
     *
     * @param name        属性名称
     * @param permissions 读写权限
     * @param required    必填规则
     * @param selector    可见性选择器
     */
    public UPAttribute(String name, UPAttributePermissions permissions, UPAttributeRequired required, UPAttributeSelector selector) {
        this(name);
        this.permissions = permissions;
        this.required = required;
        this.selector = selector;
    }

    /**
     * 构造带权限与必填规则的属性配置。
     *
     * @param name        属性名称
     * @param permissions 读写权限
     * @param required    必填规则
     */
    public UPAttribute(String name, UPAttributePermissions permissions, UPAttributeRequired required) {
        this(name, permissions, required, null);
    }

    /**
     * 构造仅带权限的属性配置。
     *
     * @param name        属性名称
     * @param permissions 读写权限
     */
    public UPAttribute(String name, UPAttributePermissions permissions) {
        this(name, permissions, null);
    }

    /**
     * 构造带多值标志与权限的属性配置。
     *
     * @param name         属性名称
     * @param multivalued  是否多值
     * @param permissions  读写权限
     */
    public UPAttribute(String name, boolean multivalued, UPAttributePermissions permissions) {
        this(name, permissions, null);
        setMultivalued(multivalued);
    }

    /**
     * 构造带自定义注解的属性配置。
     *
     * @param name        属性名称
     * @param annotations 注解映射
     */
    public UPAttribute(String name, Map<String, Object> annotations) {
        this(name);
        this.annotations = annotations;
    }

    /** @return 属性名称 */
    public String getName() {
        return name;
    }

    /** @param name 属性名称（自动 trim） */
    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    /** @return 校验器配置映射 */
    public Map<String, Map<String, Object>> getValidations() {
        return validations;
    }

    /** @param validations 校验器配置映射 */
    public void setValidations(Map<String, Map<String, Object>> validations) {
        this.validations = validations;
    }

    /** @return 自定义注解 */
    public Map<String, Object> getAnnotations() {
        return annotations;
    }

    /** @param annotations 自定义注解 */
    public void setAnnotations(Map<String, Object> annotations) {
        this.annotations = annotations;
    }

    /** @return 必填规则 */
    public UPAttributeRequired getRequired() {
        return required;
    }

    /** @param required 必填规则 */
    public void setRequired(UPAttributeRequired required) {
        this.required = required;
    }

    /** @return 读写权限 */
    public UPAttributePermissions getPermissions() {
        return permissions;
    }

    /** @param permissions 读写权限 */
    public void setPermissions(UPAttributePermissions permissions) {
        this.permissions = permissions;
    }

    /**
     * 追加或覆盖一条校验器配置。
     *
     * @param validator 校验器名称
     * @param config    校验器参数
     */
    public void addValidation(String validator, Map<String, Object> config) {
        if (validations == null) {
            validations = new HashMap<>();
        }
        validations.put(validator, config);
    }

    /** @return 可见性选择器 */
    public UPAttributeSelector getSelector() {
        return selector;
    }

    /** @param selector 可见性选择器 */
    public void setSelector(UPAttributeSelector selector) {
        this.selector = selector;
    }

    /** @return 显示名称 */
    public String getDisplayName() {
        return displayName;
    }

    /** @param displayName 显示名称 */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /** @return 分组名称 */
    public String getGroup() {
        return group;
    }

    /** @param group 分组名称（自动 trim） */
    public void setGroup(String group) {
        this.group = group != null ? group.trim() : null;
    }

    /** @param multivalued 是否多值 */
    public void setMultivalued(boolean multivalued) {
        this.multivalued = multivalued;
    }

    /** @return 默认值 */
    public String getDefaultValue() {
        return defaultValue;
    }

    /** @param defaultValue 默认值 */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /** @return 是否多值 */
    public boolean isMultivalued() {
        return multivalued;
    }

    @Override
    public String toString() {
        return "UPAttribute [name=" + name + ", displayName=" + displayName + ", permissions=" + permissions + ", selector=" + selector + ", required=" + required + ", validations=" + validations + ", annotations=" + annotations + ", group=" + group + ", multivalued=" + multivalued + ", defaultValue=" + defaultValue + "]";
    }

    /**
     * 深拷贝当前属性配置（嵌套 Map 与关联对象均复制）。
     *
     * @return 独立的属性配置副本
     */
    @Override
    protected UPAttribute clone() {
        UPAttribute attr = new UPAttribute(this.name);
        attr.setDisplayName(this.displayName);

        Map<String, Map<String, Object>> validations;
        if (this.validations == null) {
            validations = null;
        } else {
            validations = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, Object>> entry : this.validations.entrySet()) {
                Map<String, Object> newVal = entry.getValue() == null ? null : new LinkedHashMap<>(entry.getValue());
                validations.put(entry.getKey(), newVal);
            }
        }
        attr.setValidations(validations);

        attr.setAnnotations(this.annotations == null ? null : new HashMap<>(this.annotations));
        attr.setRequired(this.required == null ? null : this.required.clone());
        attr.setPermissions(this.permissions == null ? null : this.permissions.clone());
        attr.setSelector(this.selector == null ? null : this.selector.clone());
        attr.setGroup(this.group);
        attr.setMultivalued(this.multivalued);
        attr.setDefaultValue(this.defaultValue);
        return attr;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final UPAttribute other = (UPAttribute) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.displayName, other.displayName)
                && Objects.equals(this.group, other.group)
                && Objects.equals(this.validations, other.validations)
                && Objects.equals(this.annotations, other.annotations)
                && Objects.equals(this.required, other.required)
                && Objects.equals(this.permissions, other.permissions)
                && Objects.equals(this.selector, other.selector)
                && Objects.equals(this.multivalued, other.multivalued)
                && Objects.equals(this.defaultValue, other.defaultValue);
    }
}
