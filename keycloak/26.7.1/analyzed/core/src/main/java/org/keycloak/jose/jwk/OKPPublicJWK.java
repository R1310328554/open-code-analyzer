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

import org.keycloak.crypto.KeyType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OKP（Octet Key Pair，如 Ed25519/Ed448）公钥的 JWK 表示。
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class OKPPublicJWK extends JWK {

    /** JWK 密钥类型：OKP。 */
    public static final String OKP = KeyType.OKP;

    /** JSON 属性名：曲线标识（{@code crv}）。 */
    public static final String CRV = "crv";
    /** JSON 属性名：公钥材料（{@code x}，RFC 8037）。 */
    public static final String X = "x";

    @JsonProperty(CRV)
    private String crv;

    @JsonProperty(X)
    private String x;

    /** 返回曲线标识（如 Ed25519）。 */
    public String getCrv() {
        return crv;
    }

    /** 设置曲线标识。 */
    public void setCrv(String crv) {
        this.crv = crv;
    }

    /** 返回公钥 {@code x} 字段（Base64URL）。 */
    public String getX() {
        return x;
    }

    /** 设置公钥 {@code x} 字段。 */
    public void setX(String x) {
        this.x = x;
    }

    /**
     * 优先从 OKP 专用字段读取声明，否则委托 {@link JWK#getOtherClaim}。
     *
     * @param claimName 声明名
     * @param claimType 期望类型
     * @return 声明值，不存在时返回 {@code null}
     */
    @JsonIgnore
    @Override
    public <T> T getOtherClaim(String claimName, Class<T> claimType) {
        Object claim = null;
        switch (claimName) {
            case CRV:
                claim = getCrv();
                break;
            case X:
                claim = getX();
                break;
        }
        if (claim != null) {
            return claimType.cast(claim);
        } else {
            return super.getOtherClaim(claimName, claimType);
        }
    }

}
