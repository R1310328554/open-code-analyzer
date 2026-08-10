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

package com.alibaba.nacos.plugin.ai.importer;

/**
 * AI 资源导入插件使用的公共常量。
 *
 * <p>集中定义资源类型标识，供导入 SPI 与 Nacos 导入管理器对齐语义。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public final class AiResourceImportConstants {
    
    /** MCP（Model Context Protocol）服务器资源类型标识。 */
    public static final String RESOURCE_TYPE_MCP = "mcp";
    
    /** Skill 技能包资源类型标识。 */
    public static final String RESOURCE_TYPE_SKILL = "skill";
    
    private AiResourceImportConstants() {
    }
}
