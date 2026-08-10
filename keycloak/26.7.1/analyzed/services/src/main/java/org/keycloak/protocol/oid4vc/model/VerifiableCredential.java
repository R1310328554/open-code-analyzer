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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * 内部处理用的可验证凭证（Verifiable Credential）POJO。
 * <p>映射 W3C VC 数据模型的核心字段及扩展属性。</p>
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifiableCredential {

    /** W3C VC 1.1 @context 常量。 */
    public static final String VC_CONTEXT_V1 = "https://www.w3.org/ns/credentials/v1";
    /** W3C VC 2.0 @context 常量。 */
    public static final String VC_CONTEXT_V2 = "https://www.w3.org/ns/credentials/v2";

    /** JSON-LD @context 有序集合，首项通常为 W3C credentials 命名空间。 */
    @JsonProperty("@context")
    private List<String> context = new ArrayList<>(List.of(VC_CONTEXT_V1));
    private List<String> type = new ArrayList<>();

    /** 签发者：URI 字符串或含 id 字段的对象。 */
    @JsonDeserialize(using = IssuerDeserializer.class)
    private Object issuer;
    /** 签发时间。 */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant issuanceDate;
    /** 凭证标识 URI。 */
    private URI id;
    /** 过期时间。 */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant expirationDate;
    /** 凭证主体（subject）及声明。 */
    private CredentialSubject credentialSubject = new CredentialSubject();
    /** 未显式建模的附加 JSON 属性。 */
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /** @return 附加属性映射 */
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    /** @param additionalProperties 附加属性
     * @return 当前实例 */
    public VerifiableCredential setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
        return this;
    }

    /** @param name 属性名
     * @param property 属性值
     * @return 当前实例 */
    @JsonAnySetter
    public VerifiableCredential setAdditionalProperties(String name, Object property) {
        additionalProperties.put(name, property);
        return this;
    }

    /** @return @context 列表 */
    public List<String> getContext() {
        return context;
    }

    /** @param context @context 列表
     * @return 当前实例 */
    public VerifiableCredential setContext(List<String> context) {
        this.context = context;
        return this;
    }

    /** @return 凭证 type 列表 */
    public List<String> getType() {
        return type;
    }

    /** @param type type 列表
     * @return 当前实例 */
    public VerifiableCredential setType(List<String> type) {
        this.type = type;
        return this;
    }

    /** @return 签发者（URI 或 Map） */
    public Object getIssuer() {
        return issuer;
    }

    /**
     * 设置签发者并校验 id 为合法 URI。
     *
     * @param issuer URI 或含 id 的 Map
     * @return 当前实例
     */
        if (issuer instanceof Map<?, ?> issuerMap) {

            Optional.ofNullable(issuerMap).ifPresent(map -> {
                String id = (String) Optional.ofNullable(map.get("id"))
                                             .orElseThrow(() -> new IllegalArgumentException(
                                                     "id is a required field for issuer"));
                try {
                    // id 必须为 URL（W3C VC 数据模型）
                    new URI(id);
                } catch (URISyntaxException e) {
                    throw new IllegalStateException("id must be a valid URI", e);
                }
            });
            this.issuer = issuerMap;
        }
        else {
            try {
                this.issuer = new URI(String.valueOf(issuer));
            } catch (URISyntaxException e) {
                throw new IllegalStateException("id must be a valid URI", e);
            }
        }
        return this;
    }

    /** @param issuer 含 id 的签发者 Map
     * @return 当前实例 */
    public VerifiableCredential setIssuerMap(Map<String, String> issuer) {
        this.issuer = issuer;
        return this;
    }

    /** @return 签发时间 */
    public Instant getIssuanceDate() {
        return issuanceDate;
    }

    /** @param issuanceDate 签发时间
     * @return 当前实例 */
    public VerifiableCredential setIssuanceDate(Instant issuanceDate) {
        this.issuanceDate = issuanceDate;
        return this;
    }

    /** @return 凭证 ID URI */
    public URI getId() {
        return id;
    }

    /** @param id 凭证 ID
     * @return 当前实例 */
    public VerifiableCredential setId(URI id) {
        this.id = id;
        return this;
    }

    /** @return 过期时间 */
    public Instant getExpirationDate() {
        return expirationDate;
    }

    /** @param expirationDate 过期时间
     * @return 当前实例 */
    public VerifiableCredential setExpirationDate(Instant expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    /** @return 凭证主体 */
    public CredentialSubject getCredentialSubject() {
        return credentialSubject;
    }

    /** @param credentialSubject 凭证主体
     * @return 当前实例 */
    public VerifiableCredential setCredentialSubject(CredentialSubject credentialSubject) {
        this.credentialSubject = credentialSubject;
        return this;
    }

    /** 将 issuer JSON 反序列化为 URI 或 Map。 */
    public static class IssuerDeserializer extends JsonDeserializer<Object> {

        /** @param p JSON 解析器
         * @param ctxt 反序列化上下文
         * @return URI 或 Map */
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.readValueAsTree();
            if (node instanceof TextNode) {
                try {
                    return new URI(node.textValue());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            else if (node instanceof ObjectNode objectNode) {
                return JsonSerialization.mapper.convertValue(objectNode, Map.class);
            }
            else {
                throw new IllegalArgumentException("Issuer must be a valid URI or a JSON object");
            }
        }
    }
}
