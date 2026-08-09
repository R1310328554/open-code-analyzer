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
package org.apache.rocketmq.tools.command.consumer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * getConsumerConfig 子命令：按订阅组名查询各 Broker 上的消费组配置。
 * <p>通过反射打印 {@link SubscriptionGroupConfig} 全部非空字段。
 */
public class GetConsumerConfigSubCommand implements SubCommand {

    @Override
    public String commandName() {
        return "getConsumerConfig";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Get consumer config by subscription group name.";
    }

    @Override
    public Options buildCommandlineOptions(final Options options) {
        Option opt = new Option("g", "groupName", true, "待查询的订阅组（消费组）名称");
        opt.setRequired(true);
        options.addOption(opt);
        return options;
    }

    @Override
    /** 遍历集群全部 Broker，拉取并打印订阅组配置字段。 */
    public void execute(CommandLine commandLine, Options options,
        RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt adminExt = new DefaultMQAdminExt(rpcHook);
        adminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        String groupName = commandLine.getOptionValue('g').trim();

        if (commandLine.hasOption('n')) {
            adminExt.setNamesrvAddr(commandLine.getOptionValue('n').trim());
        }

        try {
            adminExt.start();
            List<ConsumerConfigInfo> consumerConfigInfoList = new ArrayList<>();
            ClusterInfo clusterInfo = adminExt.examineBrokerClusterInfo();
            Map<String, Set<String>> clusterAddrTable = clusterInfo.getClusterAddrTable();
            // 遍历路由表中的每个 Broker
            for (Entry<String, BrokerData> brokerEntry : clusterInfo.getBrokerAddrTable().entrySet()) {
                String clusterName = this.getClusterName(brokerEntry.getKey(), clusterAddrTable);
                String brokerAddress = brokerEntry.getValue().selectBrokerAddr();
                SubscriptionGroupConfig subscriptionGroupConfig = adminExt.examineSubscriptionGroupConfig(brokerAddress, groupName);
                if (subscriptionGroupConfig == null) {
                    continue;
                }
                consumerConfigInfoList.add(new ConsumerConfigInfo(clusterName, brokerEntry.getKey(), subscriptionGroupConfig));
            }
            if (CollectionUtils.isEmpty(consumerConfigInfoList)) {
                return;
            }
            for (ConsumerConfigInfo info : consumerConfigInfoList) {
                System.out.printf("=============================%s:%s=============================\n",
                    info.getClusterName(), info.getBrokerName());
                SubscriptionGroupConfig config = info.getSubscriptionGroupConfig();
                // 反射遍历配置对象全部字段并打印
                Field[] fields = config.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (field.get(config) != null) {
                        System.out.printf("%s%-40s=  %s\n", "", field.getName(), field.get(config).toString());
                    } else {
                        System.out.printf("%s%-40s=  %s\n", "", field.getName(), "");
                    }
                }
            }
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            adminExt.shutdown();
        }
    }

    /** 根据 Broker 名反查所属集群名。 */
    private String getClusterName(String brokeName, Map<String, Set<String>> clusterAddrTable) {
        for (Map.Entry<String, Set<String>> entry : clusterAddrTable.entrySet()) {
            Set<String> brokerNameSet = entry.getValue();
            if (brokerNameSet.contains(brokeName)) {
                return entry.getKey();
            }
        }
        return null;
    }
}

/** 封装单个 Broker 上的消费组配置查询结果。 */
class ConsumerConfigInfo {
    private String clusterName;

    private String brokerName;

    private SubscriptionGroupConfig subscriptionGroupConfig;

    /** 构造消费组配置信息条目。 */
    public ConsumerConfigInfo(String clusterName, String brokerName, SubscriptionGroupConfig subscriptionGroupConfig) {
        this.clusterName = clusterName;
        this.brokerName = brokerName;
        this.subscriptionGroupConfig = subscriptionGroupConfig;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public SubscriptionGroupConfig getSubscriptionGroupConfig() {
        return subscriptionGroupConfig;
    }

    public void setSubscriptionGroupConfig(SubscriptionGroupConfig subscriptionGroupConfig) {
        this.subscriptionGroupConfig = subscriptionGroupConfig;
    }
}
