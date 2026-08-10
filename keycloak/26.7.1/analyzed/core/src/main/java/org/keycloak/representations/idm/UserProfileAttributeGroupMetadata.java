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
 * 用户配置（User Profile）属性分组的元数据，描述分组名称、展示标题及附加注解。
 */
public class UserProfileAttributeGroupMetadata {

    /** 分组内部名称标识。 */
    private String name;
    /** 分组展示标题（UI 头部文本）。 */
    private String displayHeader;
    /** 分组展示描述（UI 说明文本）。 */
    private String displayDescription;
    /** 分组级自定义注解键值对。 */
    private Map<String, Object> annotations;

    public UserProfileAttributeGroupMetadata() {
    }

    public UserProfileAttributeGroupMetadata(String name, String displayHeader, String displayDescription, Map<String, Object> annotations) {
        this.name = name;
        this.displayHeader = displayHeader;
        this.displayDescription = displayDescription;
        this.annotations = annotations;
    }

    /** @return 分组名称 */
    public String getName() {
        return name;
    }

    /** @return 分组展示标题 */
    public String getDisplayHeader() {
        return displayHeader;
    }


    /** @return 分组展示描述 */
    public String getDisplayDescription() {
        return displayDescription;
    }

    /** @return 分组注解映射 */
    public Map<String, Object> getAnnotations() {
        return annotations;
    }
}
