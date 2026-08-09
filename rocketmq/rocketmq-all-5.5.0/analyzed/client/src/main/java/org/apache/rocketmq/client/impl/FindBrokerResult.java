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
package org.apache.rocketmq.client.impl;

/**
 * 路由查找 Broker 的结果：包含 Broker 地址、是否从节点及版本号。
 */
public class FindBrokerResult {
    /** Broker 地址（ip:port）。 */
    private final String brokerAddr;
    /** 是否为 Broker 从节点。 */
    private final boolean slave;
    /** Broker 版本号，用于特性兼容判断。 */
    private final int brokerVersion;

    /** 构造结果，版本号默认为 0。 */
    public FindBrokerResult(String brokerAddr, boolean slave) {
        this.brokerAddr = brokerAddr;
        this.slave = slave;
        this.brokerVersion = 0;
    }

    /** 构造结果并指定 Broker 版本。 */
    public FindBrokerResult(String brokerAddr, boolean slave, int brokerVersion) {
        this.brokerAddr = brokerAddr;
        this.slave = slave;
        this.brokerVersion = brokerVersion;
    }

    /** 返回 Broker 地址。 */
    public String getBrokerAddr() {
        return brokerAddr;
    }

    /** 是否从节点。 */
    public boolean isSlave() {
        return slave;
    }

    /** 返回 Broker 版本号。 */
    public int getBrokerVersion() {
        return brokerVersion;
    }
}
