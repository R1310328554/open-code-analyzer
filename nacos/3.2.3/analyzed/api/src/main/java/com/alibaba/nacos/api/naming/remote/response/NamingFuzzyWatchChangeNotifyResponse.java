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

package com.alibaba.nacos.api.naming.remote.response;

import com.alibaba.nacos.api.remote.response.Response;

/**
 * 模糊监听单服务变更通知的客户端应答。
 *
 * <p>客户端收到 {@link com.alibaba.nacos.api.naming.remote.request.NamingFuzzyWatchChangeNotifyRequest} 后返回此空响应，表示已成功处理变更通知。</p>
 *
 * @author tanyongquan
 */
public class NamingFuzzyWatchChangeNotifyResponse extends Response {
    
}
