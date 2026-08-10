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
 * Realm 导出/导入时组件树的 REST 表示，支持嵌套子组件与多值配置。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ComponentExportRepresentation {

    /** 组件 UUID。 */
    private String id;
    /** 组件显示名称。 */
    private String name;
    /** SPI 提供方 ID。 */
    private String providerId;
    /** 组件子类型。 */
    private String subType;
    /** 嵌套子组件（类型 → 子组件列表）。 */
    private MultivaluedHashMap<String, ComponentExportRepresentation> subComponents = new MultivaluedHashMap<>();
    /** 组件配置项（键 → 值列表）。 */
    private MultivaluedHashMap<String, String> config = new MultivaluedHashMap<>();

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

    /** @return 子组件映射 */
    public MultivaluedHashMap<String, ComponentExportRepresentation> getSubComponents() {
        return subComponents;
    }

    /** @param subComponents 子组件映射 */
    public void setSubComponents(MultivaluedHashMap<String, ComponentExportRepresentation> subComponents) {
        this.subComponents = subComponents;
    }
}
