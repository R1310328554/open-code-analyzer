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

package org.apache.rocketmq.remoting.protocol.namesrv;

import org.apache.rocketmq.remoting.protocol.body.KVTable;

/**
 * Broker 注册 NameServer 的结果体：HA 地址、Master 地址与 KV 配置表。
 */
public class RegisterBrokerResult {
    /** HA 高可用服务地址。 */
    private String haServerAddr;
    /** Master Broker 地址。 */
    private String masterAddr;
    /** NameServer 返回的 KV 配置表。 */
    private KVTable kvTable;

    /** 返回 HA 服务地址。 */
    public String getHaServerAddr() {
        return haServerAddr;
    }

    /** 设置 HA 服务地址。 */
    public void setHaServerAddr(String haServerAddr) {
        this.haServerAddr = haServerAddr;
    }

    /** 返回 Master 地址。 */
    public String getMasterAddr() {
        return masterAddr;
    }

    /** 设置 Master 地址。 */
    public void setMasterAddr(String masterAddr) {
        this.masterAddr = masterAddr;
    }

    /** 返回 KV 配置表。 */
    public KVTable getKvTable() {
        return kvTable;
    }

    /** 设置 KV 配置表。 */
    public void setKvTable(KVTable kvTable) {
        this.kvTable = kvTable;
    }
}
