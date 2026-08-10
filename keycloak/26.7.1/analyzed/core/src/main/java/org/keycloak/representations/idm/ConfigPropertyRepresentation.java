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
 * 组件或 SPI 配置项的元数据表示，描述单个配置属性的名称、类型、默认值及 UI 展示信息。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ConfigPropertyRepresentation {
    /** 配置项内部名称（键）。 */
    protected String name;
    /** 管理控制台中显示的标签文本。 */
    protected String label;
    /** 帮助说明文本。 */
    protected String helpText;
    /** 配置项类型（如 String、boolean、List 等）。 */
    protected String type;
    /** 默认值。 */
    protected Object defaultValue;
    /** 可选值列表（适用于下拉选择类型）。 */
    protected List<String> options;
    /** 是否为敏感/密钥字段。 */
    protected boolean secret;
    /** 是否为必填项。 */
    protected boolean required;
    /** 是否为只读字段。 */
    private boolean readOnly;

    /** @return 配置项名称 */
    public String getName() {
        return name;
    }

    /** @param name 配置项名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 显示标签 */
    public String getLabel() {
        return label;
    }

    /** @param label 显示标签 */
    public void setLabel(String label) {
        this.label = label;
    }

    /** @return 配置项类型 */
    public String getType() {
        return type;
    }

    /** @param type 配置项类型 */
    public void setType(String type) {
        this.type = type;
    }

    /** @return 默认值 */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /** @param defaultValue 默认值 */
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /** @return 帮助说明文本 */
    public String getHelpText() {
        return helpText;
    }

    /** @param helpText 帮助说明文本 */
    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    /** @return 可选值列表 */
    public List<String> getOptions() {
        return options;
    }

    /** @param options 可选值列表 */
    public void setOptions(List<String> options) {
        this.options = options;
    }

    /** @return 是否为敏感字段 */
    public boolean isSecret() {
        return secret;
    }

    /** @param secret 是否为敏感字段 */
    public void setSecret(boolean secret) {
        this.secret = secret;
    }

    /** @return 是否为必填项 */
    public boolean isRequired() {
        return required;
    }

    /** @param required 是否为必填项 */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /** @param readOnly 是否为只读字段 */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /** @return 是否为只读字段 */
    public boolean isReadOnly() {
        return readOnly;
    }
}
