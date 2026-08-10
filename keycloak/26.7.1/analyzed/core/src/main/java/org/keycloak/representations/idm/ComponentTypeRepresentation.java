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
import java.util.List;
import java.util.Map;

/**
 * 组件 SPI 类型的 Admin REST 表示，描述可配置属性与扩展元数据。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ComponentTypeRepresentation {
    /** 组件类型 ID（提供方标识）。 */
    protected String id;
    /** 组件类型帮助说明文本。 */
    protected String helpText;
    /** 组件级配置属性定义。 */
    protected List<ConfigPropertyRepresentation> properties;
    /** 客户端相关配置属性定义。 */
    protected List<ConfigPropertyRepresentation> clientProperties;

    /** 来自注解或接口的扩展元数据（如 ImportSynchronization 能力）。 */
    protected Map<String, Object> metadata = new HashMap<>();


    /** @return 组件类型 ID */
    public String getId() {
        return id;
    }

    /** @param id 组件类型 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 帮助文本 */
    public String getHelpText() {
        return helpText;
    }

    /** @param helpText 帮助文本 */
    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    /** @return 组件配置属性列表 */
    public List<ConfigPropertyRepresentation> getProperties() {
        return properties;
    }

    /** @param properties 组件配置属性列表 */
    public void setProperties(List<ConfigPropertyRepresentation> properties) {
        this.properties = properties;
    }

    /** @return 客户端配置属性列表 */
    public List<ConfigPropertyRepresentation> getClientProperties() {
        return clientProperties;
    }

    /** @param clientProperties 客户端配置属性列表 */
    public void setClientProperties(List<ConfigPropertyRepresentation> clientProperties) {
        this.clientProperties = clientProperties;
    }

    /**
     * 组件的扩展元数据，可能来自实现接口或注解。
     * 例如 UserStorageProviderFactory 实现 ImportSynchronization 时的同步能力信息。
     *
     * @return 元数据键值映射
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /** @param metadata 扩展元数据 */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
