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

package com.alibaba.nacos.client.address;

import com.alibaba.nacos.common.notify.SlowEvent;

/**
 * Server List Change Event.
 * <p>服务端地址列表变更慢事件：由 {@link EndpointServerListProvider} 在 endpoint 拉取结果变化时通过 {@link com.alibaba.nacos.common.notify.NotifyCenter} 发布，供客户端重连或刷新路由。</p>
 *
 * @author zongtanghu
 */
public class ServerListChangeEvent extends SlowEvent {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = -1655577508567092395L;
}
