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
 * 身份提供者映射器（Identity Provider Mapper）的 REST 表示，用于 IdP 登录时的属性/角色映射。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class IdentityProviderMapperRepresentation {
    /** 映射器持久化 ID。 */
    protected String id;
    /** 映射器显示名称。 */
    protected String name;
    /** 所属身份提供者的别名。 */
    protected String identityProviderAlias;
    /** 映射器类型 ID（SPI 标识）。 */
    protected String identityProviderMapper;
    /** 映射器配置键值对。 */
    protected Map<String, String> config = new HashMap<>();


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

    /** @return 身份提供者别名 */
    public String getIdentityProviderAlias() {
        return identityProviderAlias;
    }

    /** @param identityProviderAlias 身份提供者别名 */
    public void setIdentityProviderAlias(String identityProviderAlias) {
        this.identityProviderAlias = identityProviderAlias;
    }

    /** @return 映射器类型 ID */
    public String getIdentityProviderMapper() {
        return identityProviderMapper;
    }

    /** @param identityProviderMapper 映射器类型 ID */
    public void setIdentityProviderMapper(String identityProviderMapper) {
        this.identityProviderMapper = identityProviderMapper;
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
