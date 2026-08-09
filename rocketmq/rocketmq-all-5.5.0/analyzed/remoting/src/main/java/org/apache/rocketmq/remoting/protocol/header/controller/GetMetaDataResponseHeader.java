/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.remoting.protocol.header.controller;

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * Controller 元数据查询响应头：返回 Controller 组、Leader 信息、本节点是否为 Leader 及 peers 列表。
 */
public class GetMetaDataResponseHeader implements CommandCustomHeader {
    /** Controller 组名称。 */
    private String group;
    /** 当前 Controller Leader 的节点 ID。 */
    private String controllerLeaderId;
    /** 当前 Controller Leader 的网络地址。 */
    private String controllerLeaderAddress;
    /** 本节点是否为 Controller Leader。 */
    private boolean isLeader;
    /** Controller 集群 peers 信息（序列化字符串）。 */
    private String peers;

    /** 默认构造。 */
    public GetMetaDataResponseHeader() {
    }

    /** 指定组、Leader 信息、本节点角色与 peers 的构造。 */
    public GetMetaDataResponseHeader(String group, String controllerLeaderId, String controllerLeaderAddress, boolean isLeader, String peers) {
        this.group = group;
        this.controllerLeaderId = controllerLeaderId;
        this.controllerLeaderAddress = controllerLeaderAddress;
        this.isLeader = isLeader;
        this.peers = peers;
    }

    /** 返回 Controller 组名称。 */
    public String getGroup() {
        return group;
    }

    /** 设置 Controller 组名称。 */
    public void setGroup(String group) {
        this.group = group;
    }

    /** 返回 Controller Leader 节点 ID。 */
    public String getControllerLeaderId() {
        return controllerLeaderId;
    }

    /** 设置 Controller Leader 节点 ID。 */
    public void setControllerLeaderId(String controllerLeaderId) {
        this.controllerLeaderId = controllerLeaderId;
    }

    /** 返回 Controller Leader 地址。 */
    public String getControllerLeaderAddress() {
        return controllerLeaderAddress;
    }

    /** 设置 Controller Leader 地址。 */
    public void setControllerLeaderAddress(String controllerLeaderAddress) {
        this.controllerLeaderAddress = controllerLeaderAddress;
    }

    /** 返回本节点是否为 Leader。 */
    public boolean isLeader() {
        return isLeader;
    }

    /** 设置本节点是否为 Leader。 */
    public void setIsLeader(boolean leader) {
        isLeader = leader;
    }

    /** 返回 peers 信息字符串。 */
    public String getPeers() {
        return peers;
    }

    /** 设置 peers 信息字符串。 */
    public void setPeers(String peers) {
        this.peers = peers;
    }

    /** 返回含组、Leader 与 peers 的调试字符串。 */
    @Override
    public String toString() {
        return "GetMetaDataResponseHeader{" +
            "group='" + group + '\'' +
            ", controllerLeaderId='" + controllerLeaderId + '\'' +
            ", controllerLeaderAddress='" + controllerLeaderAddress + '\'' +
            ", isLeader=" + isLeader +
            ", peers='" + peers + '\'' +
            '}';
    }

    /** 校验响应头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }
}
