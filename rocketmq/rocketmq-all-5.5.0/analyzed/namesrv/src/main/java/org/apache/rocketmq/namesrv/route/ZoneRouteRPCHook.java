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
package org.apache.rocketmq.namesrv.route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.QueueData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;

/**
 * 多 Zone 路由 RPC 钩子：在客户端启用 Zone 模式时，按 Zone 名称过滤返回的路由数据。
 */
public class ZoneRouteRPCHook implements RPCHook {

    @Override
    /** 请求发出前钩子（当前无额外处理）。 */
    public void doBeforeRequest(String remoteAddr, RemotingCommand request) {

    }

    @Override
    /** 路由响应返回后，若启用 Zone 模式则按 Zone 名称裁剪 Broker 与队列信息。 */
    public void doAfterResponse(String remoteAddr, RemotingCommand request, RemotingCommand response) {
        if (RequestCode.GET_ROUTEINFO_BY_TOPIC != request.getCode()) {
            return;
        }
        if (response == null || response.getBody() == null || ResponseCode.SUCCESS != response.getCode()) {
            return;
        }
        boolean zoneMode = Boolean.parseBoolean(request.getExtFields().get(MixAll.ZONE_MODE));
        if (!zoneMode) {
            return;
        }
        String zoneName = request.getExtFields().get(MixAll.ZONE_NAME);
        if (StringUtils.isBlank(zoneName)) {
            return;
        }
        TopicRouteData topicRouteData = RemotingSerializable.decode(response.getBody(), TopicRouteData.class);
        response.setBody(filterByZoneName(topicRouteData, zoneName).encode());
    }

    /** 保留目标 Zone 及 Master 宕机需走 Slave 的 Broker，并同步清理队列与 FilterServer 表。 */
    private TopicRouteData filterByZoneName(TopicRouteData topicRouteData, String zoneName) {
        List<BrokerData> brokerDataReserved = new ArrayList<>();
        Map<String, BrokerData> brokerDataRemoved = new HashMap<>();
        for (BrokerData brokerData : topicRouteData.getBrokerDatas()) {
            if (brokerData.getBrokerAddrs() == null) {
                continue;
            }
            // Master 宕机时从 Slave 消费，需打破就近路由规则保留该 Broker
            if (brokerData.getBrokerAddrs().get(MixAll.MASTER_ID) == null
                || StringUtils.equalsIgnoreCase(brokerData.getZoneName(), zoneName)) {
                brokerDataReserved.add(brokerData);
            } else {
                brokerDataRemoved.put(brokerData.getBrokerName(), brokerData);
            }
        }
        topicRouteData.setBrokerDatas(brokerDataReserved);

        List<QueueData> queueDataReserved = new ArrayList<>();
        for (QueueData queueData : topicRouteData.getQueueDatas()) {
            if (!brokerDataRemoved.containsKey(queueData.getBrokerName())) {
                queueDataReserved.add(queueData);
            }
        }
        topicRouteData.setQueueDatas(queueDataReserved);
        // 按被移除 Broker 地址清理 FilterServer 映射表
        if (topicRouteData.getFilterServerTable() != null && !topicRouteData.getFilterServerTable().isEmpty()) {
            for (Entry<String, BrokerData> entry : brokerDataRemoved.entrySet()) {
                BrokerData brokerData = entry.getValue();
                brokerData.getBrokerAddrs().values()
                    .forEach(brokerAddr -> topicRouteData.getFilterServerTable().remove(brokerAddr));
            }
        }
        return topicRouteData;
    }
}
