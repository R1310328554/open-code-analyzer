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

import java.util.HashMap;
import java.util.Map;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 管理端重置消费位点请求体：MessageQueue → 目标位点映射。
 */
public class ResetOffsetBody extends RemotingSerializable {

    /** 待重置的队列与目标位点。 */
    private Map<MessageQueue, Long> offsetTable;

    /** 默认构造，初始化空位点表。 */
    public ResetOffsetBody() {
        offsetTable = new HashMap<>();
    }

    /** 返回位点映射表。 */
    public Map<MessageQueue, Long> getOffsetTable() {
        return offsetTable;
    }

    /** 设置位点映射表。 */
    public void setOffsetTable(Map<MessageQueue, Long> offsetTable) {
        this.offsetTable = offsetTable;
    }
}
