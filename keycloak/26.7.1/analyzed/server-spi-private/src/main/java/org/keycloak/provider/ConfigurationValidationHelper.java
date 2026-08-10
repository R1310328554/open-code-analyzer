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

package org.keycloak.provider;

import java.util.List;

import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.utils.StringUtil;

/**
 * 组件配置校验辅助类：对 {@link ComponentModel} 配置项进行类型、必填与选项校验。
 * <p>采用链式调用，校验失败时抛出 {@link ComponentValidationException}。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ConfigurationValidationHelper {

    /** 待校验的组件模型。 */
    private ComponentModel model;

    private ConfigurationValidationHelper(ComponentModel model) {
        this.model = model;
    }

    /** 创建校验辅助实例。 */
    public static ConfigurationValidationHelper check(ComponentModel model) {
        return new ConfigurationValidationHelper(model);
    }

    /** 校验整型配置项。 */
    public ConfigurationValidationHelper checkInt(ProviderConfigProperty property, boolean required) throws ComponentValidationException {
        return checkInt(property.getName(), property.getLabel(), required);
    }

    /** 校验枚举列表配置项（值须在 {@link ProviderConfigProperty#getOptions()} 中）。 */
    public ConfigurationValidationHelper checkList(ProviderConfigProperty property, boolean required) throws ComponentValidationException {
        checkSingle(property.getName(), property.getLabel(), required);

        String value = model.getConfig().getFirst(property.getName());
        if (value != null && !property.getOptions().contains(value)) {
            String options = StringUtil.joinValuesWithLogicalCondition("or", property.getOptions());
            throw new ComponentValidationException("''{0}'' should be {1}", property.getLabel(), options);
        }

        return this;
    }

    /** 按 key/label 校验整型配置项。 */
    public ConfigurationValidationHelper checkInt(String key, String label, boolean required) throws ComponentValidationException {
        checkSingle(key, label, required);

        String val = model.getConfig().getFirst(key);
        if (val != null) {
            try {
                Integer.parseInt(val);
            } catch (NumberFormatException e) {
                throw new ComponentValidationException("''{0}'' should be a number", label);
            }
        }

        return this;
    }

    /** 校验长整型配置项。 */
    public ConfigurationValidationHelper checkLong(ProviderConfigProperty property, boolean required) throws ComponentValidationException {
        return checkLong(property.getName(), property.getLabel(), required);
    }

    /** 按 key/label 校验长整型配置项。 */
    public ConfigurationValidationHelper checkLong(String key, String label, boolean required) throws ComponentValidationException {
        checkSingle(key, label, required);

        String val = model.getConfig().getFirst(key);
        if (val != null) {
            try {
                Long.parseLong(val);
            } catch (NumberFormatException e) {
                throw new ComponentValidationException("''{0}'' should be a number", label);
            }
        }

        return this;
    }

    /** 校验配置项为单值。 */
    public ConfigurationValidationHelper checkSingle(ProviderConfigProperty property, boolean required) throws ComponentValidationException {
        return checkSingle(property.getName(), property.getLabel(), required);
    }

    /** 按 key/label 校验配置项为单值，可选必填。 */
    public ConfigurationValidationHelper checkSingle(String key, String label, boolean required) throws ComponentValidationException {
        if (model.getConfig().containsKey(key) && model.getConfig().get(key).size() > 1) {
            throw new ComponentValidationException("''{0}'' should be a single entry", label);
        }

        if (required) {
            checkRequired(key, label);
        }

        return this;
    }

    /** 校验配置项必填。 */
    public ConfigurationValidationHelper checkRequired(ProviderConfigProperty property) throws ComponentValidationException {
        return checkRequired(property.getName(), property.getLabel());
    }

    /** 按 key/label 校验配置项必填。 */
    public ConfigurationValidationHelper checkRequired(String key, String label) throws ComponentValidationException {
        List<String> values = model.getConfig().get(key);
        if (values == null) {
            throw new ComponentValidationException("''{0}'' is required", label);
        }

        return this;
    }

    /** 校验布尔配置项（值为 {@code true} 或 {@code false}）。 */
    public ConfigurationValidationHelper checkBoolean(ProviderConfigProperty property, boolean required) throws ComponentValidationException {
        return checkBoolean(property.getName(), property.getLabel(), required);
    }

    /** 按 key/label 校验布尔配置项。 */
    public ConfigurationValidationHelper checkBoolean(String key, String label, boolean required) {
        checkSingle(key, label, required);

        String val = model.getConfig().getFirst(key);
        if (val != null && !(val.equals("true") || val.equals("false"))) {
            throw new ComponentValidationException("''{0}'' should be ''true'' or ''false''", label);
        }

        return this;
    }
}
