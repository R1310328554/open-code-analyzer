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
package org.apache.rocketmq.common;

import java.util.Set;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * 消费端队列加锁结果回调。
 */
public interface LockCallback {
    /** 加锁成功，返回已锁定队列集合。 */
    void onSuccess(final Set<MessageQueue> lockOKMQSet);

    /** 加锁失败或异常。 */
    void onException(final Throwable e);
}
