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

package org.keycloak.representations.idm;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 客户端 Scope 的 Admin REST 表示，定义协议、属性与协议映射器。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@JsonIgnoreProperties(ignoreUnknown=true) // 兼容旧版 Admin REST（ClientTemplateRepresentation 字段更丰富）
public class ClientScopeRepresentation {

    /** Scope 内部 UUID。 */
    protected String id;
    /** Scope 名称。 */
    protected String name;
    /** Scope 描述。 */
    protected String description;
    /** 关联协议（如 openid-connect）。 */
    protected String protocol;
    /** Scope 自定义属性。 */
    protected Map<String, String> attributes;

    /** 协议映射器列表。 */
    protected List<ProtocolMapperRepresentation> protocolMappers;

    /** @return Scope ID */
    public String getId() {
        return id;
    }

    /** @param id Scope ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return Scope 名称 */
    public String getName() {
        return name;
    }

    /** @param name Scope 名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return Scope 描述 */
    public String getDescription() {
        return description;
    }

    /** @param description Scope 描述 */
    public void setDescription(String description) {
        this.description = description;
    }


    /** @return 协议映射器列表 */
    public List<ProtocolMapperRepresentation> getProtocolMappers() {
        return protocolMappers;
    }

    /** @param protocolMappers 协议映射器列表 */
    public void setProtocolMappers(List<ProtocolMapperRepresentation> protocolMappers) {
        this.protocolMappers = protocolMappers;
    }

    /** @return 协议标识 */
    public String getProtocol() {
        return protocol;
    }

    /** @param protocol 协议标识 */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /** @return 自定义属性 */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /** @param attributes 自定义属性 */
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    /** 仅基于 ID 比较相等性。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ClientScopeRepresentation)) return false;

        ClientScopeRepresentation that = (ClientScopeRepresentation) o;
        return that.getId().equals(getId());
    }

    /** 基于 ID 计算哈希。 */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
