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
package org.apache.rocketmq.broker.transaction.queue;

import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.common.message.MessageExt;

/** 事务队列拉取结果封装：关联单条 {@link MessageExt} 与 {@link PullResult}。 */
public class GetResult {
    private MessageExt msg;
    private PullResult pullResult;

    /** 返回拉取到的单条消息。 */
    public MessageExt getMsg() {
        return msg;
    }

    /** 设置拉取到的单条消息。 */
    public void setMsg(MessageExt msg) {
        this.msg = msg;
    }

    /** 返回底层 Pull 结果（含 offset 与状态）。 */
    public PullResult getPullResult() {
        return pullResult;
    }

    /** 设置底层 Pull 结果。 */
    public void setPullResult(PullResult pullResult) {
        this.pullResult = pullResult;
    }
}
