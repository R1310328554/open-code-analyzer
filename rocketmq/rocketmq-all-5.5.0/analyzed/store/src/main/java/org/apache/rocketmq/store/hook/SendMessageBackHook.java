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

import java.util.List;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * 从节点回传消息钩子：HA 握手时按偏移将消息发回主节点。
 */
public interface SendMessageBackHook {

    /**
     * HA 握手阶段从节点将指定偏移的消息回传给主节点。
     *
     * @param msgList 待回传的消息列表
     * @param brokerName Broker 名称
     * @param brokerAddr Broker 地址
     * @return 回传成功返回 true
     */
    boolean executeSendMessageBack(List<MessageExt> msgList, String brokerName, String brokerAddr);
}
