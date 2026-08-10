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

package com.alibaba.nacos.api.naming.listener;

/**
 * 模糊订阅负载超限监听器，在加载器超过配额限制时触发回调。
 *
 * <p>用于感知服务端模糊订阅模式数量或匹配服务数量达到上限时的告警场景。</p>
 *
 * @author shiyiyue
 */
public interface FuzzyWatchLoadWatcher {
    
    /**
     * 当服务端模糊订阅模式（pattern）数量超过上限时触发。
     */
    void onPatternOverLimit();
    
    /**
     * 当单个模式匹配到的服务数量超过上限时触发。
     */
    void onServiceReachUpLimit();
    
}
