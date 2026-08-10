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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.keycloak.models.oid4vci.CredentialScopeModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 内部处理用的凭证定义（CredentialDefinition）POJO。
 * <p>映射 W3C VCDM 的 {@code @context} 与 {@code type} 字段，由 {@link CredentialScopeModel} 解析并确保包含 {@link #VERIFIABLE_CREDENTIAL_TYPE} 基类型。</p>
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CredentialDefinition {

    /** JSON-LD 上下文 URI 列表（JSON 字段 {@code @context}）。 */
    @JsonProperty("@context")
    private List<String> context;
    /** 凭证类型列表，默认包含 {@link #VERIFIABLE_CREDENTIAL_TYPE}。 */
    private List<String> type = new ArrayList<>();

    /** W3C VCDM 规定的基础凭证类型。 */
    public static final String VERIFIABLE_CREDENTIAL_TYPE = "VerifiableCredential";

    /**
     * 从凭证范围模型解析凭证定义。
     * @param credentialModel 凭证范围模型
     * @return 含上下文与类型的凭证定义
     */
    public static CredentialDefinition parse(CredentialScopeModel credentialModel) {
        List<String> contexts = Optional.of(credentialModel.getVcContexts())
                                        .filter(list -> !list.isEmpty())
                                        .orElseGet(() -> new ArrayList<>(List.of(credentialModel.getName())));
        List<String> types = Optional.ofNullable(credentialModel.getSupportedCredentialTypes())
                                     .filter(list -> !list.isEmpty())
                                     .map(ArrayList::new)
                                     .orElseGet(() -> new ArrayList<>(List.of(credentialModel.getName())));

        // 按 W3C VCDM 规范，始终将 VerifiableCredential 作为基类型置于 type 列表首位。
        if (!types.contains(VERIFIABLE_CREDENTIAL_TYPE)) {
            types.add(0, VERIFIABLE_CREDENTIAL_TYPE);
        }

        return new CredentialDefinition().setContext(contexts)
                                         .setType(types);
    }

    /** @return JSON-LD 上下文 URI 列表 */
    public List<String> getContext() {
        return context;
    }

    /** @param context JSON-LD 上下文 URI 列表 */
    public CredentialDefinition setContext(List<String> context) {
        this.context = context;
        return this;
    }

    /** @return 凭证类型列表 */
    public List<String> getType() {
        return type;
    }

    /** @param type 凭证类型列表 */
    public CredentialDefinition setType(List<String> type) {
        this.type = type;
        return this;
    }
}
