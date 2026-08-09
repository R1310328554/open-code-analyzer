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
package org.apache.rocketmq.tools.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.tools.admin.MQAdminExt;

/**
 * mqadmin 命令行工具辅助类：按集群/Broker 名解析 Master/Slave 地址。
 */
public class CommandUtil {

    /** 集群名不存在或 NameServer 连接异常时的提示信息。 */
    private static final String ERROR_MESSAGE = "Make sure the specified clusterName exists or the name server connected to is correct.";

    /** 无 Master 时的占位键。 */
    public static final String NO_MASTER_PLACEHOLDER = "NO_MASTER";

    /** 按集群名拉取 Master 地址到 Slave 地址列表的映射。 */
    public static Map<String/*master addr*/, List<String>/*slave addr*/> fetchMasterAndSlaveDistinguish(
        final MQAdminExt adminExt, final String clusterName)
        throws InterruptedException, RemotingConnectException,
        RemotingTimeoutException, RemotingSendRequestException,
        MQBrokerException {
        Map<String, List<String>> masterAndSlaveMap = new HashMap<>(4);

        ClusterInfo clusterInfoSerializeWrapper = adminExt.examineBrokerClusterInfo();
        Set<String> brokerNameSet = clusterInfoSerializeWrapper.getClusterAddrTable().get(clusterName);

        if (brokerNameSet == null) {
            System.out.printf("[error] %s", ERROR_MESSAGE);
            return masterAndSlaveMap;
        }

        for (String brokerName : brokerNameSet) {
            BrokerData brokerData = clusterInfoSerializeWrapper.getBrokerAddrTable().get(brokerName);

            if (brokerData == null || brokerData.getBrokerAddrs() == null) {
                continue;
            }

            String masterAddr = brokerData.getBrokerAddrs().get(MixAll.MASTER_ID);

            if (masterAddr == null) {
                masterAndSlaveMap.putIfAbsent(NO_MASTER_PLACEHOLDER, new ArrayList<>());
            } else {
                masterAndSlaveMap.put(masterAddr, new ArrayList<>());
            }

            for (Entry<Long, String> brokerAddrEntry : brokerData.getBrokerAddrs().entrySet()) {
                if (brokerAddrEntry.getValue() == null || brokerAddrEntry.getKey() == MixAll.MASTER_ID) {
                    continue;
                }

                if (masterAddr == null) {
                    masterAndSlaveMap.get(NO_MASTER_PLACEHOLDER).add(brokerAddrEntry.getValue());
                } else {
                    masterAndSlaveMap.get(masterAddr).add(brokerAddrEntry.getValue());
                }
            }
        }

        return masterAndSlaveMap;
    }

    /** 返回指定集群内全部 Broker 的 Master 地址集合。 */
    public static Set<String> fetchMasterAddrByClusterName(final MQAdminExt adminExt, final String clusterName)
        throws InterruptedException, RemotingConnectException, RemotingTimeoutException,
        RemotingSendRequestException, MQBrokerException {
        Set<String> masterSet = new HashSet<>();

        ClusterInfo clusterInfoSerializeWrapper = adminExt.examineBrokerClusterInfo();

        Set<String> brokerNameSet = clusterInfoSerializeWrapper.getClusterAddrTable().get(clusterName);

        if (brokerNameSet != null) {
            for (String brokerName : brokerNameSet) {
                BrokerData brokerData = clusterInfoSerializeWrapper.getBrokerAddrTable().get(brokerName);
                if (brokerData != null) {

                    String addr = brokerData.getBrokerAddrs().get(MixAll.MASTER_ID);
                    if (addr != null) {
                        masterSet.add(addr);
                    }
                }
            }
        } else {
            System.out.printf("[error] %s", ERROR_MESSAGE);
        }

        return masterSet;
    }

