/*
 * Copyright 1999-$toady.year Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.enums;

/**
 * 配置中心 HTTP API 版本枚举，标识 v1/v2 路由前缀。
 * Config Api Version enum.
 * @author Nacos
 */
public enum ApiVersionEnum {
    
    /**
     * API 版本 v1（历史 Open API）。
     * API version v1.
     */
    V1("v1"),
    
    /**
     * API 版本 v2（统一 Result 包装）。
     * API version v2.
     */
    V2("v2");
    
    private final String version;
    
    ApiVersionEnum(String version) {
        this.version = version;
    }
    
    /** 返回版本字符串标识（如 {@code v1}、{@code v2}） */
    public String getVersion() {
        return version;
    }
}
