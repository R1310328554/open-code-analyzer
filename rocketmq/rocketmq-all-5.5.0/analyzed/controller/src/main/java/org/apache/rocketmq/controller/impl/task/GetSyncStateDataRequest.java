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

/** 获取同步状态集数据的 Raft 请求，记录发起调用的毫秒时间戳。 */
public class GetSyncStateDataRequest implements CommandCustomHeader {
    /** 请求发起时刻，供状态机按时间点判断 Broker 活跃性。 */
    private final Long invokeTime = System.currentTimeMillis();

    @Override
    public void checkFields() throws RemotingCommandException {

    }

    public GetSyncStateDataRequest() {

    }

    /** 返回请求发起时间戳。 */
    public Long getInvokeTime() {
        return invokeTime;
    }

    @Override
    public String toString() {
        return "GetSyncStateDataRequest{" +
            "invokeTime=" + invokeTime +
            '}';
    }
}
