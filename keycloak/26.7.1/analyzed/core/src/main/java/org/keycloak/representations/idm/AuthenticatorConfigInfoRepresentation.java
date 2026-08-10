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

/**
 * 认证器工厂的配置元信息表示，描述 Admin Console 中可编辑的配置属性 schema。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class AuthenticatorConfigInfoRepresentation {

    /** 认证器显示名称。 */
    protected String name;
    /** 认证器 SPI 提供方 ID。 */
    protected String providerId;
    /** 配置说明/help 文本。 */
    protected String helpText;

    /** 可配置属性列表及其类型约束。 */
    protected List<ConfigPropertyRepresentation> properties;

    /** @return 认证器名称 */
    public String getName() {
        return name;
    }

    /** @param name 认证器名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 帮助文本 */
    public String getHelpText() {
        return helpText;
    }

    /** @return 提供方 ID */
    public String getProviderId() {
        return providerId;
    }

    /** @param providerId 提供方 ID */
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    /** @param helpText 帮助文本 */
    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    /** @return 配置属性 schema 列表 */
    public List<ConfigPropertyRepresentation> getProperties() {
        return properties;
    }

    /** @param properties 配置属性 schema 列表 */
    public void setProperties(List<ConfigPropertyRepresentation> properties) {
        this.properties = properties;
    }
}
