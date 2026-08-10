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
package org.keycloak.protocol.oidc.mappers;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.representations.provider.ScriptProviderMetadata;

/**
 * 已部署脚本 OIDC 协议映射器。
 * <p>基于 {@link ScriptProviderMetadata} 元数据，在运行时加载并执行自定义脚本以映射 OIDC 声明。</p>
 */
public class DeployedScriptOIDCProtocolMapper extends ScriptBasedOIDCProtocolMapper {

    /** 映射器配置属性列表 */
    private List<ProviderConfigProperty> configProperties;

    /** 脚本提供方元数据（ID、名称、描述与脚本代码） */
    protected ScriptProviderMetadata metadata;

    /** @param metadata 脚本提供方元数据 */
    public DeployedScriptOIDCProtocolMapper(ScriptProviderMetadata metadata) {
        this.metadata = metadata;
    }

    /** 无参构造，供反射实例化使用 */
    public DeployedScriptOIDCProtocolMapper() {
        // 供反射调用
    }

    /** @return 脚本映射器标识 */
    @Override
    public String getId() {
        return metadata.getId();
    }

    /** @return 脚本映射器显示名称 */
    @Override
    public String getDisplayType() {
        return metadata.getName();
    }

    /** @return 脚本映射器说明 */
    @Override
    public String getHelpText() {
        return metadata.getDescription();
    }

    /** @param mapperModel 映射配置 @return 脚本源代码 */
    @Override
    protected String getScriptCode(ProtocolMapperModel mapperModel) {
        return metadata.getCode();
    }

    /** 初始化配置属性（多值与属性映射选项） @param config 配置作用域 */
    @Override
    public void init(Config.Scope config) {
        configProperties = ProviderConfigurationBuilder.create()
                .property()
                .name(ProtocolMapperUtils.MULTIVALUED)
                .label(ProtocolMapperUtils.MULTIVALUED_LABEL)
                .helpText(ProtocolMapperUtils.MULTIVALUED_HELP_TEXT)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue(false)
                .add()
                .build();

        OIDCAttributeMapperHelper.addAttributeConfig(configProperties, UserPropertyMapper.class);
    }

    /** @return 配置属性列表 */
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    /** 是否支持：需启用 SCRIPTS 特性 @param config 配置作用域 @return 是否可用 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.SCRIPTS);
    }

    /** 设置脚本元数据 @param metadata 脚本提供方元数据 */
    public void setMetadata(ScriptProviderMetadata metadata) {
        this.metadata = metadata;
    }

    /** @return 脚本提供方元数据 */
    public ScriptProviderMetadata getMetadata() {
        return metadata;
    }
}