    /** 返回指定集群内全部 Broker（Master + Slave）地址集合。 */
    public static Set<String> fetchMasterAndSlaveAddrByClusterName(final MQAdminExt adminExt, final String clusterName)
        throws InterruptedException, RemotingConnectException, RemotingTimeoutException,
        RemotingSendRequestException, MQBrokerException {
        Set<String> brokerAddressSet = new HashSet<>();
        ClusterInfo clusterInfoSerializeWrapper = adminExt.examineBrokerClusterInfo();
        Set<String> brokerNameSet = clusterInfoSerializeWrapper.getClusterAddrTable().get(clusterName);
        if (brokerNameSet != null) {
            for (String brokerName : brokerNameSet) {
                BrokerData brokerData = clusterInfoSerializeWrapper.getBrokerAddrTable().get(brokerName);
                if (brokerData != null) {
                    final Collection<String> addrs = brokerData.getBrokerAddrs().values();
                    brokerAddressSet.addAll(addrs);
                }
            }
        } else {
            System.out.printf("[error] %s", ERROR_MESSAGE);
        }

        return brokerAddressSet;
    }

    /** 根据 Broker 名解析 Master 地址；不存在则抛异常。 */
    public static String fetchMasterAddrByBrokerName(final MQAdminExt adminExt,
        final String brokerName) throws Exception {
        ClusterInfo clusterInfoSerializeWrapper = adminExt.examineBrokerClusterInfo();
        BrokerData brokerData = clusterInfoSerializeWrapper.getBrokerAddrTable().get(brokerName);
        if (null != brokerData) {
            String addr = brokerData.getBrokerAddrs().get(MixAll.MASTER_ID);
            if (addr != null) {
                return addr;
            }
        }
        throw new Exception(String.format("No broker address for broker name %s.", brokerName));
    }

    /** 返回指定 Broker 的全部角色地址（Master + Slave）。 */
    public static Set<String> fetchMasterAndSlaveAddrByBrokerName(final MQAdminExt adminExt, final String brokerName)
        throws InterruptedException, RemotingConnectException, RemotingTimeoutException,
        RemotingSendRequestException, MQBrokerException {
        Set<String> brokerAddressSet = new HashSet<>();
        ClusterInfo clusterInfoSerializeWrapper = adminExt.examineBrokerClusterInfo();
        final BrokerData brokerData = clusterInfoSerializeWrapper.getBrokerAddrTable().get(brokerName);
        if (brokerData != null) {
            brokerAddressSet.addAll(brokerData.getBrokerAddrs().values());
        }
        return brokerAddressSet;
    }

    /** 返回集群内全部 Broker 名称；集群不存在则抛异常。 */
    public static Set<String> fetchBrokerNameByClusterName(final MQAdminExt adminExt, final String clusterName)
        throws Exception {
        ClusterInfo clusterInfoSerializeWrapper = adminExt.examineBrokerClusterInfo();
        Set<String> brokerNameSet = clusterInfoSerializeWrapper.getClusterAddrTable().get(clusterName);
        if (brokerNameSet == null || brokerNameSet.isEmpty()) {
            throw new Exception(ERROR_MESSAGE);
        }
        return brokerNameSet;
    }

    /** 根据 Broker 地址反查 Broker 名称。 */
    public static String fetchBrokerNameByAddr(final MQAdminExt adminExt, final String addr) throws Exception {
        ClusterInfo clusterInfoSerializeWrapper = adminExt.examineBrokerClusterInfo();
        Map<String/* brokerName */, BrokerData> brokerAddrTable = clusterInfoSerializeWrapper.getBrokerAddrTable();
        Iterator<Map.Entry<String, BrokerData>> it = brokerAddrTable.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, BrokerData> entry = it.next();
            HashMap<Long, String> brokerAddrs = entry.getValue().getBrokerAddrs();
            if (brokerAddrs.containsValue(addr)) {
                return entry.getKey();
            }
        }
        throw new Exception(ERROR_MESSAGE);
    }
}
