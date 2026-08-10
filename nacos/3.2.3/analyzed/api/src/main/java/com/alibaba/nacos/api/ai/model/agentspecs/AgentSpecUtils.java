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

package com.alibaba.nacos.api.ai.model.agentspecs;

import com.alibaba.nacos.api.ai.model.NacosAiConfigKeyCodec;
import com.alibaba.nacos.api.utils.StringUtils;

/**
 * AgentSpec 配置键与 Nacos Config dataId/group 构建工具类。
 *
 * <p>设计模式对标 {@link com.alibaba.nacos.api.ai.model.skills.SkillUtils}，
 * 提供 manifest、资源文件、版本化 group 等常量及编解码辅助方法。</p>
 *
 * @author nacos
 */
public class AgentSpecUtils {
    
    /** AgentSpec 主配置 dataId（manifest.json）。 */
    public static final String AGENTSPEC_MAIN_DATA_ID = "manifest.json";
    
    /** 资源子配置 dataId 前缀。 */
    public static final String RESOURCE_DATA_ID_PREFIX = "resource_";
    
    /** 资源子配置 dataId 后缀。 */
    public static final String RESOURCE_DATA_ID_SUFFIX = ".json";
    
    /**
     * 客户端缓存用的 AgentSpec 索引配置 dataId。
     * <p>服务端在 group {@code agentspec__{name}} 下写入该 dataId，
     * 内容包含当前在线版本与文件清单。</p>
     */
    public static final String AGENTSPEC_INDEX_DATA_ID = "agentspec_index.json";
    
    /** AgentSpec 配置 group 前缀。 */
    public static final String AGENTSPEC_GROUP_PREFIX = "agentspec__";
    
    private static final String DOUBLE_UNDERSCORE = "__";
    private static final String FILE_EXTENSION_PATTERN = ".*\\.[a-zA-Z0-9]+$";
    
    /** 封装 Nacos Config 的 dataId 与 group。 */
    public static class ConfigInfo {
        
        private final String dataId;
        
        private final String group;
        
        public ConfigInfo(String dataId, String group) {
            this.dataId = dataId;
            this.group = group;
        }
        
        /** 返回配置 dataId。 */
        public String getDataId() {
            return dataId;
        }
        
        /** 返回配置 group。 */
        public String getGroup() {
            return group;
        }
    }
    
    /**
     * 根据资源类型与名称生成资源 ID。
     * <p>格式为 {@code {type}_{resourcename}}；若名称以 {@code .xx} 结尾则将最后一个 {@code .} 转为 {@code __}；
     * 类型中的 {@code /} 编码为 {@code .}，以保证 dataId {@code resource_{resourceId}.json} 合法。</p>
     *
     * @param type resource type (can be null or empty; may contain / for multi-level paths)
     * @param resourceName resource name
     * @return resource ID (safe for use in config dataId)
     */
    public static String generateResourceId(String type, String resourceName) {
        if (resourceName == null || resourceName.trim().isEmpty()) {
            return "";
        }
        
        // 若资源名以 .xx 结尾，将最后一个 . 替换为 __
        String processedName = resourceName;
        if (resourceName.matches(FILE_EXTENSION_PATTERN)) {
            int lastDotIndex = resourceName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                processedName = resourceName.substring(0, lastDotIndex) + DOUBLE_UNDERSCORE
                    + resourceName.substring(lastDotIndex + 1);
            }
        }
        
