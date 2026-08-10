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

package com.alibaba.nacos.api.ai.remote.request;

/**
 * 查询 Nacos AI 模块中 MCP Server 详情的远程请求。
 *
 * <p>继承 {@link AbstractMcpRequest}，通过 {@link #version} 指定目标版本。</p>
 *
 * @author xiweng.yy
 */
public class QueryMcpServerRequest extends AbstractMcpRequest {
    
    /** 目标 MCP Server 版本号；为空时返回最新或默认版本。 */
    private String version;
    
    /** 获取查询目标版本号。 */
    public String getVersion() {
        return version;
    }
    
    /** 设置查询目标版本号。 */
    public void setVersion(String version) {
        this.version = version;
    }
}
