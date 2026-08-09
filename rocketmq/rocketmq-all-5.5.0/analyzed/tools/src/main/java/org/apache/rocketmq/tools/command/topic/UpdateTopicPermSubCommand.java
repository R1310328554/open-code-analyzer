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
package org.apache.rocketmq.tools.command.topic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.QueueData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.srvutil.ServerUtil;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * updateTopicPerm 子命令：更新 Topic 读写权限。
 * <p>权限值：2=写、4=读、6=读写；可指定 Broker 或整个集群。
 */
public class UpdateTopicPermSubCommand implements SubCommand {

    @Override
    public String commandName() {
        return "updateTopicPerm";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Update topic perm.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("b", "brokerAddr", true, "目标 Broker 地址");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("c", "clusterName", true, "目标集群名称");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("t", "topic", true, "Topic 名称");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("p", "perm", true, "Topic 权限（2=写，4=读，6=读写）");
        opt.setRequired(true);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 读取路由信息并更新指定 Broker 或集群的 Topic 权限。 */
    public void execute(final CommandLine commandLine, final Options options,
        RPCHook rpcHook) throws SubCommandException {

        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            defaultMQAdminExt.start();

            TopicConfig topicConfig = new TopicConfig();
            String topic;
            if (commandLine.hasOption('t')) {
                topic = commandLine.getOptionValue('t').trim();
            } else {
                System.out.printf("topic parameter value must be need.%n");
                return;
            }
            TopicRouteData topicRouteData = defaultMQAdminExt.examineTopicRouteInfo(topic);
            assert topicRouteData != null;
            List<QueueData> queueDatas = topicRouteData.getQueueDatas();
            assert queueDatas != null && queueDatas.size() > 0;
            QueueData queueData = queueDatas.get(0);
            topicConfig.setTopicName(topic);
            topicConfig.setWriteQueueNums(queueData.getWriteQueueNums());
            topicConfig.setReadQueueNums(queueData.getReadQueueNums());
            topicConfig.setTopicSysFlag(queueData.getTopicSysFlag());
            // 解析并设置新的读写权限位
            int perm;
            if (commandLine.hasOption('p')) {
                perm = Integer.parseInt(commandLine.getOptionValue('p').trim());
            } else {
                System.out.printf("perm parameter value must be need.%n");
                return;
            }
            topicConfig.setPerm(perm);
            if (commandLine.hasOption('b')) {
                String brokerAddr = commandLine.getOptionValue('b').trim();
                List<BrokerData> brokerDatas = topicRouteData.getBrokerDatas();
                String brokerName = null;
                // 按 Master Broker 地址反查 brokerName
                for (BrokerData data : brokerDatas) {
                    HashMap<Long, String> brokerAddrs = data.getBrokerAddrs();
                    if (brokerAddrs == null || brokerAddrs.size() == 0) {
                        continue;
                    }
                    for (Map.Entry<Long, String> entry : brokerAddrs.entrySet()) {
                        if (brokerAddr.equals(entry.getValue()) && MixAll.MASTER_ID == entry.getKey()) {
                            brokerName = data.getBrokerName();
                            break;
                        }
                    }
                    if (brokerName != null) {
                        break;
                    }
                }

                if (brokerName != null) {
                    List<QueueData> queueDataList = topicRouteData.getQueueDatas();
                    assert queueDataList != null && queueDataList.size() > 0;
                    int oldPerm = 0;
                    for (QueueData data : queueDataList) {
                        if (brokerName.equals(data.getBrokerName())) {
                            oldPerm = data.getPerm();
                            if (perm == oldPerm) {
                                System.out.printf("new perm equals to the old one!%n");
                                return;
                            }
                            break;
                        }
                    }
                    defaultMQAdminExt.createAndUpdateTopicConfig(brokerAddr, topicConfig);
                    System.out.printf("update topic perm from %s to %s in %s success.%n", oldPerm, perm, brokerAddr);
                    System.out.printf("%s.%n", topicConfig);
                    return;
                } else {
                    System.out.printf("updateTopicPerm error broker not exit or broker is not master!.%n");
                    return;
                }

            // 向集群全部 Master 批量更新权限
            } else if (commandLine.hasOption('c')) {
                String clusterName = commandLine.getOptionValue('c').trim();
                Set<String> masterSet =
                    CommandUtil.fetchMasterAddrByClusterName(defaultMQAdminExt, clusterName);
                for (String addr : masterSet) {
                    defaultMQAdminExt.createAndUpdateTopicConfig(addr, topicConfig);
                    System.out.printf("update topic perm from %s to %s in %s success.%n", queueData.getPerm(), perm, addr);
                }
                return;
            }
            ServerUtil.printCommandLineHelp("mqadmin " + this.commandName(), options);
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}
