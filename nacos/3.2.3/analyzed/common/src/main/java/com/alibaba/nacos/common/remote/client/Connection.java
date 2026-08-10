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

package com.alibaba.nacos.common.remote.client;

import com.alibaba.nacos.api.ability.constant.AbilityKey;
import com.alibaba.nacos.api.ability.constant.AbilityStatus;
import com.alibaba.nacos.api.remote.Requester;

import java.util.Map;

/**
 * 客户端侧 RPC 连接抽象基类：封装连接 ID、目标服务器信息、能力表及废弃标记，
 * 具体传输实现（如 gRPC）继承此类并完成 {@link com.alibaba.nacos.api.remote.Requester} 请求发送。
 * connection on client side.
 *
 * @author liuzunfei
 * @version $Id: Connection.java, v 0.1 2020年08月09日 1:32 PM liuzunfei Exp $
 */
public abstract class Connection implements Requester {
    
    /** 服务端分配的唯一连接标识 */
    private String connectionId;
    
    /** 连接是否已废弃；废弃后不再触发连接事件回调 */
    private boolean abandon = false;
    
    /** 当前连接对应的服务器 IP 与端口 */
    protected RpcClient.ServerInfo serverInfo;
    
    /** 服务端能力协商表：能力名 → 是否支持 */
    protected Map<String, Boolean> abilityTable;
    
    public Connection(RpcClient.ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }
    
    public String getConnectionId() {
        return connectionId;
    }
    
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
    
    /** 查询当前连接对指定 {@link AbilityKey} 的支持状态 */
    public AbilityStatus getConnectionAbility(AbilityKey abilityKey) {
        if (abilityTable == null || !abilityTable.containsKey(abilityKey.getName())) {
            return AbilityStatus.UNKNOWN;
        }
        return abilityTable.get(abilityKey.getName()) ? AbilityStatus.SUPPORTED
            : AbilityStatus.NOT_SUPPORTED;
    }
    
    /** 能力表是否已从服务端同步 */
    public boolean isAbilitiesSet() {
        return abilityTable != null;
    }
    
    /** 设置协商后的能力表 */
    public void setAbilityTable(Map<String, Boolean> abilityTable) {
        this.abilityTable = abilityTable;
    }
    
    /**
     * Getter method for property <tt>abandon</tt>.
     *
     * @return property value of abandon
      * <p>客户端 RPC 连接抽象；详见类级说明。</p>
     */
    public boolean isAbandon() {
        return abandon;
    }
    
    /**
     * Setter method for property <tt>abandon</tt>. connection event will be ignored if connection is abandoned.
     *
     * @param abandon value to be assigned to property abandon
      * <p>客户端 RPC 连接抽象；详见类级说明。</p>
     */
    public void setAbandon(boolean abandon) {
        this.abandon = abandon;
    }
    
}
