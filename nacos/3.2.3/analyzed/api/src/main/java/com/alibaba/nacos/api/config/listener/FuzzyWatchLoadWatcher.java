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

package com.alibaba.nacos.api.config.listener;

/**
 * 模糊监听负载超限时的回调接口。
 *
 * <p>当服务端模式数量或匹配配置数量超过限制时触发相应告警回调。</p>
 *
 * @author shiyiyue
 */
public interface FuzzyWatchLoadWatcher {
    
    /** 服务端已注册的模糊监听模式数量超过上限时触发。 */
    void onPatternOverLimit();
    
    /** 单个模式匹配到的配置数量超过上限时触发。 */
    void onConfigReachUpLimit();
    
}
