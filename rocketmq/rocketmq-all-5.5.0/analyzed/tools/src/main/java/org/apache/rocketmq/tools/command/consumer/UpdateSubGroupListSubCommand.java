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

import com.alibaba.fastjson2.JSON;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;
import org.apache.rocketmq.srvutil.ServerUtil;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * updateSubGroupList 子命令：从 JSON 文件批量创建或更新订阅组配置。
 * <p>文件内容为 {@link SubscriptionGroupConfig} 数组。
 */
public class UpdateSubGroupListSubCommand implements SubCommand {
    @Override
    public String commandName() {
        return "updateSubGroupList";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Update or create subscription group in batch";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        final OptionGroup optionGroup = new OptionGroup();
        Option opt = new Option("b", "brokerAddr", true, "目标 Broker 地址（与 -c 二选一）");
        optionGroup.addOption(opt);

        opt = new Option("c", "clusterName", true, "目标集群名（与 -b 二选一）");
        optionGroup.addOption(opt);
        optionGroup.setRequired(true);
        options.addOptionGroup(optionGroup);

        opt = new Option("f", "filename", true,
            "订阅组配置 JSON 文件路径（SubscriptionGroupConfig 数组）");
        opt.setRequired(true);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 读取 JSON 文件并批量提交订阅组配置到 Broker 或集群。 */
    public void execute(CommandLine commandLine, Options options,
        RPCHook rpcHook) throws SubCommandException {
        final DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        final String fileName = commandLine.getOptionValue('f').trim();

        try {
            final Path filePath = Paths.get(fileName);
            if (!Files.exists(filePath)) {
                System.out.printf("the file path %s does not exists%n", fileName);
                return;
            }
            final byte[] groupConfigListBytes = Files.readAllBytes(filePath);
            // 解析 JSON 为订阅组配置列表
            final List<SubscriptionGroupConfig> groupConfigs = JSON.parseArray(groupConfigListBytes, SubscriptionGroupConfig.class);
            if (null == groupConfigs || groupConfigs.isEmpty()) {
                return;
            }

            if (commandLine.hasOption('b')) {
                String brokerAddress = commandLine.getOptionValue('b').trim();
                defaultMQAdminExt.start();
                defaultMQAdminExt.createAndUpdateSubscriptionGroupConfigList(brokerAddress, groupConfigs);

                System.out.printf("submit batch of group config to %s success, please check the result later.%n",
                    brokerAddress);
                return;

            } else if (commandLine.hasOption('c')) {
                final String clusterName = commandLine.getOptionValue('c').trim();

                defaultMQAdminExt.start();

                // 集群模式：向各 Master Broker 批量提交配置
                Set<String> masterSet =
                    CommandUtil.fetchMasterAddrByClusterName(defaultMQAdminExt, clusterName);
                for (String brokerAddress : masterSet) {
                    defaultMQAdminExt.createAndUpdateSubscriptionGroupConfigList(brokerAddress, groupConfigs);

                    System.out.printf("submit batch of subscription group config to %s success, please check the result later.%n",
                        brokerAddress);
                }
            }

            ServerUtil.printCommandLineHelp("mqadmin " + this.commandName(), options);
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}
