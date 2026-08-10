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
package org.keycloak.representations.idm;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 安全配置文件（Security Profile）的默认配置表示。当前包含名称及指向全局默认客户端 Profile 与 Policy 的引用。
 *
 * @author rmartinc
 */
public class SecurityProfileConfiguration {

    /** 安全配置文件名称。 */
    private String name;
    /** 客户端 Profile 配置文件路径或引用。 */
    @JsonProperty("client-profiles")
    private String clientProfiles;
    /** 客户端 Policy 配置文件路径或引用。 */
    @JsonProperty("client-policies")
    private String clientPolicies;
    /** 解析后的默认客户端 Profile 列表（不参与 JSON 序列化）。 */
    @JsonIgnore
    private List<ClientProfileRepresentation> defaultClientProfiles;
    /** 解析后的默认客户端 Policy 列表（不参与 JSON 序列化）。 */
    @JsonIgnore
    private List<ClientPolicyRepresentation> defaultClientPolicies;

    /** @return 安全配置文件名称 */
    public String getName() {
        return name;
    }

    /** @param name 安全配置文件名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 客户端 Profile 引用 */
    public String getClientProfiles() {
        return clientProfiles;
    }

    /** @param clientProfiles 客户端 Profile 引用 */
    public void setClientProfiles(String clientProfiles) {
        this.clientProfiles = clientProfiles;
    }

    /** @return 客户端 Policy 引用 */
    public String getClientPolicies() {
        return clientPolicies;
    }

    /** @param clientPolicies 客户端 Policy 引用 */
    public void setClientPolicies(String clientPolicies) {
        this.clientPolicies = clientPolicies;
    }

    /** @return 默认客户端 Profile 列表 */
    public List<ClientProfileRepresentation> getDefaultClientProfiles() {
        return defaultClientProfiles;
    }

    /** @param defaultClientProfiles 默认客户端 Profile 列表 */
    public void setDefaultClientProfiles(List<ClientProfileRepresentation> defaultClientProfiles) {
        this.defaultClientProfiles = defaultClientProfiles;
    }

    /** @return 默认客户端 Policy 列表 */
    public List<ClientPolicyRepresentation> getDefaultClientPolicies() {
        return defaultClientPolicies;
    }

    /** @param defaultClientPolicies 默认客户端 Policy 列表 */
    public void setDefaultClientPolicies(List<ClientPolicyRepresentation> defaultClientPolicies) {
        this.defaultClientPolicies = defaultClientPolicies;
    }
}
