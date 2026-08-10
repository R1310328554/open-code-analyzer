/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.api.ai.model.mcp.registry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

/**
 * MCP Server 可安装包定义，对应 components.schemas.Package。
 *
 * <p>描述 Registry 中 Server 的安装来源、运行时参数、环境变量及传输层配置，
 * 支持 stdio、streamable-http、sse 等多种 {@link #transport} 类型。</p>
 *
 * @author xinluo
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Package {
    
    /** Registry 类型标识（如 npm、pypi）。 */
    private String registryType;
    
    /** Registry 基础 URL。 */
    private String registryBaseUrl;
    
    /** 包唯一标识符。 */
    private String identifier;
    
    /** 包版本号。 */
    private String version;
    
    /** 包文件 SHA-256 校验和。 */
    private String fileSha256;
    
    /** 运行时提示（如 node、python）。 */
    private String runtimeHint;
    
    /** 启动 Server 进程时的运行时参数列表。 */
    private List<Argument> runtimeArguments;
    
    /** 安装包时的包管理器参数列表。 */
    private List<Argument> packageArguments;
    
    /** 环境变量键值对列表。 */
    private List<KeyValueInput> environmentVariables;
    
    /**
     * 传输层配置（必填），支持 stdio、streamable-http、sse 等类型。
     * 反序列化为 {@link StdioTransport}、{@link StreamableHttpTransport} 或 {@link SseTransport}。
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = StdioTransport.class, name = "stdio"),
        @JsonSubTypes.Type(value = StreamableHttpTransport.class, name = "streamable-http"),
        @JsonSubTypes.Type(value = SseTransport.class, name = "sse")
    })
    @JsonIgnoreProperties(ignoreUnknown = true)
    private Object transport;
    
    /**
     * 获取 Registry 类型。
     *
     * @return Registry 类型标识
     */
    public String getRegistryType() {
        return registryType;
    }
    
    /**
     * 设置 Registry 类型。
     *
     * @param registryType Registry 类型标识
     */
    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }
    
    /**
     * 获取 Registry 基础 URL。
     *
     * @return 基础 URL
     */
    public String getRegistryBaseUrl() {
        return registryBaseUrl;
    }
    
    /**
     * 设置 Registry 基础 URL。
     *
     * @param registryBaseUrl 基础 URL
     */
    public void setRegistryBaseUrl(String registryBaseUrl) {
        this.registryBaseUrl = registryBaseUrl;
    }
    
    /**
     * 获取包标识符。
     *
     * @return 包 ID
     */
    public String getIdentifier() {
        return identifier;
    }
    
    /**
     * 设置包标识符。
     *
     * @param identifier 包 ID
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    /**
     * 获取包版本。
     *
     * @return 版本号
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * 设置包版本。
     *
     * @param version 版本号
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /**
     * 获取包文件 SHA-256 校验和。
     *
     * @return SHA-256 十六进制字符串
     */
    public String getFileSha256() {
        return fileSha256;
    }
    
    /**
     * 设置包文件 SHA-256 校验和。
     *
     * @param fileSha256 SHA-256 字符串
     */
    public void setFileSha256(String fileSha256) {
        this.fileSha256 = fileSha256;
    }
    
    /**
     * 获取运行时提示。
     *
     * @return 运行时标识
     */
    public String getRuntimeHint() {
        return runtimeHint;
    }
    
    /**
     * 设置运行时提示。
     *
     * @param runtimeHint 运行时标识
     */
    public void setRuntimeHint(String runtimeHint) {
        this.runtimeHint = runtimeHint;
    }
    
    /**
     * 获取运行时参数列表。
     *
     * @return 运行时 {@link Argument} 列表
     */
    public List<Argument> getRuntimeArguments() {
        return runtimeArguments;
    }
    
    /**
     * 设置运行时参数列表。
     *
     * @param runtimeArguments 运行时参数
     */
    public void setRuntimeArguments(List<Argument> runtimeArguments) {
        this.runtimeArguments = runtimeArguments;
    }
    
    /**
     * 获取包安装参数列表。
     *
     * @return 包参数列表
     */
    public List<Argument> getPackageArguments() {
        return packageArguments;
    }
    
    /**
     * 设置包安装参数列表。
     *
     * @param packageArguments 包参数
     */
    public void setPackageArguments(List<Argument> packageArguments) {
        this.packageArguments = packageArguments;
    }
    
    /**
     * 获取环境变量列表。
     *
     * @return 环境变量键值对
     */
    public List<KeyValueInput> getEnvironmentVariables() {
        return environmentVariables;
    }
    
    /**
     * 设置环境变量列表。
     *
     * @param environmentVariables 环境变量
     */
    public void setEnvironmentVariables(List<KeyValueInput> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }
    
    /**
     * 获取传输层配置对象。
     *
     * @return 传输配置（stdio / streamable-http / sse）
     */
    public Object getTransport() {
        return transport;
    }
    
    /**
     * 设置传输层配置对象。
     *
     * @param transport 传输配置
     */
    public void setTransport(Object transport) {
        this.transport = transport;
    }
}
