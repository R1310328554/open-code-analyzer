/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.common.trace;

/**
 * 健康检查类型枚举：区分客户端心跳、HTTP、MySQL、TCP 等探测方式；
 * {@link #getPrefix()} 返回值用于解析健康状态变更原因字符串的前缀匹配。
 * The types of health check.
 *
 * @author yanda
 */
public enum HealthCheckType {
    
    /** 客户端主动上报心跳超时 */
    /**
     * Instance heart beat timeout.
      * <p>健康检查类型；详见类级说明。</p>
     */
    CLIENT_BEAT("client_beat"),
    /** HTTP 协议健康探测 */
    /**
     * Http health check.
      * <p>健康检查类型；详见类级说明。</p>
     */
    HTTP_HEALTH_CHECK("http"),
    /** MySQL 协议健康探测 */
    /**
     * Mysql health check.
      * <p>健康检查类型；详见类级说明。</p>
     */
    MYSQL_HEALTH_CHECK("mysql"),
    /** TCP 超感探测（端口连通性检测） */
    /**
     * Tcp super sense health check .
      * <p>健康检查类型；详见类级说明。</p>
     */
    TCP_SUPER_SENSE("tcp");
    
    /** 健康检查类型在原因字符串中的前缀标识 */
    private String prefix;
    
    HealthCheckType(String prefix) {
        this.prefix = prefix;
    }
    
    /** 返回该检查类型的前缀字符串，如 {@code http}、{@code tcp} */
    public String getPrefix() {
        return prefix;
    }
}
