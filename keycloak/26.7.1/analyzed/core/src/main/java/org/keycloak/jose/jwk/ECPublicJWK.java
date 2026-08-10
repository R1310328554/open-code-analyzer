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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 椭圆曲线（EC）公钥的 JWK 表示，包含曲线名 {@code crv} 与坐标 {@code x}/{@code y}。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ECPublicJWK extends JWK {

    /** JWK 密钥类型常量：椭圆曲线。 */
    public static final String EC = "EC";

    /** JSON 属性名：椭圆曲线标识（如 P-256）。 */
    public static final String CRV = "crv";
    /** JSON 属性名：曲线点 x 坐标（Base64URL）。 */
    public static final String X = "x";
    /** JSON 属性名：曲线点 y 坐标（Base64URL）。 */
    public static final String Y = "y";

    @JsonProperty(CRV)
    private String crv;

    @JsonProperty(X)
    private String x;

    @JsonProperty(Y)
    private String y;

    /** 返回曲线标识。 */
    public String getCrv() {
        return crv;
    }

    /** 设置曲线标识。 */
    public void setCrv(String crv) {
        this.crv = crv;
    }

    /** 返回 x 坐标（Base64URL 编码）。 */
    public String getX() {
        return x;
    }

    /** 设置 x 坐标。 */
    public void setX(String x) {
        this.x = x;
    }

    /** 返回 y 坐标（Base64URL 编码）。 */
    public String getY() {
        return y;
    }

    /** 设置 y 坐标。 */
    public void setY(String y) {
        this.y = y;
    }

    /**
     * 优先从 EC 专用字段读取声明，否则委托 {@link JWK#getOtherClaim}。
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
            case Y:
                claim = getY();
                break;
        }
        if (claim != null) {
            return claimType.cast(claim);
        } else {
            return super.getOtherClaim(claimName, claimType);
        }
    }
}
