/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.representations.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * 脚本型 Provider 的聚合描述符，按类型（认证器、策略、Mapper 等）分组列出可用的脚本 Provider 元数据。
 * JSON 序列化时通过 {@link JsonUnwrapped} 将各类型键平铺到顶层对象。
 */
public class ScriptProviderDescriptor {

    /** 认证器脚本 Provider 的 JSON 键名。 */
    public static final String AUTHENTICATORS = "authenticators";
    /** 授权策略脚本 Provider 的 JSON 键名。 */
    public static final String POLICIES = "policies";
    /** OIDC Protocol Mapper 脚本 Provider 的 JSON 键名。 */
    public static final String MAPPERS = "mappers";

    /** SAML Protocol Mapper 脚本 Provider 的 JSON 键名。 */
    public static final String SAML_MAPPERS = "saml-mappers";

    /** 类型键到 {@link ScriptProviderMetadata} 列表的内部映射。 */
    private Map<String, List<ScriptProviderMetadata>> providers = new HashMap<>();

    /** @return 全部脚本 Provider 分组映射（JSON 平铺输出） */
    @JsonUnwrapped
    @JsonGetter
    public Map<String, List<ScriptProviderMetadata>> getProviders() {
        return providers;
    }

    /** @param metadata 认证器脚本列表 */
    @JsonSetter
    public void setAuthenticators(List<ScriptProviderMetadata> metadata) {
        providers.put(AUTHENTICATORS, metadata);
    }

    /** @param metadata 策略脚本列表 */
    @JsonSetter
    public void setPolicies(List<ScriptProviderMetadata> metadata) {
        providers.put(POLICIES, metadata);
    }

    /** @param metadata OIDC Mapper 脚本列表 */
    @JsonSetter
    public void setMappers(List<ScriptProviderMetadata> metadata) {
        providers.put(MAPPERS, metadata);
    }

    /** @param metadata SAML Mapper 脚本列表 */
    @JsonSetter(SAML_MAPPERS)
    public void setSAMLMappers(List<ScriptProviderMetadata> metadata) {
        providers.put(SAML_MAPPERS, metadata);
    }

    /**
     * 注册一个认证器脚本 Provider。
     *
     * @param name     Provider 显示名称
     * @param fileName 脚本文件名
     */
    public void addAuthenticator(String name, String fileName) {
        addProvider(AUTHENTICATORS, name, fileName, null);
    }
    
    /**
     * 向指定类型分组追加脚本 Provider 元数据。
     *
     * @param type        Provider 类型键
     * @param name        显示名称
     * @param fileName    脚本文件名
     * @param description 可选描述
     */
    private void addProvider(String type, String name, String fileName, String description) {
        List<ScriptProviderMetadata> authenticators = providers.get(type);

        if (authenticators == null) {
            authenticators = new ArrayList<>();
            providers.put(type, authenticators);
        }

        authenticators.add(new ScriptProviderMetadata(name, fileName, description));
    }

    /**
     * 注册一个授权策略脚本 Provider。
     *
     * @param name     Provider 显示名称
     * @param fileName 脚本文件名
     */
    public void addPolicy(String name, String fileName) {
        addProvider(POLICIES, name, fileName, null);
    }

    /**
     * 注册一个 OIDC Protocol Mapper 脚本 Provider。
     *
     * @param name     Provider 显示名称
     * @param fileName 脚本文件名
     */
    public void addMapper(String name, String fileName) {
        addProvider(MAPPERS, name, fileName, null);
    }

    /**
     * 注册一个 SAML Protocol Mapper 脚本 Provider。
     *
     * @param name     Provider 显示名称
     * @param fileName 脚本文件名
     */
    public void addSAMLMapper(String name, String fileName) {
        addProvider(SAML_MAPPERS, name, fileName, null);
    }
}
