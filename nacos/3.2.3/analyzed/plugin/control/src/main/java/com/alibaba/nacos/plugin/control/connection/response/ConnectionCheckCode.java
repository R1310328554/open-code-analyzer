/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.control.connection.response;

/**
 * 连接准入检查结果码常量，供 {@link ConnectionCheckResponse} 使用。
 *
 * <p>数值区分放行、跳过检查与因总量超限拒绝等场景。</p>
 *
 * @author shiyiyue
 */
public class ConnectionCheckCode {
    
    /**
     * 按总连接数校验通过，允许建连。
     */
    public static final int PASS_BY_TOTAL = 200;
    
    /**
     * 跳过连接数检查（如未启用限流或规则为空）。
     */
    public static final int CHECK_SKIP = 100;
    
    /**
     * 因总连接数超过上限而拒绝建连。
     */
    public static final int DENY_BY_TOTAL_OVER = 300;
    
    /**
     * 客户端 IP 处于监控白名单，仅记录指标不限流放行。
     */
    public static final int PASS_BY_MONITOR = 205;
}
