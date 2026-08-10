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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 用户联合映射器类型（SPI 元数据）的 REST 表示，供管理控制台展示可创建的映射器类型及其配置项。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class UserFederationMapperTypeRepresentation {
    /** 映射器类型 SPI ID。 */
    protected String id;
    /** 映射器类型显示名称。 */
    protected String name;
    /** 映射器分类（用于 UI 分组）。 */
    protected String category;
    /** 帮助说明文本。 */
    protected String helpText;

    /** 该类型映射器支持的同步能力配置。 */
    protected UserFederationMapperSyncConfigRepresentation syncConfig;
    /** 可配置属性定义列表。 */
    protected List<ConfigPropertyRepresentation> properties  = new LinkedList<>();
    /** 默认配置键值对。 */
    protected Map<String, String> defaultConfig = new HashMap<>();


    /** @return 映射器类型 ID */
    public String getId() {
        return id;
    }

    /** @param id 映射器类型 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 映射器类型名称 */
    public String getName() {
        return name;
    }

    /** @param name 映射器类型名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 映射器分类 */
    public String getCategory() {
        return category;
    }

    /** @param category 映射器分类 */
    public void setCategory(String category) {
        this.category = category;
    }

    /** @return 帮助说明文本 */
    public String getHelpText() {
        return helpText;
    }

    /** @param helpText 帮助说明文本 */
    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    /** @return 同步能力配置 */
    public UserFederationMapperSyncConfigRepresentation getSyncConfig() {
        return syncConfig;
    }

    /** @param syncConfig 同步能力配置 */
    public void setSyncConfig(UserFederationMapperSyncConfigRepresentation syncConfig) {
        this.syncConfig = syncConfig;
    }

    /** @return 可配置属性定义列表 */
    public List<ConfigPropertyRepresentation> getProperties() {
        return properties;
    }

    /** @param properties 可配置属性定义列表 */
    public void setProperties(List<ConfigPropertyRepresentation> properties) {
        this.properties = properties;
    }

    /** @return 默认配置 */
    public Map<String, String> getDefaultConfig() {
        return defaultConfig;
    }

    /** @param defaultConfig 默认配置 */
    public void setDefaultConfig(Map<String, String> defaultConfig) {
        this.defaultConfig = defaultConfig;
    }
}
