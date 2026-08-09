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
package org.apache.rocketmq.store.hook;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.store.PutMessageResult;

/**
 * 写消息前置钩子：在消息写入 CommitLog 前执行校验或转换。
 */
public interface PutMessageHook {

    /**
     * 钩子名称，用于标识与日志。
     *
     * @return 钩子名称
     */
    String hookName();

    /**
     * 写消息前执行，例如消息校验或特殊消息转换。
     * @param msg 待写入的消息
     * @return 写消息结果，非 OK 则中断写入
     */
    PutMessageResult executeBeforePutMessage(MessageExt msg);
}
