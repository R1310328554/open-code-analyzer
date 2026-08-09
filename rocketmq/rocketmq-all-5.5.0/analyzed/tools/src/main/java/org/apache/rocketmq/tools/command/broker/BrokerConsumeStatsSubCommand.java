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
package org.apache.rocketmq.tools.command.broker;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.admin.OffsetWrapper;
import org.apache.rocketmq.remoting.protocol.body.ConsumeStatsList;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * brokerConsumeStats 子命令：拉取 Broker 上各消费组的消费进度与堆积差值。
 */
public class BrokerConsumeStatsSubCommand implements SubCommand {

    /** 复用的 MQAdmin 客户端实例。 */
    private DefaultMQAdminExt defaultMQAdminExt;

    /** 懒创建并启动 {@link DefaultMQAdminExt}，避免重复初始化。 */
    private DefaultMQAdminExt createMQAdminExt(RPCHook rpcHook) throws SubCommandException {
        if (this.defaultMQAdminExt != null) {
            return defaultMQAdminExt;
        } else {
            defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
            defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
            try {
                defaultMQAdminExt.start();
            }
            catch (Exception e) {
                throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
            }
            return defaultMQAdminExt;
        }
    }

    @Override
    public String commandName() {
        return "brokerConsumeStats";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Fetch broker consume stats data.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("b", "brokerAddr", true, "目标 Broker 地址");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("t", "timeoutMillis", true, "请求超时时间（毫秒，默认 50000）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("l", "level", true, "仅输出堆积量不低于该阈值的队列");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("o", "order", true, "是否按顺序 Topic 统计（true/false）");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 拉取消费统计并按 Topic/Group/队列维度打印偏移与堆积差。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        try {
            defaultMQAdminExt =  createMQAdminExt(rpcHook);

            String brokerAddr = commandLine.getOptionValue('b').trim();
            boolean isOrder = false;
            long timeoutMillis = 50000;
            long diffLevel = 0;
            if (commandLine.hasOption('o')) {
                isOrder = Boolean.parseBoolean(commandLine.getOptionValue('o').trim());
            }
            if (commandLine.hasOption('t')) {
                timeoutMillis = Long.parseLong(commandLine.getOptionValue('t').trim());
            }
            if (commandLine.hasOption('l')) {
                diffLevel = Long.parseLong(commandLine.getOptionValue('l').trim());
            }

            ConsumeStatsList consumeStatsList = defaultMQAdminExt.fetchConsumeStatsInBroker(brokerAddr, isOrder, timeoutMillis);
            System.out.printf("%-64s  %-64s  %-32s  %-4s  %-20s  %-20s  %-20s  %s%n",
                "#Topic",
                "#Group",
                "#Broker Name",
                "#QID",
                "#Broker Offset",
                "#Consumer Offset",
                "#Diff",
                "#LastTime");
            for (Map<String, List<ConsumeStats>> map : consumeStatsList.getConsumeStatsList()) {
                for (Map.Entry<String, List<ConsumeStats>> entry : map.entrySet()) {
                    String group = entry.getKey();
                    List<ConsumeStats> consumeStatsArray = entry.getValue();
                    for (ConsumeStats consumeStats : consumeStatsArray) {
                        List<MessageQueue> mqList = new LinkedList<>();
                        mqList.addAll(consumeStats.getOffsetTable().keySet());
                        Collections.sort(mqList);
                        for (MessageQueue mq : mqList) {
                            OffsetWrapper offsetWrapper = consumeStats.getOffsetTable().get(mq);
                            long diff = offsetWrapper.getBrokerOffset() - offsetWrapper.getConsumerOffset();

                            // 过滤低于阈值的堆积差
                            if (diff < diffLevel) {
                                continue;
                            }
                            String lastTime = "-";
                            try {
                                lastTime = UtilAll.formatDate(new Date(offsetWrapper.getLastTimestamp()), UtilAll.YYYY_MM_DD_HH_MM_SS);
                            } catch (Exception ignored) {

                            }
                            if (offsetWrapper.getLastTimestamp() > 0)
                                System.out.printf("%-64s  %-64s  %-32s  %-4d  %-20d  %-20d  %-20d  %s%n",
                                    UtilAll.frontStringAtLeast(mq.getTopic(), 64),
                                    group,
                                    UtilAll.frontStringAtLeast(mq.getBrokerName(), 32),
                                    mq.getQueueId(),
                                    offsetWrapper.getBrokerOffset(),
                                    offsetWrapper.getConsumerOffset(),
                                    diff,
                                    lastTime
                                );
                        }
                    }
                }
            }
            System.out.printf("%nDiff Total: %d%n", consumeStatsList.getTotalDiff());
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}
