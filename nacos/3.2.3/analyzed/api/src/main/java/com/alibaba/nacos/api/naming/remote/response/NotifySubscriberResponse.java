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

package com.alibaba.nacos.api.naming.remote.response;

import com.alibaba.nacos.api.remote.response.Response;

/**
 * 订阅者处理服务变更通知的应答。
 *
 * <p>客户端收到 {@link com.alibaba.nacos.api.naming.remote.request.NotifySubscriberRequest} 并完成本地缓存更新后，向服务端返回此空 {@link Response} 表示 ACK。</p>
 *
 * @author liuzunfei
 * @version $Id: NotifySubscriberResponse.java, v 0.1 2020年08月06日 5:28 PM liuzunfei Exp $
 */
public class NotifySubscriberResponse extends Response {
    
}
