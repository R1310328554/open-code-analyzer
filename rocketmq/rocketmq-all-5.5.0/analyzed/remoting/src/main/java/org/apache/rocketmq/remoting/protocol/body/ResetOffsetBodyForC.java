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

import java.util.List;
import org.apache.rocketmq.common.message.MessageQueueForC;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * C 语言客户端重置消费位点请求体：{@link MessageQueueForC} 列表。
 */
public class ResetOffsetBodyForC extends RemotingSerializable {

    /** 待重置的 C 风格队列位点列表。 */
    private List<MessageQueueForC> offsetTable;

    /** 返回位点列表。 */
    public List<MessageQueueForC> getOffsetTable() {
        return offsetTable;
    }

    /** 设置位点列表。 */
    public void setOffsetTable(List<MessageQueueForC> offsetTable) {
        this.offsetTable = offsetTable;
    }
}
