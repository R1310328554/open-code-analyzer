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

package org.keycloak.protocol.oid4vc.model.vcdm;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 链接数据证明（Linked Data Proof）POJO。
 * <p>对应 W3C 可验证凭证数据模型中的 proof 结构。</p>
 * {@see https://www.w3.org/TR/vc-data-model}
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LdProof {

    /** proof 类型（如 Ed25519Signature2020）。 */
    private String type;
    /** 创建时间。 */
    private Date created;
    /** proof 用途（如 assertionMethod）。 */
    private String proofPurpose;
    /** 验证方法标识。 */
    private String verificationMethod;
    /** 独立 proof 值（multibase 等）。 */
    private String proofValue;
    /** JWS 紧凑序列化 proof。 */
    private String jws;

    /** 附加 JSON-LD 属性。 */
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /** @return 附加属性 */
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    /** @param name 属性名
     * @param property 属性值 */
    @JsonAnySetter
    public void setAdditionalProperties(String name, Object property) {
        additionalProperties.put(name, property);
    }

    /** @return proof 类型 */
    public String getType() {
        return type;
    }

    /** @param type proof 类型 */
    public void setType(String type) {
        this.type = type;
    }

    /** @return 创建时间 */
    public Date getCreated() {
        return created;
    }

    /** @param created 创建时间 */
    public void setCreated(Date created) {
        this.created = created;
    }

    /** @return proof 用途 */
    public String getProofPurpose() {
        return proofPurpose;
    }

    /** @param proofPurpose proof 用途 */
    public void setProofPurpose(String proofPurpose) {
        this.proofPurpose = proofPurpose;
    }

    /** @return 验证方法 */
    public String getVerificationMethod() {
        return verificationMethod;
    }

    /** @param verificationMethod 验证方法 */
    public void setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    /** @return proof 值 */
    public String getProofValue() {
        return proofValue;
    }

    /** @param proofValue proof 值 */
    public void setProofValue(String proofValue) {
        this.proofValue = proofValue;
    }

    /** @return JWS 字符串 */
    public String getJws() {
        return jws;
    }

    /** @param jws JWS 字符串 */
    public void setJws(String jws) {
        this.jws = jws;
    }
}