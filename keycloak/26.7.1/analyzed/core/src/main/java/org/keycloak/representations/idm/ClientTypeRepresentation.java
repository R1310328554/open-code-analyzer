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
 *
 */

package org.keycloak.representations.idm;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 客户端类型定义的 REST 表示，描述类型名称、提供方、父类型及属性配置。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientTypeRepresentation {

    /** 客户端类型名称。 */
    @JsonProperty("name")
    private String name;

    /** 类型提供方 SPI 标识。 */
    @JsonProperty("provider")
    private String provider;

    /** 继承的父类型名称。 */
    @JsonProperty("parent")
    private String parent;

    /** 类型属性配置（属性名 → 适用性与值）。 */
    @JsonProperty("config")
    private Map<String, PropertyConfig> config;

    /** @return 类型名称 */
    public String getName() {
        return name;
    }

    /** @param name 类型名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 类型提供方 ID */
    public String getProvider() {
        return provider;
    }

    /** @param provider 类型提供方 ID */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /** @return 属性配置映射 */
    public Map<String, PropertyConfig> getConfig() {
        return config;
    }

    /** @param config 属性配置映射 */
    public void setConfig(Map<String, PropertyConfig> config) {
        this.config = config;
    }

    /** @return 父类型名称 */
    public String getParent() {
        return parent;
    }

    /** @param parent 父类型名称 */
    public void setParent(String parent) {
        this.parent = parent;
    }

    /** 单个客户端类型属性的配置项。 */
    public static class PropertyConfig {

        /** 该属性是否适用于此类型。 */
        @JsonProperty("applicable")
        private Boolean applicable;

        /** 属性默认值或固定值。 */
        @JsonProperty("value")
        private Object value;

        /** @return 是否适用 */
        public Boolean getApplicable() {
            return applicable;
        }

        /** @param applicable 是否适用 */
        public void setApplicable(Boolean applicable) {
            this.applicable = applicable;
        }


        /** @return 属性值 */
        public Object getValue() {
            return value;
        }

        /** @param value 属性值 */
        public void setValue(Object value) {
            this.value = value;
        }
    }
}
