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
 * 查询 Nacos AI 模块中 Agent Card 的远程请求。
 *
 * <p>可按 {@link #version} 与 {@link #registrationType} 筛选目标 Agent Card 详情。</p>
 *
 * @author xiweng.yy
 */
public class QueryAgentCardRequest extends AbstractAgentRequest {
    
    /** 目标 Agent Card 版本号；为空时返回最新或默认版本。 */
    private String version;
    
    /** 注册类型（如 service 端点类型），用于区分不同注册方式。 */
    private String registrationType;
    
    /** 获取查询目标版本号。 */
    public String getVersion() {
        return version;
    }
    
    /** 设置查询目标版本号。 */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /** 获取注册类型筛选条件。 */
    public String getRegistrationType() {
        return registrationType;
    }
    
    /** 设置注册类型筛选条件。 */
    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }
}
