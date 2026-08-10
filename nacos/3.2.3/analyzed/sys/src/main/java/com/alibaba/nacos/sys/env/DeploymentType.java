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

package com.alibaba.nacos.sys.env;

/**
 * Nacos 进程部署形态枚举。
 *
 * <p>区分 Server、Console、合并部署及带 MCP 的 Server 等运行模式，与 {@link Constants#NACOS_DEPLOYMENT_TYPE} 配置项对应。</p>
 *
 * @author xiweng.yy
 */
public enum DeploymentType {
    
    /** 默认合并部署：Server 与 Console 同进程运行。 */
    MERGED(Constants.NACOS_DEPLOYMENT_TYPE_MERGED),
    
    /** 仅 Server 部署：进程内只启动命名/配置等核心服务。 */
    SERVER(Constants.NACOS_DEPLOYMENT_TYPE_SERVER),
    
    /** 仅 Console 部署：进程内只启动控制台 Web 应用。 */
    CONSOLE(Constants.NACOS_DEPLOYMENT_TYPE_CONSOLE),
    
    /** Server 与 MCP 同进程部署。 */
    SERVER_WITH_MCP(Constants.NACOS_DEPLOYMENT_TYPE_SERVER_WITH_MCP),
    
    /** 未知或非法部署类型占位。 */
    ILLEGAL("unknown");
    
    private final String typeName;
    
    DeploymentType(String typeName) {
        this.typeName = typeName;
    }
    
    public String getTypeName() {
        return typeName;
    }
    
    /** 按配置字符串解析部署类型，无法识别时返回 {@link #ILLEGAL}。 */
    public static DeploymentType getType(String type) {
        try {
            return DeploymentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ILLEGAL;
        }
    }
}
