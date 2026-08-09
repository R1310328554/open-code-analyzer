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
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.srvutil.ServerUtil;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * updateTopicList 子命令：从 JSON 文件批量创建或更新 Topic。
 * <p>支持指定单个 Broker 或整个集群的全部 Master。
 */
public class UpdateTopicListSubCommand implements SubCommand {
    @Override
    public String commandName() {
        return "updateTopicList";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "create or update topic in batch";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        final OptionGroup optionGroup = new OptionGroup();
        Option opt = new Option("b", "brokerAddr", true, "目标 Broker 地址");
        optionGroup.addOption(opt);
        opt = new Option("c", "clusterName", true, "目标集群名称");
        optionGroup.addOption(opt);
        optionGroup.setRequired(true);
        options.addOptionGroup(optionGroup);

        opt = new Option("f", "filename", true, "TopicConfig 列表 JSON 文件路径");
        opt.setRequired(true);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 读取 JSON 文件并向 Broker 或集群批量提交 Topic 配置。 */
    public void execute(CommandLine commandLine, Options options,
        RPCHook rpcHook) throws SubCommandException {
        final DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        final String fileName = commandLine.getOptionValue('f').trim();


        try {
            final Path filePath = Paths.get(fileName);
            // 文件不存在时提前退出
            if (!Files.exists(filePath)) {
                System.out.printf("the file path %s does not exists%n", fileName);
                return;
            }
            final byte[] topicConfigListBytes = Files.readAllBytes(filePath);
            final List<TopicConfig> topicConfigs = JSON.parseArray(topicConfigListBytes, TopicConfig.class);
            if (null == topicConfigs || topicConfigs.isEmpty()) {
                return;
            }

            // 向单个 Broker 提交批量 Topic 配置
            if (commandLine.hasOption('b')) {
                String brokerAddress = commandLine.getOptionValue('b').trim();
                defaultMQAdminExt.start();
                defaultMQAdminExt.createAndUpdateTopicConfigList(brokerAddress, topicConfigs);

                System.out.printf("submit batch of topic config to %s success, please check the result later.%n",
                    brokerAddress);
                return;

            // 向集群全部 Master 逐一提交配置
            } else if (commandLine.hasOption('c')) {
                final String clusterName = commandLine.getOptionValue('c').trim();

                defaultMQAdminExt.start();

                Set<String> masterSet =
                    CommandUtil.fetchMasterAddrByClusterName(defaultMQAdminExt, clusterName);
                for (String brokerAddress : masterSet) {
                    defaultMQAdminExt.createAndUpdateTopicConfigList(brokerAddress, topicConfigs);

                    System.out.printf("submit batch of topic config to %s success, please check the result later.%n",
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
