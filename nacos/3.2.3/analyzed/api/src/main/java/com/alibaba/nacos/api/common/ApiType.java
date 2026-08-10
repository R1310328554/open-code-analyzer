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

package com.alibaba.nacos.api.common;

/**
 * Nacos API 类型分类。
 *
 * <p>区分管理员 API、控制台 API、开放 API 与集群内部 API，
 * 用于鉴权、审计与路由策略。</p>
 *
 * @author zhangyukun
 * @author xiweng.yy
 */
public enum ApiType {
    
    /** 管理员 API，供 Nacos 运维人员使用。 */
    ADMIN_API("ADMIN_API"),
    /** 控制台 API，供 Nacos 控制台调用。 */
    CONSOLE_API("CONSOLE_API"),
    /** 开放 API，供客户端或基础数据操作使用。 */
    OPEN_API("OPEN_API"),
    /** 内部 API，用于 Nacos 集群节点间通信。 */
    INNER_API("INNER_API");
    
    private final String description;
    
    ApiType(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return description;
    }
}
