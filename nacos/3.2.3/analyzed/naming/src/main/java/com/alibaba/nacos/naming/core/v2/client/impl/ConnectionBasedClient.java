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

package com.alibaba.nacos.naming.core.v2.client.impl;

import com.alibaba.nacos.naming.core.v2.client.AbstractClient;
import com.alibaba.nacos.naming.misc.ClientConfig;

/**
 * 基于 TCP 会话的 Nacos 命名客户端。
 *
 * <p>客户端与远程连接绑定；连接断开时应清理实例与订阅。</p>
 *
 * @author xiweng.yy
 */
public class ConnectionBasedClient extends AbstractClient {
    
    /** 远程连接 ID，即客户端唯一标识。 */
    private final String connectionId;
    
    /**
     * {@code true} 表示本机直连客户端；{@code false} 表示从其他节点 Distro 同步的客户端。
     */
    private final boolean isNative;
    
    /** 仅对非本机客户端有意义：上次从源节点 Distro 校验成功的时间戳。 */
    /**
     * Only has meaning when {@code isNative} is false, which means that the last time verify from source server.
      * <p>Nacos 命名 V2 客户端工厂、管理器与事件模型；详见上方类/接口说明。</p>
     */
    private volatile long lastRenewTime;
    
    public ConnectionBasedClient(String connectionId, boolean isNative, Long revision) {
        super(revision);
        this.connectionId = connectionId;
        this.isNative = isNative;
        lastRenewTime = getLastUpdatedTime();
    }
    
    @Override
    public String getClientId() {
        return connectionId;
    }
    
    @Override
    public boolean isEphemeral() {
        return true;
    }
    
    /** 是否为本机直连客户端。 */
    public boolean isNative() {
        return isNative;
    }
    
    public long getLastRenewTime() {
        return lastRenewTime;
    }
    
    /** 更新 Distro 校验续期时间戳。 */
    public void setLastRenewTime() {
        this.lastRenewTime = System.currentTimeMillis();
    }
    
    @Override
    public boolean isExpire(long currentTime) {
        return !isNative()
            && currentTime - getLastRenewTime() > ClientConfig.getInstance().getClientExpiredTime();
    }
    
    @Override
    public long recalculateRevision() {
        return revision.addAndGet(1);
    }
}
