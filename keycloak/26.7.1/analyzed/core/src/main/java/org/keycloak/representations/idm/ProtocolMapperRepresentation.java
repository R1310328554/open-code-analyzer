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

import java.util.HashMap;
import java.util.Map;

/**
 * 协议映射器（Protocol Mapper）的 REST 表示，将用户/客户端属性映射到令牌或断言声明。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ProtocolMapperRepresentation {
    /** 映射器内部 UUID。 */
    protected String id;
    /** 映射器显示名称。 */
    protected String name;
    /** 所属协议（如 openid-connect、saml）。 */
    protected String protocol;
    /** 映射器 SPI 提供方 ID。 */
    protected String protocolMapper;

    /** 已弃用：是否需要用户同意（仅向后兼容）。 */
    @Deprecated // backwards compatibility only
    protected boolean consentRequired;

    /** 已弃用：同意页面展示文本（仅向后兼容）。 */
    @Deprecated // backwards compatibility only
    protected String consentText;
    /** 映射器提供方特定的键值配置。 */
    protected Map<String, String> config = new HashMap<>();


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocolMapper() {
        return protocolMapper;
    }

    public void setProtocolMapper(String protocolMapper) {
        this.protocolMapper = protocolMapper;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    @Deprecated
    public boolean isConsentRequired() {
        return consentRequired;
    }

    @Deprecated
    public String getConsentText() {
        return consentText;
    }

}
