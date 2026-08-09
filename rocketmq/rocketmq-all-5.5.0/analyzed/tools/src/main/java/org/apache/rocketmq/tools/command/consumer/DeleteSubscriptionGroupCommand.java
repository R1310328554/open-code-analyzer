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

import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.srvutil.ServerUtil;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * deleteSubGroup 子命令：从 Broker 或集群删除订阅组（消费组）。
 * <p>集群模式下可同时清理重试 Topic 与死信 Topic。
 */
public class DeleteSubscriptionGroupCommand implements SubCommand {
    @Override
    public String commandName() {
        return "deleteSubGroup";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Delete subscription group from broker.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("b", "brokerAddr", true, "目标 Broker 地址（与 -c 二选一）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("c", "clusterName", true, "目标集群名（与 -b 二选一）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("g", "groupName", true, "待删除的订阅组（消费组）名称");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("r", "removeOffset", true, "是否同时删除消费位点（true/false）");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 解析 CLI 参数并在目标 Broker 或集群上删除订阅组。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt adminExt = new DefaultMQAdminExt(rpcHook);
        adminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        try {
            // 解析订阅组名与是否清理位点
            String groupName = commandLine.getOptionValue('g').trim();
            boolean cleanOffset = false;
            if (commandLine.hasOption('r')) {
                try {
                    cleanOffset = Boolean.valueOf(commandLine.getOptionValue('r').trim());
                } catch (Exception e) {
                }
            }

            if (commandLine.hasOption('b')) {
                String addr = commandLine.getOptionValue('b').trim();
                adminExt.start();

                adminExt.deleteSubscriptionGroup(addr, groupName, cleanOffset);
                System.out.printf("delete subscription group [%s] from broker [%s] success.%n", groupName,
                    addr);

                return;
            } else if (commandLine.hasOption('c')) {
                String clusterName = commandLine.getOptionValue('c').trim();
                adminExt.start();

                // 集群模式：遍历各 Master Broker 删除订阅组
                Set<String> masterSet = CommandUtil.fetchMasterAddrByClusterName(adminExt, clusterName);
                for (String master : masterSet) {
                    adminExt.deleteSubscriptionGroup(master, groupName, cleanOffset);
                    System.out.printf(
                        "delete subscription group [%s] from broker [%s] in cluster [%s] success.%n",
                        groupName, master, clusterName);
                }

                // 清理该消费组对应的重试 Topic 与死信 Topic
                try {
                    adminExt.deleteTopic(MixAll.RETRY_GROUP_TOPIC_PREFIX + groupName, clusterName);
                    adminExt.deleteTopic(MixAll.DLQ_GROUP_TOPIC_PREFIX + groupName, clusterName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            ServerUtil.printCommandLineHelp("mqadmin " + this.commandName(), options);
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            adminExt.shutdown();
        }
    }
}
