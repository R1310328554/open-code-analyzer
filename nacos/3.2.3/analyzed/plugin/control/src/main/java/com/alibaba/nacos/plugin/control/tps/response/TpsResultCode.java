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

package com.alibaba.nacos.plugin.control.tps.response;

/**
 * TPS 限流校验结果码常量。
 *
 * <p>定义限流点放行、监控放行、拒绝及跳过校验等状态码，供 {@link TpsCheckResponse} 使用。</p>
 *
 * @author shiyiyue
 */
public class TpsResultCode {
    
    /** 限流点规则校验通过。 */
    public static final int PASS_BY_POINT = 200;
    
    /**
     * 规则拒绝但监控模式放行。
     *
     * <p>限流规则命中拒绝条件，但当前为 monitor 模式，仅记录不拦截。</p>
     */
    public static final int PASS_BY_MONITOR = 201;
    
    /**
     * 被限流点规则拒绝。
     *
     * <p>超出限流点配置的 TPS 上限，请求被拦截。</p>
     */
    public static final int DENY_BY_POINT = 300;
    
    /**
     * 跳过 TPS 校验。
     *
     * <p>限流点未注册或管控未启用时直接放行。</p>
     */
    public static final int CHECK_SKIP = 100;
    
}
