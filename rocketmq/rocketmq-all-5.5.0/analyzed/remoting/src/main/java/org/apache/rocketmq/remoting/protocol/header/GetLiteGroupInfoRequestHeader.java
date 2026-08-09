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

import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * 查询 Lite 消费组信息的请求头：指定消费组、Lite Topic 及 TopK 条数。
 */
public class GetLiteGroupInfoRequestHeader implements CommandCustomHeader {

    /** 目标消费组名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.GROUP)
    private String group;

    /** Lite Topic 名称，可选过滤条件。 */
    private String liteTopic;

    /** 返回 TopK 条记录数。 */
    private int topK;

    /** 返回消费组名称。 */
    public String getGroup() {
        return group;
    }

    /** 设置消费组名称。 */
    public void setGroup(String group) {
        this.group = group;
    }

    /** 返回 Lite Topic 名称。 */
    public String getLiteTopic() {
        return liteTopic;
    }

    /** 设置 Lite Topic 名称。 */
    public void setLiteTopic(String liteTopic) {
        this.liteTopic = liteTopic;
    }

    /** 返回 TopK 条数。 */
    public int getTopK() {
        return topK;
    }

    /** 设置 TopK 条数。 */
    public void setTopK(int topK) {
        this.topK = topK;
    }

    /** 校验请求头字段（空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }
}
