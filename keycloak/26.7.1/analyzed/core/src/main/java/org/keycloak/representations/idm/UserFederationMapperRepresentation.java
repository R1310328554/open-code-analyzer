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

import java.util.Map;

/**
 * 用户联合映射器（User Federation Mapper）的 REST 表示，用于 LDAP 等外部用户源与 Keycloak 用户属性/角色的映射配置。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class UserFederationMapperRepresentation {

    /** 映射器持久化 ID。 */
    protected String id;
    /** 映射器显示名称。 */
    protected String name;
    /** 所属用户联合提供者的显示名称。 */
    protected String federationProviderDisplayName;
    /** 映射器类型 SPI 标识。 */
    protected String federationMapperType;
    /** 映射器配置键值对。 */
    protected Map<String, String> config;

    /** @return 映射器 ID */
    public String getId() {
        return id;
    }

    /** @param id 映射器 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 映射器名称 */
    public String getName() {
        return name;
    }

    /** @param name 映射器名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 联合提供者显示名称 */
    public String getFederationProviderDisplayName() {
        return federationProviderDisplayName;
    }

    /** @param federationProviderDisplayName 联合提供者显示名称 */
    public void setFederationProviderDisplayName(String federationProviderDisplayName) {
        this.federationProviderDisplayName = federationProviderDisplayName;
    }

    /** @return 映射器类型 ID */
    public String getFederationMapperType() {
        return federationMapperType;
    }

    /** @param federationMapperType 映射器类型 ID */
    public void setFederationMapperType(String federationMapperType) {
        this.federationMapperType = federationMapperType;
    }

    /** @return 映射器配置 */
    public Map<String, String> getConfig() {
        return config;
    }

    /** @param config 映射器配置 */
    public void setConfig(Map<String, String> config) {
        this.config = config;
    }
}
