/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 必需操作配置模型：存储必需操作 Provider 的配置项。
 * Holds the configuration for a required action.
 */
public class RequiredActionConfigModel implements Serializable {

    protected String id;

    protected String providerId;

    protected String alias;

    protected Map<String, String> config = new HashMap<>();

    /** @return 配置唯一标识符 */
    public String getId() {
        return id;
    }

    /** @param id 配置唯一标识符 */
    public void setId(String id) {
        this.id = id;
    }

    /** @return Provider 实现 ID */
    public String getProviderId() {
        return providerId;
    }

    /** @param providerId Provider 实现 ID */
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    /** @return 配置项映射 */
    public Map<String, String> getConfig() {
        return config;
    }

    /** @param config 配置项映射 */
    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    /** @return 必需操作别名 */
    public String getAlias() {
        return alias;
    }

    /** @param alias 必需操作别名 */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /** @param key 配置键
     * @return 是否包含该配置键 */
    public boolean containsConfigKey(String key) {
        return config != null && config.containsKey(key);
    }

    /** @param key 配置键
     * @return 配置值 */
    public String getConfigValue(String key) {
        return getConfigValue(key, null);
    }

    /** @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值 */
    public String getConfigValue(String key, String defaultValue) {
        if (config == null) {
            return defaultValue;
        }
        String value = config.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
