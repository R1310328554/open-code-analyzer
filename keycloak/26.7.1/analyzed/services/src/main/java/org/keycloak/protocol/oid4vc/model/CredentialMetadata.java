/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

import java.util.List;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.oid4vci.CredentialScopeModel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OID4VCI 规范定义的 {@code credential_metadata} 模型。
 * <p>包含已签发凭证的使用与展示相关信息；格式专用机制（如 SD-JWT VC）可覆盖本对象中的默认值。</p>
 *
 * @author <a href="https://github.com/forkimenjeckayang">Forkim Akwichek</a>
 * @see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0-16.html#name-credential-issuer-metadata-p
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CredentialMetadata {

    /** 凭证多语言展示信息（JSON 字段 {@code display}）。 */
    @JsonProperty("display")
    private List<DisplayObject> display;

    /** 声明元数据列表（JSON 字段 {@code claims}）。 */
    @JsonProperty("claims")
    private Claims claims;

    /**
     * 从凭证范围模型解析 credential_metadata。
     * <p>格式专用展示/声明元数据优先；本对象作为默认回退。</p>
     * @param keycloakSession Keycloak 会话
     * @param credentialScope 凭证范围模型
     * @return 解析得到的元数据；无内容时返回 {@code null}
     */
    public static CredentialMetadata parse(KeycloakSession keycloakSession, CredentialScopeModel credentialScope) {
        CredentialMetadata metadata = new CredentialMetadata();

        // 解析格式专用展示元数据（钱包优先采用）
        List<DisplayObject> formatSpecificDisplay = DisplayObject.parse(credentialScope);
        if (formatSpecificDisplay != null && !formatSpecificDisplay.isEmpty()) {
            metadata.setDisplay(formatSpecificDisplay);
        }

        // 解析格式专用声明元数据（钱包优先采用）
        Claims formatSpecificClaims = Claims.parse(keycloakSession, credentialScope);
        if (formatSpecificClaims != null && !formatSpecificClaims.isEmpty()) {
            metadata.setClaims(formatSpecificClaims);
        }

        // 仅在有展示或声明内容时返回元数据对象
        if (metadata.getDisplay() != null || metadata.getClaims() != null) {
            return metadata;
        }

        return null;
    }

    /** @return 多语言展示信息列表 */
    public List<DisplayObject> getDisplay() {
        return display;
    }

    /** @param display 多语言展示信息列表 */
    public CredentialMetadata setDisplay(List<DisplayObject> display) {
        this.display = display;
        return this;
    }

    /** @return 声明元数据列表 */
    public Claims getClaims() {
        return claims;
    }

    /** @param claims 声明元数据列表 */
    public CredentialMetadata setClaims(Claims claims) {
        this.claims = claims;
        return this;
    }
} 
