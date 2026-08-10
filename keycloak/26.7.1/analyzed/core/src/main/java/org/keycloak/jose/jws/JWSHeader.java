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

package org.keycloak.jose.jws;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.jose.JOSEHeader;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JWS 头部（JOSE Header）的 Java 表示，实现 {@link JOSEHeader}。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JWSHeader implements JOSEHeader {
    /** 签名算法。 */
    @JsonProperty("alg")
    private Algorithm algorithm;

    /** 类型（{@code typ}，如 JWT）。 */
    @JsonProperty("typ")
    private String type;

    /** 内容类型（{@code cty}）。 */
    @JsonProperty("cty")
    private String contentType;

    /** 密钥 ID（{@code kid}）。 */
    @JsonProperty("kid")
    private String keyId;

    /** 嵌入的 JWK（{@code jwk}）。 */
    @JsonProperty("jwk")
    private JWK key;

    /** X.509 证书链（Base64 DER，{@code x5c}）。 */
    @JsonProperty("x5c")
    private List<String> x5c;

    /** 自定义扩展声明。 */
    private Map<String, Object> otherClaims = new HashMap<>();

    /** 无参构造，供 Jackson 反序列化。 */
    public JWSHeader() {
    }

    /**
     * 构造带算法、类型与内容类型的头部。
     *
     * @param algorithm 签名算法
     * @param type 类型
     * @param contentType 内容类型
     */
    public JWSHeader(Algorithm algorithm, String type, String contentType) {
        this.algorithm = algorithm;
        this.type = type;
        this.contentType = contentType;
    }

    /**
     * 构造带算法、类型、密钥 ID 与嵌入 JWK 的头部。
     *
     * @param algorithm 签名算法
     * @param type 类型
     * @param keyId 密钥 ID
     * @param key 嵌入 JWK
     */
    public JWSHeader(Algorithm algorithm, String type, String keyId, JWK key) {
        this.algorithm = algorithm;
        this.type = type;
        this.keyId = keyId;
        this.key = key;
    }

    /** 返回签名算法枚举。 */
    public Algorithm getAlgorithm() {
        return algorithm;
    }

    /** 设置签名算法。 */
    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    /** 返回原始算法名字符串（{@link Algorithm#name()}）。 */
    @JsonIgnore
    @Override
    public String getRawAlgorithm() {
        return getAlgorithm().name();
    }

    /** 返回类型（{@code typ}）。 */
    public String getType() {
        return type;
    }

    /** 设置类型。 */
    public void setType(String type) {
        this.type = type;
    }

    /** 返回内容类型（{@code cty}）。 */
    public String getContentType() {
        return contentType;
    }

    /** 设置内容类型。 */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /** 返回密钥 ID。 */
    public String getKeyId() {
        return keyId;
    }

    /** 设置密钥 ID。 */
    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    /** 返回嵌入 JWK。 */
    public JWK getKey() {
        return key;
    }

    /** 设置嵌入 JWK。 */
    public void setKey(JWK key) {
        this.key = key;
    }

    /** 返回 X.509 证书链（Base64 字符串列表）。 */
    public List<String> getX5c() {
        return x5c;
    }

    /** 设置 X.509 证书链。 */
    public void setX5c(List<String> x5c) {
        this.x5c = x5c;
    }

    /** 追加一条 Base64 编码的证书 DER。 */
    public void addX5c(String x5c) {
        if (this.x5c == null) {
            this.x5c = new ArrayList<>();
        }
        this.x5c.add(x5c);
    }

    /** 追加证书并将其 DER 编码为 Base64 写入 {@code x5c}。 */
    public void addX5c(Certificate x5c) {
        if (this.x5c == null) {
            this.x5c = new ArrayList<>();
        }
        try {
            this.x5c.add(Base64.getEncoder().encodeToString(x5c.getEncoded()));
        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 头部中的自定义扩展声明（自定义实现或认证服务器可能写入）。
     */
    @JsonAnyGetter
    public Map<String, Object> getOtherClaims() {
        return otherClaims;
    }

    /** 写入单个扩展声明。 */
    @JsonAnySetter
    public void setOtherClaims(String name, Object value) {
        otherClaims.put(name, value);
    }

    /** 序列化为 JSON 字符串。 */
    public String toString() {
        try {
            return JsonSerialization.writeValueAsString(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
