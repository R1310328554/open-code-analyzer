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

package com.alibaba.nacos.client.ai.utils;

import com.alibaba.nacos.common.utils.StringUtils;

/**
 * Nacos AI 模块缓存键构建工具。
 *
 * <p>为 MCP 服务器、Agent Card、Skill、AgentSpec、Prompt 等资源生成统一格式的本地缓存键。</p>
 *
 * @author xiweng.yy
 */
public class CacheKeyUtils {
    
    /** 表示最新版本的占位符字符串。 */
    public static final String LATEST_VERSION = "latest";
    
    /**
     * 构建 MCP 服务器带版本缓存键。
     *
     * @param mcpName MCP 服务器名称
     * @param version 版本号，为空时使用 {@link #LATEST_VERSION}
     * @return 格式为 {@code ${mcpName}::${version}} 的缓存键
     */
    public static String buildMcpServerKey(String mcpName, String version) {
        return buildVersionedKey(mcpName, version);
    }
    
    /**
     * 构建 Agent Card 带版本缓存键。
     *
     * @param agentName Agent 名称
     * @param version   版本号，为空时使用 {@link #LATEST_VERSION}
     * @return 格式为 {@code ${agentName}::${version}} 的缓存键
     */
    public static String buildAgentCardKey(String agentName, String version) {
        return buildVersionedKey(agentName, version);
    }
    
    /**
     * 构建 Skill 简单缓存键（仅名称）。
     *
     * @param skillName Skill 名称
     * @return 缓存键，即 skillName 本身
     */
    public static String buildSkillKey(String skillName) {
        return skillName;
    }
    
    /**
     * 构建 Skill 查询缓存键（支持 label 或 version）。
     *
     * @param skillName Skill 名称
     * @param version   版本号，可选
     * @param label     标签，可选
     * @return 格式为 {@code ${skillName}::label:${label}} 或 {@code ::version:${version}} 或 {@code ::latest}
     */
    public static String buildSkillKey(String skillName, String version, String label) {
        if (StringUtils.isNotBlank(label)) {
            return skillName + "::label:" + label;
        }
        if (StringUtils.isNotBlank(version)) {
            return skillName + "::version:" + version;
        }
        return skillName + "::" + LATEST_VERSION;
    }
    
    /**
     * 构建 AgentSpec 缓存键。
     *
     * @param agentSpecName AgentSpec 名称
     * @return 缓存键，即 agentSpecName 本身
     */
    public static String buildAgentSpecKey(String agentSpecName) {
        return agentSpecName;
    }
    
    /**
     * 构建 Prompt 简单缓存键。
     *
     * @param promptKey Prompt 键
     * @return 用于缓存的 Prompt 键
     */
    public static String buildPromptKey(String promptKey) {
        return promptKey;
    }
    
    /**
     * 构建 Prompt 查询缓存键（支持 label 或 version）。
     *
     * @param promptKey Prompt 键
     * @param version   版本号，可选
     * @param label     标签，可选
     * @return 格式为 {@code ${promptKey}::label:${label}} 或 {@code ::version:${version}} 或 {@code ::latest}
     */
    public static String buildPromptKey(String promptKey, String version, String label) {
        if (StringUtils.isNotBlank(label)) {
            return promptKey + "::label:" + label;
        }
        if (StringUtils.isNotBlank(version)) {
            return promptKey + "::version:" + version;
        }
        return promptKey + "::" + LATEST_VERSION;
    }
    
    /** 通用带版本键构建：空版本时回退为 latest。 */
    private static String buildVersionedKey(String name, String version) {
        if (StringUtils.isBlank(version)) {
            version = LATEST_VERSION;
        }
        return name + "::" + version;
    }
}
