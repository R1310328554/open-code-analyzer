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

package org.keycloak.userprofile;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户配置属性组元数据：名称、展示标题/描述及扩展注解。
 *
 * Configuration of the attribute group.
 *
 * @author <a href="joerg.matysiak@bosch.io">Jörg Matysiak</a>
 */
public class AttributeGroupMetadata {

    /** 属性组名称。 */
    private String name;
    /** UI 展示标题。 */
    private String displayHeader;
    /** UI 展示描述。 */
    private String displayDescription;
    /** 扩展注解映射。 */
    private Map<String, Object> annotations;

    /** 构造属性组元数据。 */
    public AttributeGroupMetadata(String name, String displayHeader, String displayDescription, Map<String, Object> annotations) {
        this.name = name;
        this.displayHeader = displayHeader;
        this.displayDescription = displayDescription;
        if (annotations != null) {
            addAnnotations(annotations);
        }
    }

    /** @return 属性组名称 */
    public String getName() {
        return name;
    }

    /** 设置属性组名称（trim）。
     * @param name 名称
     * @return this */
    public AttributeGroupMetadata setName(String name) {
        this.name = name != null ? name.trim() : null;
        return this;
    }

    /** @return 展示标题 */
    public String getDisplayHeader() {
        return displayHeader;
    }

    /** 设置展示标题。
     * @return this */
    public AttributeGroupMetadata setDisplayHeader(String displayHeader) {
        this.displayHeader = displayHeader;
        return this;
    }

    /** @return 展示描述 */
    public String getDisplayDescription() {
        return displayDescription;
    }

    /** 设置展示描述。
     * @return this */
    public AttributeGroupMetadata setDisplayDescription(String displayDescription) {
        this.displayDescription = displayDescription;
        return this;
    }

    /** @return 注解映射 */
    public Map<String, Object> getAnnotations() {
        return annotations;
    }

    /** 合并注解。
     * @return this */
    public AttributeGroupMetadata addAnnotations(Map<String, Object> annotations) {
        if(annotations != null) {
            if(this.annotations == null) {
                this.annotations = new HashMap<>();
            }

            this.annotations.putAll(annotations);
        }
        return this;
    }

    /** @return 浅拷贝副本 */
    public AttributeGroupMetadata clone() {
        return new AttributeGroupMetadata(name, displayHeader, displayDescription, annotations);
    }
}
