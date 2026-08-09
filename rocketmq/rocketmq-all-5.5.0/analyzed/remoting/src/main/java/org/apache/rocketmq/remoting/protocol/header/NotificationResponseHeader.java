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
package org.apache.rocketmq.remoting.protocol.header;

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * Pop 长轮询通知响应头：告知 Consumer 队列是否有新消息及轮询队列是否已满。
 */
public class NotificationResponseHeader implements CommandCustomHeader {


    /** 队列是否有新消息可拉取。 */
    @CFNotNull
    private boolean hasMsg = false;

    /** 长轮询等待队列是否已满（Broker 侧背压信号）。 */
    private boolean pollingFull = false;

    /** 返回是否有新消息。 */
    public boolean isHasMsg() {
        return hasMsg;
    }

    /** 返回轮询队列是否已满。 */
    public boolean isPollingFull() {
        return pollingFull;
    }

    /** 设置轮询队列是否已满。 */
    public void setPollingFull(boolean pollingFull) {
        this.pollingFull = pollingFull;
    }

    /** 设置是否有新消息。 */
    public void setHasMsg(boolean hasMsg) {
        this.hasMsg = hasMsg;
    }

    /** 校验响应头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }
}
