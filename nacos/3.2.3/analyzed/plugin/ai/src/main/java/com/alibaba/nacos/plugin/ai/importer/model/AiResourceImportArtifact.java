/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.ai.importer.model;

import java.util.Map;

/**
 * AI 资源导入插件拉取到的导入边界对象（Artifact）。
 *
 * <p>Artifact 并非 Nacos 持久化资源模型本身；资源操作器负责将其转换为
 * 当前 Nacos 领域服务可识别的领域对象后再写入存储。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class AiResourceImportArtifact {
    
    /** 资源类型，例如 mcp 或 skill。 */
    private String resourceType;
    
    /** 外部系统中的唯一标识。 */
    private String externalId;
    
    /** 资源展示名称。 */
    private String name;
    
    /** 资源版本号。 */
    private String version;
    
    /** 资源描述信息。 */
    private String description;
    
    /** 载荷编码类型，见 {@link AiResourceImportPayloadKind}。 */
    private AiResourceImportPayloadKind payloadKind;
    
    /** 二进制载荷内容。 */
    private byte[] payload;
    
    /** JSON 文本载荷内容（与 payload 二选一或互补使用）。 */
    private String payloadJson;
    
    /** 载荷完整性校验和，用于去重与一致性验证。 */
    private String checksum;
    
    /** 来源侧附加元数据键值对。 */
    private Map<String, String> sourceMetadata;
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public String getExternalId() {
        return externalId;
    }
    
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public AiResourceImportPayloadKind getPayloadKind() {
        return payloadKind;
    }
    
    public void setPayloadKind(AiResourceImportPayloadKind payloadKind) {
        this.payloadKind = payloadKind;
    }
    
    public byte[] getPayload() {
        return payload;
    }
    
    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
    
    public String getPayloadJson() {
        return payloadJson;
    }
    
    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }
    
    public String getChecksum() {
        return checksum;
    }
    
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
    
    public Map<String, String> getSourceMetadata() {
        return sourceMetadata;
    }
    
    public void setSourceMetadata(Map<String, String> sourceMetadata) {
        this.sourceMetadata = sourceMetadata;
    }
}
