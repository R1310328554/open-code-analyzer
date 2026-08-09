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
package org.apache.rocketmq.client;

import java.util.List;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * 消息索引查询结果：包含索引最后更新时间戳与匹配的消息列表。
 */
public class QueryResult {
    private final long indexLastUpdateTimestamp;
    private final List<MessageExt> messageList;

    /** 构造查询结果。 */
    public QueryResult(long indexLastUpdateTimestamp, List<MessageExt> messageList) {
        this.indexLastUpdateTimestamp = indexLastUpdateTimestamp;
        this.messageList = messageList;
    }

    /** 返回索引最后更新时间戳。 */
    public long getIndexLastUpdateTimestamp() {
        return indexLastUpdateTimestamp;
    }

    /** 返回匹配的消息列表。 */
    public List<MessageExt> getMessageList() {
        return messageList;
    }

    @Override
    public String toString() {
        return "QueryResult [indexLastUpdateTimestamp=" + indexLastUpdateTimestamp + ", messageList="
            + messageList + "]";
    }
}
