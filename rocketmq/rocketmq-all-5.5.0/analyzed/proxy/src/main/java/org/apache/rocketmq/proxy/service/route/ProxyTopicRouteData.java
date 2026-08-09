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
package org.apache.rocketmq.proxy.service.route;

import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.rocketmq.proxy.common.Address;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.QueueData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;

/**
 * Proxy 侧主题路由数据：将 Broker 地址转换为 {@link Address} 列表供客户端访问。
 */
public class ProxyTopicRouteData {
    public ProxyTopicRouteData() {
    }

    /** 从标准 {@link TopicRouteData} 转换，保留原始 Broker 主机与端口。 */
    public ProxyTopicRouteData(TopicRouteData topicRouteData) {
        this.queueDatas = topicRouteData.getQueueDatas();
        this.brokerDatas = new ArrayList<>();

        for (BrokerData brokerData : topicRouteData.getBrokerDatas()) {
            ProxyTopicRouteData.ProxyBrokerData proxyBrokerData = new ProxyTopicRouteData.ProxyBrokerData();
            proxyBrokerData.setCluster(brokerData.getCluster());
            proxyBrokerData.setBrokerName(brokerData.getBrokerName());
            brokerData.getBrokerAddrs().forEach((brokerId, brokerAddr) -> {
                HostAndPort brokerHostAndPort = HostAndPort.fromString(brokerAddr);

                proxyBrokerData.getBrokerAddrs().put(brokerId, Lists.newArrayList(new Address(brokerHostAndPort)));
            });
            this.brokerDatas.add(proxyBrokerData);
        }
    }

    /** 转换路由并将所有 Broker 地址端口替换为指定 proxy 端口。 */
    public ProxyTopicRouteData(TopicRouteData topicRouteData, int port) {
        this.queueDatas = topicRouteData.getQueueDatas();
        this.brokerDatas = new ArrayList<>();

        for (BrokerData brokerData : topicRouteData.getBrokerDatas()) {
            ProxyTopicRouteData.ProxyBrokerData proxyBrokerData = new ProxyTopicRouteData.ProxyBrokerData();
            proxyBrokerData.setCluster(brokerData.getCluster());
            proxyBrokerData.setBrokerName(brokerData.getBrokerName());
            brokerData.getBrokerAddrs().forEach((brokerId, brokerAddr) -> {
                HostAndPort brokerHostAndPort = HostAndPort.fromString(brokerAddr);
                HostAndPort proxyHostAndPort = HostAndPort.fromParts(brokerHostAndPort.getHost(), port);

                proxyBrokerData.getBrokerAddrs().put(brokerId, Lists.newArrayList(new Address(proxyHostAndPort)));
            });
            this.brokerDatas.add(proxyBrokerData);
        }
    }

    /** 转换路由并将各 Broker 地址统一映射为请求侧地址列表。 */
    public ProxyTopicRouteData(TopicRouteData topicRouteData, List<Address> requestHostAndPortList) {
        this.queueDatas = topicRouteData.getQueueDatas();
        this.brokerDatas = new ArrayList<>();

        for (BrokerData brokerData : topicRouteData.getBrokerDatas()) {
            ProxyTopicRouteData.ProxyBrokerData proxyBrokerData = new ProxyTopicRouteData.ProxyBrokerData();
            proxyBrokerData.setCluster(brokerData.getCluster());
            proxyBrokerData.setBrokerName(brokerData.getBrokerName());
            for (Long brokerId : brokerData.getBrokerAddrs().keySet()) {
                proxyBrokerData.getBrokerAddrs().put(brokerId, requestHostAndPortList);
            }
            this.brokerDatas.add(proxyBrokerData);
        }
    }

    /** Proxy 视角的 Broker 路由条目，地址为 {@link Address} 列表。 */
    public static class ProxyBrokerData {
        private String cluster;
        private String brokerName;
        private Map<Long/* brokerId */, List<Address>/* broker address */> brokerAddrs = new HashMap<>();

        public String getCluster() {
            return cluster;
        }

        public void setCluster(String cluster) {
            this.cluster = cluster;
        }

        public String getBrokerName() {
            return brokerName;
        }

        public void setBrokerName(String brokerName) {
            this.brokerName = brokerName;
        }

        public Map<Long, List<Address>> getBrokerAddrs() {
            return brokerAddrs;
        }

        public void setBrokerAddrs(Map<Long, List<Address>> brokerAddrs) {
            this.brokerAddrs = brokerAddrs;
        }

        /** 还原为 Remoting 协议使用的 {@link BrokerData}。 */
        public BrokerData buildBrokerData() {
            BrokerData brokerData = new BrokerData();
            brokerData.setCluster(cluster);
            brokerData.setBrokerName(brokerName);
            HashMap<Long, String> buildBrokerAddress = new HashMap<>();
            brokerAddrs.forEach((k, v) -> {
                if (!v.isEmpty()) {
                    buildBrokerAddress.put(k, v.get(0).getHostAndPort().toString());
                }
            });
            brokerData.setBrokerAddrs(buildBrokerAddress);
            return brokerData;
        }
    }

    private List<QueueData> queueDatas = new ArrayList<>();
    private List<ProxyBrokerData> brokerDatas = new ArrayList<>();

    public List<QueueData> getQueueDatas() {
        return queueDatas;
    }

    public void setQueueDatas(List<QueueData> queueDatas) {
        this.queueDatas = queueDatas;
    }

    public List<ProxyBrokerData> getBrokerDatas() {
        return brokerDatas;
    }

    public void setBrokerDatas(List<ProxyBrokerData> brokerDatas) {
        this.brokerDatas = brokerDatas;
    }

    /** 组装并返回标准 {@link TopicRouteData}。 */
    public TopicRouteData buildTopicRouteData() {
        TopicRouteData topicRouteData = new TopicRouteData();
        topicRouteData.setQueueDatas(queueDatas);
        topicRouteData.setBrokerDatas(brokerDatas.stream()
            .map(ProxyBrokerData::buildBrokerData)
            .collect(Collectors.toList()));
        return topicRouteData;
    }
}
