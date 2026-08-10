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

import java.util.Map;

/**
 * 必需操作提供者（Required Action Provider）的 REST 表示，用于管理登录后需用户完成的操作（如更新密码、验证邮箱等）。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RequiredActionProviderRepresentation {

    /** 必需操作的别名标识。 */
    private String alias;
    /** 用于界面展示的友好名称。 */
    private String name;
    /** 提供者 SPI 标识。 */
    private String providerId;
    /** 是否启用该必需操作。 */
    private boolean enabled;
    /** 是否作为新用户的默认必需操作。 */
    private boolean defaultAction;
    /** 执行优先级（数值越小越优先）。 */
    private int priority;
    /** 提供者配置键值对。 */
    private Map<String, String> config;

    /** @return 必需操作别名 */
    public String getAlias() {
        return alias;
    }

    /** @param alias 必需操作别名 */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * 用于界面展示的名称。历史原因 alias 与 name 可能不同：旧代码引用枚举值，
     * 管理控制台会为每个枚举生成友好名称。
     *
     * @return 展示名称
     */
    public String getName() {
        return name;
    }

    /** @param name 展示名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 是否启用 */
    public boolean isEnabled() {
        return enabled;
    }

    /** @param enabled 是否启用 */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** @return 是否为默认必需操作 */
    public boolean isDefaultAction() {
        return defaultAction;
    }

    /** @param defaultAction 是否为默认必需操作 */
    public void setDefaultAction(boolean defaultAction) {
        this.defaultAction = defaultAction;
    }

    /** @return 提供者 ID */
    public String getProviderId() {
        return providerId;
    }

    /** @param providerId 提供者 ID */
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    /** @return 执行优先级 */
    public int getPriority() {
        return priority;
    }

    /** @param priority 执行优先级 */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /** @return 提供者配置 */
    public Map<String, String> getConfig() {
        return config;
    }

    /** @param config 提供者配置 */
    public void setConfig(Map<String, String> config) {
        this.config = config;
    }
}
