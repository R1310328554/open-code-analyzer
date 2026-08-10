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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oid4vc.issuance.keybinding.ProofValidator;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jboss.logging.Logger;

/**
 * 凭证颁发者支持的 proof 类型集合（动态 JSON 映射）。
 * <p>键为 proof 类型（如 {@code jwt}），值为 {@link SupportedProofTypeData}；规范见 https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-proof-types。</p>
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProofTypesSupported {

    private static final Logger LOGGER = Logger.getLogger(ProofTypesSupported.class);

    /** proof 类型键到元数据的映射。 */
    protected Map<String, SupportedProofTypeData> supportedProofTypes = new HashMap<>();

    /**
     * 根据已注册的 {@link ProofValidator} 提供者构建支持的 proof 类型映射。
     * @param keycloakSession Keycloak 会话
     * @param keyAttestationsRequired 全局密钥证明要求
     * @param globalSupportedSigningAlgorithms 全局支持的签名算法
     * @return 聚合后的 proof 类型支持信息
     */
    public static ProofTypesSupported parse(KeycloakSession keycloakSession,
                                            KeyAttestationsRequired keyAttestationsRequired,
                                            List<String> globalSupportedSigningAlgorithms) {
        ProofTypesSupported proofTypesSupported = new ProofTypesSupported();
        keycloakSession.getAllProviders(ProofValidator.class).forEach(proofValidator -> {
            String type = proofValidator.getProofType();
            SupportedProofTypeData supportedProofTypeData = new SupportedProofTypeData(globalSupportedSigningAlgorithms,
                    keyAttestationsRequired);
            proofTypesSupported.getSupportedProofTypes().put(type, supportedProofTypeData);
        });
        return proofTypesSupported;
    }

    /**
     * 按给定类型列表过滤，仅保留本实例中已存在的 proof 类型。
     * @param types 要保留的 proof 类型键列表
     * @return 过滤后的新实例
     */
    public ProofTypesSupported filterByTypes(List<String> types) {
        ProofTypesSupported filtered = new ProofTypesSupported();
        if (types == null || types.isEmpty() || supportedProofTypes == null || supportedProofTypes.isEmpty()) {
            return filtered;
        }
        for (String type : types) {
            SupportedProofTypeData data = supportedProofTypes.get(type);
            if (data != null) {
                filtered.supportedProofTypes.put(type, data);
            } else {
                LOGGER.warnf("Ignoring unknown proof type '%s' in credential configuration. Supported types are: %s",
                        type, supportedProofTypes.keySet());
            }
        }
        return filtered;
    }

    /**
     * 从 JSON 字符串反序列化。
     * @param jsonString JSON 文本
     * @return 解析后的实例
     */
    public static ProofTypesSupported fromJsonString(String jsonString) {
        try {
            return JsonSerialization.readValue(jsonString, ProofTypesSupported.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** @return proof 类型到元数据的映射（JSON 任意属性） */
    @JsonAnyGetter
    public Map<String, SupportedProofTypeData> getSupportedProofTypes() {
        return supportedProofTypes;
    }

    /**
     * 反序列化时设置单个 proof 类型条目。
     * @param name proof 类型键
     * @param value 元数据
     * @return 当前实例
     */
    @JsonAnySetter
    public ProofTypesSupported setSupportedProofTypes(String name, SupportedProofTypeData value) {
        supportedProofTypes.put(name, value);
        return this;
    }

    /** @return JSON 字符串表示 */
    public String toJsonString() {
        try {
            return JsonSerialization.writeValueAsString(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof ProofTypesSupported that)) {
            return false;
        }
        return Objects.equals(supportedProofTypes, that.supportedProofTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(supportedProofTypes);
    }
}
