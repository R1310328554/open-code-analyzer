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

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * 查询 Lite 消费客户端信息的请求头：指定父 Topic、消费组、客户端 ID 及返回条数上限。
 */
public class GetLiteClientInfoRequestHeader implements CommandCustomHeader {

    /** 父 Topic 名称。 */
    private String parentTopic;
    /** 消费组名称。 */
    private String group;
    /** Consumer 客户端 ID。 */
    private String clientId;
    /** 最大返回条数，默认 1000。 */
    private int maxCount = 1000;

    /** 校验 maxCount 必须大于 0。 */
    @Override
    public void checkFields() throws RemotingCommandException {
        if (maxCount <= 0) {
            throw new RemotingCommandException("[maxCount] field invalid");
        }
    }

    /** 返回父 Topic 名称。 */
    public String getParentTopic() {
        return parentTopic;
    }

    /** 设置父 Topic 名称。 */
    public void setParentTopic(String parentTopic) {
        this.parentTopic = parentTopic;
    }

    /** 返回消费组名称。 */
    public String getGroup() {
        return group;
    }

    /** 设置消费组名称。 */
    public void setGroup(String group) {
        this.group = group;
    }

    /** 返回客户端 ID。 */
    public String getClientId() {
        return clientId;
    }

    /** 设置客户端 ID。 */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** 返回最大返回条数。 */
    public int getMaxCount() {
        return maxCount;
    }

    /** 设置最大返回条数。 */
    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }
}
