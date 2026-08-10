/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.common.paramcheck;

import java.util.Map;

/**
 * 待校验参数聚合 DTO：封装命名、配置、服务注册及 MCP/Agent/Skill 相关字段，供 {@link AbstractParamChecker#checkParamInfoList} 逐项校验。
 * Param info.
 *
 * @author zhuoguang
 */
public class ParamInfo {
    
    /** 命名空间展示名称 */
    private String namespaceShowName;
    
    private String namespaceId;
    
    /** 配置 dataId */
    private String dataId;
    
    /** 服务名 */
    private String serviceName;
    
    private String group;
    
    private String cluster;
    
    private String clusters;
    
    private String ip;
    
    private String port;
    
    /** 服务或实例元数据键值对 */
    private Map<String, String> metadata;
    
    private String mcpName;
    
    private String mcpId;
    
    private String agentName;
    
    /** Skill 名称（小写 DNS 子域风格） */
    private String skillName;
    
    public String getNamespaceShowName() {
        return namespaceShowName;
    }
    
    public void setNamespaceShowName(String namespaceShowName) {
        this.namespaceShowName = namespaceShowName;
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    public String getDataId() {
        return dataId;
    }
    
    public void setDataId(String dataId) {
        this.dataId = dataId;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getGroup() {
        return group;
    }
    
    public void setGroup(String group) {
        this.group = group;
    }
    
    public String getCluster() {
        return cluster;
    }
    
    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
    
    public String getClusters() {
        return clusters;
    }
    
    public void setClusters(String clusters) {
        this.clusters = clusters;
    }
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public String getPort() {
        return port;
    }
    
    public void setPort(String port) {
        this.port = port;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public String getMcpName() {
        return mcpName;
    }
    
    public String getMcpId() {
        return mcpId;
    }
    
    public void setMcpId(String mcpId) {
        this.mcpId = mcpId;
    }
    
    public void setMcpName(String mcpName) {
        this.mcpName = mcpName;
    }
    
    public String getAgentName() {
        return agentName;
    }
    
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }
    
    public String getSkillName() {
        return skillName;
    }
    
    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }
}
