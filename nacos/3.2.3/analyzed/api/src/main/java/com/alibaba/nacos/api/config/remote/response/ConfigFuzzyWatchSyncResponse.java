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

package com.alibaba.nacos.api.config.remote.response;

import com.alibaba.nacos.api.remote.response.Response;

/**
 * 模糊监听配置同步响应。
 *
 * <p>客户端与服务端同步模糊订阅状态时，通过本响应返回当前匹配的配置键集合。</p>
 *
 * @author stone-98
 * @date 2024/3/18
 */
public class ConfigFuzzyWatchSyncResponse extends Response {
    
}
