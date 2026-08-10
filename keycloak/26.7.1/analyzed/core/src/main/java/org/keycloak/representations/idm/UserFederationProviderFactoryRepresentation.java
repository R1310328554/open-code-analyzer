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

import java.util.List;
import java.util.Set;

/**
 * 用户联合提供者工厂（SPI 元数据）的 REST 表示，描述可用的 LDAP 等联合提供者类型及其配置选项。
 *
 * @author <a href="mailto:bburke@redhat.com">Bill Burke</a>
 */
public class UserFederationProviderFactoryRepresentation {

    /** 提供者工厂 SPI ID。 */
    private String id;
    /** 可选配置项集合；TODO: 可配置提供者更灵活，后续可能移除此字段。 */
    private Set<String> options; // TODO:Remove as configurable providers are more flexible?
    /** 帮助说明文本（用于可配置提供者）。 */
    private String helpText; // Used for configurable providers
    /** 可配置属性定义列表（用于可配置提供者）。 */
    private List<ConfigPropertyRepresentation> properties; // Used for configurable providers

    /** @return 提供者工厂 ID */
    public String getId() {
        return id;
    }

    /** @param id 提供者工厂 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 可选配置项集合 */
    public Set<String> getOptions() {
        return options;
    }

    /** @param options 可选配置项集合 */
    public void setOptions(Set<String> options) {
        this.options = options;
    }

    /** @return 帮助说明文本 */
    public String getHelpText() {
        return helpText;
    }

    /** @param helpText 帮助说明文本 */
    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    /** @return 可配置属性定义列表 */
    public List<ConfigPropertyRepresentation> getProperties() {
        return properties;
    }

    /** @param properties 可配置属性定义列表 */
    public void setProperties(List<ConfigPropertyRepresentation> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserFederationProviderFactoryRepresentation that = (UserFederationProviderFactoryRepresentation) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
