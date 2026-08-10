/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.oid4vc.model;

import java.util.Objects;

import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 声明（claim）的多语言展示描述。
 * <p>包含本地化名称与 locale，用于凭证元数据中向钱包展示人类可读标签。</p>
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClaimDisplay {
    /** 本地化展示名称（JSON 字段 {@code name}）。 */
    @JsonProperty("name")
    private String name;

    /** BCP 47 语言区域标识（JSON 字段 {@code locale}）。 */
    @JsonProperty("locale")
    private String locale;

    /** @return 本地化展示名称 */
    public String getName() {
        return name;
    }

    /** @param name 本地化展示名称 */
    public ClaimDisplay setName(String name) {
        this.name = name;
        return this;
    }

    /** @return 语言区域标识 */
    public String getLocale() {
        return locale;
    }

    /** @param locale 语言区域标识 */
    public ClaimDisplay setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    @Override
    public final boolean equals(Object object) {

        if (!(object instanceof ClaimDisplay that)) {
            return false;
        }

        return Objects.equals(name, that.name) && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(locale);
        return result;
    }

    @Override
    public String toString() {
        try {
            return JsonSerialization.mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
