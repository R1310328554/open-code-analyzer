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
package org.apache.rocketmq.remoting.protocol.header.namesrv;

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * 清除 Broker 写权限的响应头：返回受影响的 Topic 数量。
 */
public class WipeWritePermOfBrokerResponseHeader implements CommandCustomHeader {
    /** 被撤销写权限的 Topic 数量。 */
    @CFNotNull
    private Integer wipeTopicCount;

    /** 校验响应头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }

    /** 返回受影响的 Topic 数量。 */
    public Integer getWipeTopicCount() {
        return wipeTopicCount;
    }

    /** 设置受影响的 Topic 数量。 */
    public void setWipeTopicCount(Integer wipeTopicCount) {
        this.wipeTopicCount = wipeTopicCount;
    }
}
