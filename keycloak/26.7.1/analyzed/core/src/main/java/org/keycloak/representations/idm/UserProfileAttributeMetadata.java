/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
 * 用户配置（User Profile）单个属性的元数据，包含展示名、校验规则、分组及读写约束等信息。
 *
 * @author Vlastimil Elias <velias@redhat.com>
 */
public class UserProfileAttributeMetadata {

    /** 属性内部名称。 */
    private String name;
    /** 属性展示名称。 */
    private String displayName;
    /** 是否为必填属性。 */
    private boolean required;
    /** 是否为只读属性。 */
    private boolean readOnly;
    /** 属性级自定义注解。 */
    private Map<String, Object> annotations;
    /** 属性校验器配置（validatorId → 配置映射）。 */
    private Map<String, Map<String, Object>> validators;
    /** 所属属性分组名称。 */
    private String group;
    /** 是否支持多值。 */
    private boolean multivalued;
    /** 属性默认值。 */
    private String defaultValue;

    public UserProfileAttributeMetadata() {

    }

    public UserProfileAttributeMetadata(String name, String displayName, boolean required, boolean readOnly, String group, Map<String, Object> annotations,
            Map<String, Map<String, Object>> validators, boolean multivalued, String defaultValue) {
        this.name = name;
        this.displayName = displayName;
        this.required = required;
        this.readOnly = readOnly;
        this.annotations = annotations;
        this.validators = validators;
        this.group = group;
        this.multivalued = multivalued;
        this.defaultValue = defaultValue;
    }

    /** @return 属性名称 */
    public String getName() {
        return name;
    }

    /** @return 属性默认值 */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @return 展示名称，可为直接显示的字符串，或 i18n 占位符如 {@code ${i18nkey}}
     */
    public String getDisplayName() {
        return displayName;
    }

    /** @return 是否必填 */
    public boolean isRequired() {
        return required;
    }

    /** @return 是否只读 */
    public boolean isReadOnly() {
        return readOnly;
    }

    /** @return 所属分组名称 */
    public String getGroup() {
        return group;
    }

    /**
     * 获取从 User Profile 配置加载的属性注解信息。
     */
    public Map<String, Object> getAnnotations() {
        return annotations;
    }

    /**
     * 获取应用于该属性的校验器信息。
     *
     * @return 键为 validatorId、值为该校验器配置映射（来自 User Profile 配置）
     */
    public Map<String, Map<String, Object>> getValidators() {
        return validators;
    }

    /** @param multivalued 是否多值 */
    public void setMultivalued(boolean multivalued) {
        this.multivalued = multivalued;
    }

    /** @return 是否多值属性 */
    public boolean isMultivalued() {
        return multivalued;
    }
}
