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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.admin.TopicOffset;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * topicStatus 子命令：查询 Topic 各队列位点与写入 TPS。
 * <p>支持通过集群或 LMQ 父 Topic 路由聚合多 Broker 统计。
 */
public class TopicStatusSubCommand implements SubCommand {

    @Override
    public String commandName() {
        return "topicStatus";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Examine topic Status info.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("t", "topic", true, "Topic 名称");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("c", "cluster", true, "集群名或 LMQ 父 Topic（用于路由查找）");
        opt.setRequired(false);
        options.addOption(opt);
        return options;
    }

    @Override
    /** 查询 Topic 各队列 min/max 位点及最后更新时间。 */
    public void execute(final CommandLine commandLine, final Options options,
        RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);

        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));


        try {
            TopicStatsTable topicStatsTable = new TopicStatsTable();
            defaultMQAdminExt.start();
            String topic = commandLine.getOptionValue('t').trim();

            // 指定集群时遍历 Broker 聚合位点表
            if (commandLine.hasOption('c')) {
                String cluster = commandLine.getOptionValue('c').trim();
                TopicRouteData topicRouteData = defaultMQAdminExt.examineTopicRouteInfo(cluster);

                for (BrokerData bd : topicRouteData.getBrokerDatas()) {
                    String addr = bd.selectBrokerAddr();
                    if (addr != null) {
                        TopicStatsTable tst = defaultMQAdminExt.examineTopicStats(addr, topic);
                        topicStatsTable.getOffsetTable().putAll(tst.getOffsetTable());
                    }
                }
            } else {
                topicStatsTable = defaultMQAdminExt.examineTopicStats(topic);
            }

            List<MessageQueue> mqList = new LinkedList<>();
            mqList.addAll(topicStatsTable.getOffsetTable().keySet());
            Collections.sort(mqList);

            System.out.printf("%-32s  %-4s  %-20s  %-20s    %s%n",
                "#Broker Name",
                "#QID",
                "#Min Offset",
                "#Max Offset",
                "#Last Updated"
            );

            for (MessageQueue mq : mqList) {
                TopicOffset topicOffset = topicStatsTable.getOffsetTable().get(mq);

                String humanTimestamp = "";
                if (topicOffset.getLastUpdateTimestamp() > 0) {
                    humanTimestamp = UtilAll.timeMillisToHumanString2(topicOffset.getLastUpdateTimestamp());
                }

                System.out.printf("%-32s  %-4d  %-20d  %-20d    %s%n",
                    UtilAll.frontStringAtLeast(mq.getBrokerName(), 32),
                    mq.getQueueId(),
                    topicOffset.getMinOffset(),
                    topicOffset.getMaxOffset(),
                    humanTimestamp
                );
            }
            System.out.printf("%n");
            // 打印 Topic 写入 TPS
            System.out.printf("Topic Put TPS: %s%n", topicStatsTable.getTopicPutTps());
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}
