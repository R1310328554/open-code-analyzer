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
package org.apache.rocketmq.controller.impl.task;

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/** 扫描非活跃 Broker 的 Raft 请求：携带发起扫描时的毫秒时间戳作为判定基准。 */
public class CheckNotActiveBrokerRequest implements CommandCustomHeader {
    /** 扫描基准时刻，用于与最后心跳时间比较。 */
    private final Long checkTimeMillis = System.currentTimeMillis();

    public CheckNotActiveBrokerRequest() {
    }

    /** 返回扫描基准时间戳。 */
    public Long getCheckTimeMillis() {
        return checkTimeMillis;
    }

    @Override
    public void checkFields() throws RemotingCommandException {

    }

    @Override
    public String toString() {
        return "CheckNotActiveBrokerRequest{" +
            "checkTimeMillis=" + checkTimeMillis +
            '}';
    }
}
