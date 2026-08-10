/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.model.response;

import com.alibaba.nacos.api.ability.ServerAbilities;
import com.alibaba.nacos.api.common.NodeState;
import com.alibaba.nacos.api.utils.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Nacos 集群成员节点信息。
 *
 * <p>描述成员 IP、端口、运行状态、扩展信息及（已废弃的）服务端能力。</p>
 *
 * @author xiweng.yy
 */
public class NacosMember implements Serializable {
    
    private static final long serialVersionUID = 6295022126554026016L;
    
    /** 成员节点 IP 地址。 */
    private String ip;
    
    /** 成员节点端口，默认 -1 表示未设置。 */
    private int port = -1;
    
    /** 节点运行状态（UP/DOWN 等）。 */
    private volatile NodeState state = NodeState.UP;
    
    /** 扩展信息键值对（线程安全有序 Map）。 */
    private Map<String, Object> extendInfo = Collections.synchronizedMap(new TreeMap<>());
    
    /** 节点地址字符串（ip:port）。 */
    private String address = "";
    
    /** @deprecated 服务端能力描述，已废弃。 */
    @Deprecated
    private ServerAbilities abilities = new ServerAbilities();
    
    /** 获取成员 IP。 */
    public String getIp() {
        return ip;
    }
    
    /** 设置成员 IP，并同步更新 address。 */
    public void setIp(String ip) {
        this.ip = ip;
        this.address = ip + ":" + port;
    }
    
    /** 获取成员端口。 */
    public int getPort() {
        return port;
    }
    
    /** 设置成员端口，并同步更新 address。 */
    public void setPort(int port) {
        this.port = port;
        this.address = ip + ":" + port;
    }
    
    /** 获取节点状态。 */
    public NodeState getState() {
        return state;
    }
    
    /** 设置节点状态。 */
    public void setState(NodeState state) {
        this.state = state;
    }
    
    /** 获取扩展信息。 */
    public Map<String, Object> getExtendInfo() {
        return extendInfo;
    }
    
    /** 设置扩展信息。 */
    public void setExtendInfo(Map<String, Object> extendInfo) {
        this.extendInfo = extendInfo;
    }
    
    /** 获取节点地址（ip:port）。 */
    public String getAddress() {
        return address;
    }
    
    /** 设置节点地址。 */
    public void setAddress(String address) {
        this.address = address;
    }
    
    /** @deprecated 获取服务端能力，已废弃。 */
    public ServerAbilities getAbilities() {
        return abilities;
    }
    
    /** @deprecated 设置服务端能力，已废弃。 */
    public void setAbilities(ServerAbilities abilities) {
        this.abilities = abilities;
    }
    
    /** 按 IP 与端口判断成员是否相等。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NacosMember that = (NacosMember) o;
        return port == that.port && StringUtils.equals(ip, that.ip);
    }
    
    /** 返回成员摘要字符串。 */
    @Override
    public String toString() {
        return "Member{" + "ip='" + ip + '\'' + ", port=" + port + ", state=" + state
            + ", extendInfo=" + extendInfo
            + '}';
    }
    
    /** 基于 IP 与端口计算哈希码。 */
    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }
}
