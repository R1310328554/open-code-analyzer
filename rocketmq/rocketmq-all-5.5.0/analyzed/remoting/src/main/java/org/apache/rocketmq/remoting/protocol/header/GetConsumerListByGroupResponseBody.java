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

import java.util.List;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 按消费组查询 Consumer 列表的响应体：返回客户端 ID 列表。
 */
public class GetConsumerListByGroupResponseBody extends RemotingSerializable {
    /** 消费组下在线 Consumer 客户端 ID 列表。 */
    private List<String> consumerIdList;

    /** 返回 Consumer 客户端 ID 列表。 */
    public List<String> getConsumerIdList() {
        return consumerIdList;
    }

    /** 设置 Consumer 客户端 ID 列表。 */
    public void setConsumerIdList(List<String> consumerIdList) {
        this.consumerIdList = consumerIdList;
    }
}
