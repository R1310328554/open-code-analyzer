/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AKP（Asymmetric Key Pair，如 ML-DSA）公钥的 JWK 表示，使用 {@code pub} 字段承载编码公钥。
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class AKPPublicJWK extends JWK {

    /** JWK 中 AKP 公钥材料的 JSON 属性名。 */
    public static final String PUB = "pub";

    /** Base64URL 编码的公钥（不含 X.509 前缀）。 */
    @JsonProperty(PUB)
    private String pub;

    public String getPub() {
        return pub;
    }

    public void setPub(String pub) {
        this.pub = pub;
    }

    /** 优先从 {@code pub} 读取扩展声明，否则委托 {@link JWK#getOtherClaim}。 */
    @JsonIgnore
    @Override
    public <T> T getOtherClaim(String claimName, Class<T> claimType) {
        Object claim = null;
        if (claimName.equals(PUB)) {
            claim = getPub();
        }
        if (claim != null) {
            return claimType.cast(claim);
        } else {
            return super.getOtherClaim(claimName, claimType);
        }
    }

}
