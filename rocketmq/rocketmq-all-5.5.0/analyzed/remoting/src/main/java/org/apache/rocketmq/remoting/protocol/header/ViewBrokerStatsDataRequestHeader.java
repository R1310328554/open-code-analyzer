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

import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * 查看 Broker 统计数据的请求头：按 statsName 与 statsKey 查询集群级监控指标。
 */
@RocketMQAction(value = RequestCode.VIEW_BROKER_STATS_DATA, resource = ResourceType.CLUSTER, action = Action.GET)
public class ViewBrokerStatsDataRequestHeader implements CommandCustomHeader {
    /** 统计项名称。 */
    @CFNotNull
    private String statsName;
    /** 统计项键值（维度标识）。 */
    @CFNotNull
    private String statsKey;

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }

    /** 返回统计项名称。 */
    public String getStatsName() {
        return statsName;
    }

    /** 设置统计项名称。 */
    public void setStatsName(String statsName) {
        this.statsName = statsName;
    }

    /** 返回统计项键值。 */
    public String getStatsKey() {
        return statsKey;
    }

    /** 设置统计项键值。 */
    public void setStatsKey(String statsKey) {
        this.statsKey = statsKey;
    }
}
