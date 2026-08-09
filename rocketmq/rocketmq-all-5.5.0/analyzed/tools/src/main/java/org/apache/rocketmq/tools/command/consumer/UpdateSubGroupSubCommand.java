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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.common.attribute.AttributeParser;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.subscription.GroupRetryPolicy;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;
import org.apache.rocketmq.srvutil.ServerUtil;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

import java.util.Map;
import java.util.Set;

/**
 * updateSubGroup 子命令：创建或更新单个订阅组（消费组）配置。
 * <p>支持消费开关、广播/顺序消费、重试策略及自定义属性等参数。
 */
public class UpdateSubGroupSubCommand implements SubCommand {

    @Override
    public String commandName() {
        return "updateSubGroup";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Update or create subscription group.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("b", "brokerAddr", true, "目标 Broker 地址（与 -c 二选一）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("c", "clusterName", true, "目标集群名（与 -b 二选一）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("g", "groupName", true, "消费组名称");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("s", "consumeEnable", true, "是否允许消费（true/false）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("m", "consumeFromMinEnable", true, "是否从最小位点开始消费");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("d", "consumeBroadcastEnable", true, "是否启用广播消费");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("o", "consumeMessageOrderly", true, "是否顺序消费消息");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("q", "retryQueueNums", true, "重试队列数量");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("r", "retryMaxTimes", true, "最大重试次数");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("p", "groupRetryPolicy", true,
            "the json string of retry policy ( exp: " +
                "{\"type\":\"EXPONENTIAL\",\"exponentialRetryPolicy\":{\"initial\":5000,\"max\":7200000,\"multiplier\":2}} " +
                "{\"type\":\"CUSTOMIZED\",\"customizedRetryPolicy\":{\"next\":[1000,5000,10000]}} )");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("i", "brokerId", true, "consumer from which broker id");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("w", "whichBrokerWhenConsumeSlowly", true, "which broker id when consume slowly");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("a", "notifyConsumerIdsChanged", true, "notify consumerId changed");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(null, "attributes", true, "自定义属性（+a=b,+c=d,-e 格式）");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 解析 CLI 参数构建 SubscriptionGroupConfig 并提交到 Broker 或集群。 */
    public void execute(final CommandLine commandLine, final Options options,
        RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);

        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            SubscriptionGroupConfig subscriptionGroupConfig = new SubscriptionGroupConfig();
            subscriptionGroupConfig.setConsumeBroadcastEnable(false);
            subscriptionGroupConfig.setConsumeFromMinEnable(false);

            // 设置消费组名称
            subscriptionGroupConfig.setGroupName(commandLine.getOptionValue('g').trim());

            // consumeEnable
            if (commandLine.hasOption('s')) {
                subscriptionGroupConfig.setConsumeEnable(Boolean.parseBoolean(commandLine.getOptionValue('s')
                    .trim()));
            }

            // consumeFromMinEnable
            if (commandLine.hasOption('m')) {
                subscriptionGroupConfig.setConsumeFromMinEnable(Boolean.parseBoolean(commandLine
                    .getOptionValue('m').trim()));
            }

            // consumeBroadcastEnable
            if (commandLine.hasOption('d')) {
                subscriptionGroupConfig.setConsumeBroadcastEnable(Boolean.parseBoolean(commandLine
                    .getOptionValue('d').trim()));
            }

            // consumeMessageOrderly
            if (commandLine.hasOption('o')) {
                subscriptionGroupConfig.setConsumeMessageOrderly(Boolean.parseBoolean(commandLine
                    .getOptionValue('o').trim()));
            }

            // retryQueueNums
            if (commandLine.hasOption('q')) {
                subscriptionGroupConfig.setRetryQueueNums(Integer.parseInt(commandLine.getOptionValue('q')
                    .trim()));
            }

            // retryMaxTimes
            if (commandLine.hasOption('r')) {
                subscriptionGroupConfig.setRetryMaxTimes(Integer.parseInt(commandLine.getOptionValue('r')
                    .trim()));
            }

            // groupRetryPolicy
            if (commandLine.hasOption('p')) {
                String groupRetryPolicyJson = commandLine.getOptionValue('p').trim();
                GroupRetryPolicy groupRetryPolicy = JSON.parseObject(groupRetryPolicyJson, GroupRetryPolicy.class);
                subscriptionGroupConfig.setGroupRetryPolicy(groupRetryPolicy);
            }

            // brokerId
            if (commandLine.hasOption('i')) {
                subscriptionGroupConfig.setBrokerId(Long.parseLong(commandLine.getOptionValue('i').trim()));
            }

            // whichBrokerWhenConsumeSlowly
            if (commandLine.hasOption('w')) {
                subscriptionGroupConfig.setWhichBrokerWhenConsumeSlowly(Long.parseLong(commandLine
                    .getOptionValue('w').trim()));
            }

            // notifyConsumerIdsChanged
            if (commandLine.hasOption('a')) {
                subscriptionGroupConfig.setNotifyConsumerIdsChangedEnable(Boolean.parseBoolean(commandLine
                    .getOptionValue('a').trim()));
            }

            if (commandLine.hasOption("attributes")) {
                String attributesModification = commandLine.getOptionValue("attributes").trim();
                Map<String, String> attributes = AttributeParser.parseToMap(attributesModification);
                subscriptionGroupConfig.setAttributes(attributes);
            }

            if (commandLine.hasOption('b')) {
                String addr = commandLine.getOptionValue('b').trim();

                defaultMQAdminExt.start();

                defaultMQAdminExt.createAndUpdateSubscriptionGroupConfig(addr, subscriptionGroupConfig);
                System.out.printf("create subscription group to %s success.%n", addr);
                System.out.printf("%s", subscriptionGroupConfig);
                return;

            } else if (commandLine.hasOption('c')) {
                String clusterName = commandLine.getOptionValue('c').trim();

                defaultMQAdminExt.start();
                // 集群模式：向各 Master Broker 提交订阅组配置
                Set<String> masterSet =
                    CommandUtil.fetchMasterAddrByClusterName(defaultMQAdminExt, clusterName);
                for (String addr : masterSet) {
                    try {
                        defaultMQAdminExt.createAndUpdateSubscriptionGroupConfig(addr, subscriptionGroupConfig);
                        System.out.printf("create subscription group to %s success.%n", addr);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Thread.sleep(1000 * 1);
                    }
                }
                System.out.printf("%s", subscriptionGroupConfig);
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
