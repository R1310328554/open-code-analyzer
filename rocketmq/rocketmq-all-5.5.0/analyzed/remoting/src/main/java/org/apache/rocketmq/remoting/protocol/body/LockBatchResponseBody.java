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

package org.apache.rocketmq.remoting.protocol.body;

import java.util.HashSet;
import java.util.Set;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 顺序消费批量加锁响应：返回 Broker 成功锁定的 MessageQueue 集合。
 */
public class LockBatchResponseBody extends RemotingSerializable {

    /** 加锁成功的 MessageQueue 集合。 */
    private Set<MessageQueue> lockOKMQSet = new HashSet<>();

    /** 返回加锁成功队列。 */
    public Set<MessageQueue> getLockOKMQSet() {
        return lockOKMQSet;
    }

    /** 设置加锁成功队列。 */
    public void setLockOKMQSet(Set<MessageQueue> lockOKMQSet) {
        this.lockOKMQSet = lockOKMQSet;
    }

}
