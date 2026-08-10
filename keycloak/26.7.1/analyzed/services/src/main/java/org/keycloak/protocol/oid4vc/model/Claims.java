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
import java.util.ArrayList;
import java.util.Optional;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.util.JsonSerialization;

/**
 * 可验证凭证声明元数据列表。
 * <p>继承 {@link java.util.ArrayList}{@code <Claim>}，聚合凭证范围内各协议映射器解析出的 claim 元数据。</p>
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
public class Claims extends ArrayList<Claim> {

    /**
     * 从凭证范围模型解析全部 claim 元数据。
     * @param keycloakSession Keycloak 会话
     * @param credentialScope 凭证范围模型
     * @return 解析得到的声明列表
     */
    public static Claims parse(KeycloakSession keycloakSession, CredentialScopeModel credentialScope) {
        Claims claims = new Claims();
        credentialScope.getOid4vcProtocolMappersStream().forEach(protocolMapper -> {
            Optional<Claim> claim = Claim.parse(keycloakSession, credentialScope.getFormat(), protocolMapper);
            claim.ifPresent(claims::add);
        });
        return claims;
    }

    /** @return 本列表的 JSON 字符串表示 */
    public String toJsonString(){
        try {
            return JsonSerialization.writeValueAsString(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从 JSON 字符串反序列化为 {@link Claims}。
     * @param jsonString JSON 文本
     * @return 反序列化后的声明列表
     */
    public static Claims fromJsonString(String jsonString){
        try {
            return JsonSerialization.readValue(jsonString, Claims.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
