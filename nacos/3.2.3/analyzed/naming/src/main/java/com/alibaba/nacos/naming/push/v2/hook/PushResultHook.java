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

package com.alibaba.nacos.naming.push.v2.hook;

/**
 * 命名推送结果回调钩子接口。
 *
 * <p>推送链路在 RPC 回调成功或失败后调用，供监控、审计等扩展；可通过 SPI 注册并由 {@link PushResultHookHolder} 聚合分发。</p>
 *
 * @author xiweng.yy
 */
public interface PushResultHook {
    
    /**
     * 推送成功回调。
     *
     * @param result push result
     */
    void pushSuccess(PushResult result);
    
    /**
     * 推送失败回调。
     *
     * @param result push result
     */
    void pushFailed(PushResult result);
}
