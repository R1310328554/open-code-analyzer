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

package org.keycloak.representations.info;

import java.util.List;
import java.util.Map;

import org.keycloak.representations.idm.ComponentTypeRepresentation;
import org.keycloak.representations.idm.PasswordPolicyTypeRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.idm.ProtocolMapperTypeRepresentation;

/**
 * Keycloak 服务器综合信息的 REST 表示，由 {@code /admin/serverinfo} 端点返回，
 * 聚合系统/内存/CPU、Profile、功能开关、主题、Provider SPI、协议映射器等运行时元数据。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ServerInfoRepresentation {

    /** 操作系统与 JVM 等系统信息。 */
    private SystemInfoRepresentation systemInfo;
    /** CPU 信息。 */
    private CpuInfoRepresentation cpuInfo;
    /** JVM 堆内存信息。 */
    private MemoryInfoRepresentation memoryInfo;
    /** 当前运行 Profile 及功能分类。 */
    private ProfileInfoRepresentation profileInfo;

    /** 全部可配置功能及其启用状态。 */
    private List<FeatureRepresentation> features;

    /** 加密 Provider 与支持的算法/密钥库类型。 */
    private CryptoInfoRepresentation cryptoInfo;

    /** 主题类型到可用主题列表的映射（如 login、account）。 */
    private Map<String, List<ThemeInfoRepresentation>> themes;

    /** 社交登录 Provider 元数据列表。 */
    private List<Map<String, String>> socialProviders;
    /** 身份联邦 Provider 元数据列表。 */
    private List<Map<String, String>> identityProviders;
    /** 客户端导入器 Provider 元数据列表。 */
    private List<Map<String, String>> clientImporters;

    /** SPI 名称到 {@link SpiInfoRepresentation} 的映射。 */
    private Map<String, SpiInfoRepresentation> providers;

    /** 协议 ID 到可用 Protocol Mapper 类型列表的映射。 */
    private Map<String, List<ProtocolMapperTypeRepresentation>> protocolMapperTypes;
    /** 协议 ID 到内置 Protocol Mapper 实例列表的映射。 */
    private Map<String, List<ProtocolMapperRepresentation>> builtinProtocolMappers;
    /** 协议 ID 到客户端安装指引列表的映射。 */
    private Map<String, List<ClientInstallationRepresentation>> clientInstallations;
    /** 组件类型分组到 {@link ComponentTypeRepresentation} 列表的映射。 */
    private Map<String, List<ComponentTypeRepresentation>> componentTypes;

    /** 可用的密码策略类型列表。 */
    private List<PasswordPolicyTypeRepresentation> passwordPolicies;

    /** 枚举类名到合法枚举值列表的映射，供 Admin UI 下拉框使用。 */
    private Map<String, List<String>> enums;

    /** 支持的参数化 OAuth Scope 类型列表。 */
    private List<ParameterizedScopeTypeRepresentation> parameterizedScopeTypes;

    /** @return 系统信息 */
    public SystemInfoRepresentation getSystemInfo() {
        return systemInfo;
    }

    /** @param systemInfo 系统信息 */
    public void setSystemInfo(SystemInfoRepresentation systemInfo) {
        this.systemInfo = systemInfo;
    }

    /** @return 内存信息 */
    public MemoryInfoRepresentation getMemoryInfo() {
        return memoryInfo;
    }

    /** @param memoryInfo 内存信息 */
    public void setMemoryInfo(MemoryInfoRepresentation memoryInfo) {
        this.memoryInfo = memoryInfo;
    }

    /** @return CPU 信息 */
    public CpuInfoRepresentation getCpuInfo() {
        return cpuInfo;
    }

    /** @param cpuInfo CPU 信息 */
    public void setCpuInfo(CpuInfoRepresentation cpuInfo) {
        this.cpuInfo = cpuInfo;
    }

    /** @return Profile 信息 */
    public ProfileInfoRepresentation getProfileInfo() {
        return profileInfo;
    }

    /** @param profileInfo Profile 信息 */
    public void setProfileInfo(ProfileInfoRepresentation profileInfo) {
        this.profileInfo = profileInfo;
    }

    /** @return 功能开关列表 */
    public List<FeatureRepresentation> getFeatures() {
        return features;
    }

    /** @param features 功能开关列表 */
    public void setFeatures(List<FeatureRepresentation> features) {
        this.features = features;
    }

    /** @return 加密信息 */
    public CryptoInfoRepresentation getCryptoInfo() {
        return cryptoInfo;
    }

    /** @param cryptoInfo 加密信息 */
    public void setCryptoInfo(CryptoInfoRepresentation cryptoInfo) {
        this.cryptoInfo = cryptoInfo;
    }

    /** @return 主题映射 */
    public Map<String, List<ThemeInfoRepresentation>> getThemes() {
        return themes;
    }

    /** @param themes 主题映射 */
    public void setThemes(Map<String, List<ThemeInfoRepresentation>> themes) {
        this.themes = themes;
    }

    /** @return 社交 Provider 列表 */
    public List<Map<String, String>> getSocialProviders() {
        return socialProviders;
    }

    /** @param socialProviders 社交 Provider 列表 */
    public void setSocialProviders(List<Map<String, String>> socialProviders) {
        this.socialProviders = socialProviders;
    }

    /** @return 身份 Provider 列表 */
    public List<Map<String, String>> getIdentityProviders() {
        return identityProviders;
    }

    /** @param identityProviders 身份 Provider 列表 */
    public void setIdentityProviders(List<Map<String, String>> identityProviders) {
        this.identityProviders = identityProviders;
    }

    /** @return 客户端导入器列表 */
    public List<Map<String, String>> getClientImporters() {
        return clientImporters;
    }

    /** @param clientImporters 客户端导入器列表 */
    public void setClientImporters(List<Map<String, String>> clientImporters) {
        this.clientImporters = clientImporters;
    }

    /** @return SPI Provider 映射 */
    public Map<String, SpiInfoRepresentation> getProviders() {
        return providers;
    }

    /** @param providers SPI Provider 映射 */
    public void setProviders(Map<String, SpiInfoRepresentation> providers) {
        this.providers = providers;
    }

    /** @return 协议 Mapper 类型映射 */
    public Map<String, List<ProtocolMapperTypeRepresentation>> getProtocolMapperTypes() {
        return protocolMapperTypes;
    }

    /** @param protocolMapperTypes 协议 Mapper 类型映射 */
    public void setProtocolMapperTypes(Map<String, List<ProtocolMapperTypeRepresentation>> protocolMapperTypes) {
        this.protocolMapperTypes = protocolMapperTypes;
    }

    /** @return 内置协议 Mapper 映射 */
    public Map<String, List<ProtocolMapperRepresentation>> getBuiltinProtocolMappers() {
        return builtinProtocolMappers;
    }

    /** @param builtinProtocolMappers 内置协议 Mapper 映射 */
    public void setBuiltinProtocolMappers(Map<String, List<ProtocolMapperRepresentation>> builtinProtocolMappers) {
        this.builtinProtocolMappers = builtinProtocolMappers;
    }

    /** @return 枚举值映射 */
    public Map<String, List<String>> getEnums() {
        return enums;
    }

    /** @param enums 枚举值映射 */
    public void setEnums(Map<String, List<String>> enums) {
        this.enums = enums;
    }

    /** @return 客户端安装指引映射 */
    public Map<String, List<ClientInstallationRepresentation>> getClientInstallations() {
        return clientInstallations;
    }

    /** @param clientInstallations 客户端安装指引映射 */
    public void setClientInstallations(Map<String, List<ClientInstallationRepresentation>> clientInstallations) {
        this.clientInstallations = clientInstallations;
    }

    /** @return 密码策略类型列表 */
    public List<PasswordPolicyTypeRepresentation> getPasswordPolicies() {
        return passwordPolicies;
    }

    /** @param passwordPolicies 密码策略类型列表 */
    public void setPasswordPolicies(List<PasswordPolicyTypeRepresentation> passwordPolicies) {
        this.passwordPolicies = passwordPolicies;
    }

    /** @return 组件类型映射 */
    public Map<String, List<ComponentTypeRepresentation>> getComponentTypes() {
        return componentTypes;
    }

    /** @param componentTypes 组件类型映射 */
    public void setComponentTypes(Map<String, List<ComponentTypeRepresentation>> componentTypes) {
        this.componentTypes = componentTypes;
    }

    /** @return 参数化 Scope 类型列表 */
    public List<ParameterizedScopeTypeRepresentation> getParameterizedScopeTypes() {
        return parameterizedScopeTypes;
    }

    /** @param parameterizedScopeTypes 参数化 Scope 类型列表 */
    public void setParameterizedScopeTypes(List<ParameterizedScopeTypeRepresentation> parameterizedScopeTypes) {
        this.parameterizedScopeTypes = parameterizedScopeTypes;
    }
}
