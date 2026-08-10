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
import java.util.Map;
import java.util.Objects;

/**
 * 用户 Profile 属性分组配置，定义 UI 分组名称、标题、描述及自定义注解。
 *
 * @author <a href="joerg.matysiak@bosch.io">Jörg Matysiak</a>
 */
public class UPGroup implements Cloneable {

    /** 分组内部名称（存储键）。 */
    private String name;
    /** 分组在 UI 中显示的标题。 */
    private String displayHeader;
    /** 分组在 UI 中显示的描述文本。 */
    private String displayDescription;
    /** 自定义注解键值对，供 UI 或扩展逻辑使用。 */
    private Map<String, Object> annotations;

    /** 默认构造函数，供 JSON 反序列化与反射使用。 */
    public UPGroup() {
        // 供反射使用
    }

    /**
     * 以分组名称构造配置。
     *
     * @param name 分组名称
     */
    public UPGroup(String name) {
        this.name = name;
    }

    /** @return 分组名称 */
    public String getName() {
        return name;
    }

    /** @param name 分组名称（自动 trim） */
    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    /** @return 显示标题 */
    public String getDisplayHeader() {
        return displayHeader;
    }

    /** @param displayHeader 显示标题 */
    public void setDisplayHeader(String displayHeader) {
        this.displayHeader = displayHeader;
    }

    /** @return 显示描述 */
    public String getDisplayDescription() {
        return displayDescription;
    }

    /** @param displayDescription 显示描述 */
    public void setDisplayDescription(String displayDescription) {
        this.displayDescription = displayDescription;
    }

    /** @return 自定义注解 */
    public Map<String, Object> getAnnotations() {
        return annotations;
    }

    /** @param annotations 自定义注解 */
    public void setAnnotations(Map<String, Object> annotations) {
        this.annotations = annotations;
    }

    /**
     * 深拷贝当前分组配置（注解 Map 独立复制）。
     *
     * @return 独立的分组配置副本
     */
    @Override
    protected UPGroup clone() {
        UPGroup group = new UPGroup(this.name);
        group.setDisplayHeader(displayHeader);
        group.setDisplayDescription(displayDescription);
        group.setAnnotations(this.annotations == null ? null : new HashMap<>(this.annotations));
        return group;
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
        final UPGroup other = (UPGroup) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.displayHeader, other.displayHeader)
                && Objects.equals(this.displayDescription, other.displayDescription)
                && Objects.equals(this.annotations, other.annotations);
    }
}
