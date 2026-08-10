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

package org.keycloak.jose.jwk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON Web Key Set（JWKS）容器，对应 RFC 7517 中 {@code keys} 数组结构。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JSONWebKeySet {

    /** JWKS 中的 JWK 列表。 */
    @JsonProperty("keys")
    private JWK[] keys;

    /** 返回 JWK 数组。 */
    public JWK[] getKeys() {
        return keys;
    }

    /** 设置 JWK 数组。 */
    public void setKeys(JWK[] keys) {
        this.keys = keys;
    }

}
