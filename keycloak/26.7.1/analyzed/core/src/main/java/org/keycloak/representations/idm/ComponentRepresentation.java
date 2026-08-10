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

import org.keycloak.common.util.MultivaluedHashMap;

/**
 * Keycloak 组件（如 User Storage、Client Storage）的 Admin REST 表示。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ComponentRepresentation {

    /** 敏感配置项在 REST 响应中的占位掩码值。 */
    public static final String SECRET_VALUE = "**********";

    /** 组件 UUID。 */
    private String id;
    /** 组件显示名称。 */
    private String name;
    /** SPI 提供方 ID。 */
    private String providerId;
    /** 提供方接口类型全限定名。 */
    private String providerType;
    /** 父组件 ID（用于嵌套结构）。 */
    private String parentId;
    /** 组件子类型。 */
    private String subType;
    /** 组件配置项（键 → 值列表）。 */
    private MultivaluedHashMap<String, String> config;

    /** @return 组件 ID */
    public String getId() {
        return id;
    }

    /** @param id 组件 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 组件名称 */
    public String getName() {
        return name;
    }

    /** @param name 组件名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 提供方 ID */
    public String getProviderId() {
        return providerId;
    }

    /** @param providerId 提供方 ID */
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    /** @return 提供方类型 */
    public String getProviderType() {
        return providerType;
    }

    /** @param providerType 提供方类型 */
    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    /** @return 父组件 ID */
    public String getParentId() {
        return parentId;
    }

    /** @param parentId 父组件 ID */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /** @return 子类型 */
    public String getSubType() {
        return subType;
    }

    /** @param subType 子类型 */
    public void setSubType(String subType) {
        this.subType = subType;
    }

    /** @return 配置映射 */
    public MultivaluedHashMap<String, String> getConfig() {
        return config;
    }

    /** @param config 配置映射 */
    public void setConfig(MultivaluedHashMap<String, String> config) {
        this.config = config;
    }

    /** 仅基于 ID 比较相等性。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComponentRepresentation that = (ComponentRepresentation) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    /** 基于 ID 计算哈希。 */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
