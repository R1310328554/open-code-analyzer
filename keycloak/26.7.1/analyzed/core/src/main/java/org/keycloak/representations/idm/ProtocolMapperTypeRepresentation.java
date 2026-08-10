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
 * 协议映射器 SPI 类型的元数据表示，描述可配置属性与 UI 分组。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ProtocolMapperTypeRepresentation {
    /** 映射器类型 ID。 */
    protected String id;
    /** 映射器类型显示名称。 */
    protected String name;
    /** Admin Console 中的分类标签。 */
    protected String category;
    /** 面向管理员的帮助说明文本。 */
    protected String helpText;
    /** 在 UI 列表中的排序优先级。 */
    protected int priority;

    /** 该映射器类型可配置的属性定义列表。 */
    protected List<ConfigPropertyRepresentation> properties;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<ConfigPropertyRepresentation> getProperties() {
        return properties;
    }

    public void setProperties(List<ConfigPropertyRepresentation> properties) {
        this.properties = properties;
    }
}
