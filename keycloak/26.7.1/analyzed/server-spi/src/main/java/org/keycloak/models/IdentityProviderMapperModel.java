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

package org.keycloak.models;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.keycloak.models.utils.MapperTypeSerializer;

/**
 * 身份提供方映射器模型：定义联邦登录到本地用户数据的属性映射规则。
 * Specifies a mapping from broker login to user data.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class IdentityProviderMapperModel implements Serializable {
    /** 同步模式配置键。 */
    public static final String SYNC_MODE = "syncMode";

    protected String id;
    protected String name;
    protected String identityProviderAlias;
    protected String identityProviderMapper;
    protected Map<String, String> config;


    /** @return 映射器唯一标识符 */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /** @return 映射器显示名称 */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** @return 所属身份提供方别名 */
    public String getIdentityProviderAlias() {
        return identityProviderAlias;
    }

    public void setIdentityProviderAlias(String identityProviderAlias) {
        this.identityProviderAlias = identityProviderAlias;
    }

    /** @return 映射器实现类型 ID */
    public String getIdentityProviderMapper() {
        return identityProviderMapper;
    }

    public void setIdentityProviderMapper(String identityProviderMapper) {
        this.identityProviderMapper = identityProviderMapper;
    }

    /** @return 用户属性同步模式 */
    public IdentityProviderMapperSyncMode getSyncMode() {
        return IdentityProviderMapperSyncMode.valueOf(getConfig().getOrDefault(SYNC_MODE, "LEGACY"));
    }

    public void setSyncMode(IdentityProviderMapperSyncMode syncMode) {
        getConfig().put(SYNC_MODE, syncMode.toString());
    }

    /** @return 映射器配置项 */
    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    /** @param configKey 配置键
     * @return 反序列化后的多值配置映射 */
    public Map<String, List<String>> getConfigMap(String configKey) {
        String configMap = config.get(configKey);
        return MapperTypeSerializer.deserialize(configMap);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IdentityProviderMapperModel that = (IdentityProviderMapperModel) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
