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

/**
 * 密码策略 SPI 提供方类型的元数据表示，用于 Admin Console 展示可用策略项。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class PasswordPolicyTypeRepresentation {

    /** 策略类型 ID（如 {@code length}、{@code digits}）。 */
    private String id;
    /** 管理控制台显示名称。 */
    private String displayName;
    /** 配置值类型（如 int、boolean）。 */
    private String configType;
    /** 策略默认值。 */
    private String defaultValue;
    /** 是否允许在同一域中配置多个同类型策略实例。 */
    private boolean multipleSupported;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isMultipleSupported() {
        return multipleSupported;
    }

    public void setMultipleSupported(boolean multipleSupported) {
        this.multipleSupported = multipleSupported;
    }
}
