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

/**
 * AI 资源导入插件返回的载荷（Payload）类型枚举。
 *
 * <p>标识 {@link AiResourceImportArtifact} 中实际内容的编码形式，
 * 便于下游资源操作器选择正确的解析与持久化路径。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public enum AiResourceImportPayloadKind {
    
    /** MCP 服务器详情 JSON 载荷。 */
    MCP_DETAIL,
    
    /** 标准 Skill 压缩包（ZIP）二进制载荷。 */
    SKILL_ZIP,
    
    /** 通用 JSON 文本载荷。 */
    JSON,
    
    /** 通用二进制字节载荷。 */
    BYTES
}