        if (type != null && !type.trim().isEmpty()) {
            String safeType = type.trim().replace("/", ".");
            return safeType + "_" + processedName;
        } else {
            return processedName;
        }
    }
    
    /**
     * 构建 AgentSpec 主配置（manifest）的 dataId 与 group。
     *
     * @param agentSpecName name of AgentSpec
     * @return ConfigInfo containing dataId and group
     * @throws IllegalArgumentException if agentSpecName is blank
     */
    public static ConfigInfo buildAgentSpecMainConfigInfo(String agentSpecName) {
        if (StringUtils.isBlank(agentSpecName)) {
            throw new IllegalArgumentException("AgentSpec name cannot be blank");
        }
        return new ConfigInfo(AGENTSPEC_MAIN_DATA_ID, buildAgentSpecGroup(agentSpecName));
    }
    
    /**
     * 构建 AgentSpec 的无版本后缀 Nacos Config group。
     *
     * @param agentSpecName name of AgentSpec
     * @return config group string, e.g. "agentspec__myworker"
     * @throws IllegalArgumentException if agentSpecName is blank
     */
    public static String buildAgentSpecGroup(String agentSpecName) {
        if (StringUtils.isBlank(agentSpecName)) {
            throw new IllegalArgumentException("AgentSpec name cannot be blank");
        }
        return AGENTSPEC_GROUP_PREFIX
            + NacosAiConfigKeyCodec.encodeManifestGroupNameSegment(agentSpecName);
    }
    
    /**
     * 构建指定 AgentSpec 版本的 Nacos Config group。
     *
     * @param agentSpecName name of AgentSpec
     * @param version       version string, e.g. "v1"
     * @return config group string, e.g. "agentspec__myworker__v1"
     * @throws IllegalArgumentException if agentSpecName or version is blank
     */
    public static String buildAgentSpecVersionGroup(String agentSpecName, String version) {
        if (StringUtils.isBlank(agentSpecName)) {
            throw new IllegalArgumentException("AgentSpec name cannot be blank");
        }
        if (StringUtils.isBlank(version)) {
            throw new IllegalArgumentException("Version cannot be blank");
        }
        return AGENTSPEC_GROUP_PREFIX
            + NacosAiConfigKeyCodec.encodeVersionedGroupSegment(agentSpecName)
            + DOUBLE_UNDERSCORE + NacosAiConfigKeyCodec.encodeVersionedGroupSegment(version);
    }
    
    /**
     * 构建 AgentSpec 资源子配置的 dataId 与 group。
     *
     * @param agentSpecName name of AgentSpec
     * @param type resource type (can be null or empty)
     * @param resourceName resource name
     * @return ConfigInfo containing dataId and group
     * @throws IllegalArgumentException if agentSpecName or resourceName is blank
     */
    public static ConfigInfo buildAgentSpecResourceConfigInfo(String agentSpecName, String type,
        String resourceName) {
        if (StringUtils.isBlank(agentSpecName)) {
            throw new IllegalArgumentException("AgentSpec name cannot be blank");
        }
        if (StringUtils.isBlank(resourceName)) {
            throw new IllegalArgumentException("Resource name cannot be blank");
        }
        
        String resourceId = generateResourceId(type, resourceName);
        String dataId = NacosAiConfigKeyCodec.encodeSegment(
            RESOURCE_DATA_ID_PREFIX + resourceId + RESOURCE_DATA_ID_SUFFIX);
        String group = buildAgentSpecGroup(agentSpecName);
        
        return new ConfigInfo(dataId, group);
    }
    
    /**
     * 将存储的 AgentSpec Nacos Config {@code group} 解码为逻辑名称与可选版本。
     *
     * @param group physical group, e.g. {@code agentspec__myagent} or {@code agentspec__name__v1}
     * @return array of length 2: {@code [agentSpecName, version]}; {@code version} is {@code null} when not versioned
     */
    public static String[] decodeAgentSpecGroupToNameAndVersion(String group) {
        if (StringUtils.isBlank(group) || !group.startsWith(AGENTSPEC_GROUP_PREFIX)) {
            throw new IllegalArgumentException("Not an AgentSpec config group: " + group);
        }
        String rest = group.substring(AGENTSPEC_GROUP_PREFIX.length());
        int idx = rest.lastIndexOf(DOUBLE_UNDERSCORE);
        if (idx < 0) {
            return new String[] {NacosAiConfigKeyCodec.decodeSegment(rest), null};
        }
        return new String[] {NacosAiConfigKeyCodec.decodeSegment(rest.substring(0, idx)),
            NacosAiConfigKeyCodec.decodeSegment(rest.substring(idx + DOUBLE_UNDERSCORE.length()))};
    }
}
