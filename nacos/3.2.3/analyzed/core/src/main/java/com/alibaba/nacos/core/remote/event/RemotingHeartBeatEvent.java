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

package com.alibaba.nacos.core.remote.event;

import com.alibaba.nacos.common.notify.Event;

/**
 * 远程连接心跳事件，携带连接标识与客户端信息供监控/通知使用。
 * Remoting connection heart beat event.
 *
 * @author xiweng.yy
 */
public class RemotingHeartBeatEvent extends Event {
    
    /** 连接唯一标识。 */
    private final String connectionId;
    
    /** 客户端 IP 地址。 */
    private final String clientIp;
    
    /** 客户端版本号。 */
    private final String clientVersion;
    
    /** 构造心跳事件。
     * @param connectionId 连接 ID
     * @param clientIp 客户端 IP
     * @param clientVersion 客户端版本
     */
    public RemotingHeartBeatEvent(String connectionId, String clientIp, String clientVersion) {
        this.connectionId = connectionId;
        this.clientIp = clientIp;
        this.clientVersion = clientVersion;
    }
    
    /** 获取连接 ID。 */
    public String getConnectionId() {
        return connectionId;
    }
    
    /** 获取客户端 IP。 */
    public String getClientIp() {
        return clientIp;
    }
    
    /** 获取客户端版本。 */
    public String getClientVersion() {
        return clientVersion;
    }
}
