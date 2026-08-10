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

import java.util.List;
import java.util.Optional;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.oid4vci.Oid4vcProtocolMapperModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oid4vc.issuance.mappers.OID4VCMapper;
import org.keycloak.util.JsonSerialization;
import org.keycloak.utils.StringUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 可验证凭证中单个声明（claim）的元数据模型。
 * <p>描述声明路径、是否必填及多语言展示信息，对应 OID4VCI 凭证元数据附录 A.2.2。详见 <a href="https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#appendix-A.2.2">openid-4-verifiable-credential-issuance-1_0.html#appendix-A.2.2</a>。</p>
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Claim {

    /** 声明名称（点分路径拼接），不参与底层 JSON 序列化。 */
    @JsonIgnore
    private String name;

    /** 声明在凭证中的 JSON 路径（JSON 字段 {@code path}）。 */
    @JsonProperty("path")
    private List<String> path;

    /** 钱包是否必须请求该声明（JSON 字段 {@code mandatory}）。 */
    @JsonProperty("mandatory")
    private Boolean mandatory;

    /** 多语言展示信息列表（JSON 字段 {@code display}）。 */
    @JsonProperty("display")
    private List<ClaimDisplay> display;

    /**
     * 从 OID4VC 协议映射器解析 claim 元数据。
     * @param keycloakSession Keycloak 会话
     * @param credentialFormat 凭证格式标识
     * @param protocolMapper 协议映射器模型
     * @return 解析成功返回 {@link Claim}，否则为空
     */
    public static Optional<Claim> parse(KeycloakSession keycloakSession,
                                        String credentialFormat,
                                        Oid4vcProtocolMapperModel protocolMapper) {
        try {
            Claim claim = new Claim();
            ProtocolMapper protocolMapperImpl = keycloakSession.getProvider(ProtocolMapper.class,
                                                                            protocolMapper.getProtocolMapper());
            if (!(protocolMapperImpl instanceof OID4VCMapper)) {
                return Optional.empty();
            }
            OID4VCMapper mapper = (OID4VCMapper) protocolMapperImpl;
            mapper.setMapperModel(protocolMapper, credentialFormat);

            if (!mapper.includeInMetadata()) {
                return Optional.empty();
            }

            List<String> attributePath = mapper.getMetadataAttributePath();
            if (attributePath == null || attributePath.isEmpty()) {
                return Optional.empty();
            }

            claim.setName(String.join(".", attributePath));

            claim.setPath(attributePath);
            claim.setMandatory(protocolMapper.isMandatory());

            String displayString = protocolMapper.getDisplay();
            if (StringUtil.isNotBlank(displayString)) {
                TypeReference<List<ClaimDisplay>> typeReference = new TypeReference<>() {};
                List<ClaimDisplay> claimDisplayList = JsonSerialization.mapper.readValue(displayString, typeReference);
                claim.setDisplay(claimDisplayList);
            }

            return Optional.of(claim);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /** @return 声明名称（点分路径） */
    public String getName() {
        return name;
    }

    /** @param name 声明名称 */
    public Claim setName(String name) {
        this.name = name;
        return this;
    }

    /** @return 声明 JSON 路径段列表 */
    public List<String> getPath() {
        return path;
    }

    /** @param path 声明 JSON 路径段列表 */
    public Claim setPath(List<String> path) {
        this.path = path;
        return this;
    }

    /** @return 是否必填（未设置时默认为 {@code false}） */
    public boolean isMandatory() {
        return Optional.ofNullable(mandatory).orElse(false);
    }

    /** @param mandatory 是否必填 */
    public Claim setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
        return this;
    }

    /** @return 多语言展示信息列表 */
    public List<ClaimDisplay> getDisplay() {
        return display;
    }

    /** @param display 多语言展示信息列表 */
    public Claim setDisplay(List<ClaimDisplay> display) {
        this.display = display;
        return this;
    }
}
