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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 内部处理用的凭证主体（Credential Subject）POJO。
 * <p>通过 {@link JsonAnyGetter}/{@link JsonAnySetter} 动态承载任意声明键值对。</p>
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CredentialSubject {

    /** 凭证主体声明映射（键为声明名，值为声明内容）。 */
    @JsonIgnore
    private Map<String, Object> claims = new HashMap<>();

    /** @return 全部主体声明 */
    @JsonAnyGetter
    public Map<String, Object> getClaims() {
        return claims;
    }

    /**
     * 设置单个声明（Jackson 反序列化入口）。
     *
     * @param name  声明名
     * @param claim 声明值
     */
        claims.put(name, claim);
    }

    /** @param claims 完整声明映射 */
    public CredentialSubject setClaims(Map<String, Object> claims) {
        this.claims = claims;
        return this;
    }
}