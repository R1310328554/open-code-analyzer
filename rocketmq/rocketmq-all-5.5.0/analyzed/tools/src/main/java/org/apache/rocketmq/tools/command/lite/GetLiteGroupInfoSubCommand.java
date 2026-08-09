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
package org.apache.rocketmq.tools.command.lite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.lite.LiteLagInfo;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.admin.OffsetWrapper;
import org.apache.rocketmq.remoting.protocol.body.GetLiteGroupInfoResponseBody;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * getLiteGroupInfo 子命令：查询 LMQ 消费组在各 Broker 上的堆积与 TopK 统计。
 */
public class GetLiteGroupInfoSubCommand implements SubCommand {

    @Override
    /** 返回子命令名 getLiteGroupInfo。 */
    public String commandName() {
        return "getLiteGroupInfo";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Get lite group info.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("p", "parentTopic", true, "LMQ 父 Topic 名");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("g", "group", true, "消费组名");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("l", "liteTopic", true, "指定 Lite Topic 查询单条偏移详情");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("k", "topK", true, "各 Broker 返回的 TopK 条数，默认 20");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 汇总各 Broker 堆积量，按 lag 数量或时间输出 TopK Lite Topic。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            defaultMQAdminExt.start();

            String parentTopic = commandLine.getOptionValue('p').trim();
            String group = commandLine.getOptionValue('g').trim();
            int topK = 20;
            if (commandLine.hasOption('k')) {
                topK = Integer.parseInt(commandLine.getOptionValue('k').trim());
            }
            String liteTopic = commandLine.hasOption('l') ? commandLine.getOptionValue('l').trim() : null;
            boolean queryByLiteTopic = StringUtils.isNotEmpty(liteTopic);

            TopicRouteData topicRouteData = defaultMQAdminExt.examineTopicRouteInfo(parentTopic);
            System.out.printf("Lite Group Info: [%s] [%s]%n", group, parentTopic);

            long totalLagCount = 0;
            long earliestUnconsumedTimestamp = System.currentTimeMillis();
            List<LiteLagInfo> lagCountTopK = new ArrayList<>();
            List<LiteLagInfo> lagTimestampTopK = new ArrayList<>();

            if (queryByLiteTopic) {
                System.out.printf("%-50s %-16s %-16s %-16s %-30s%n",
                    "#Broker Name",
                    "#BrokerOffset",
                    "#ConsumeOffset",
                    "#LagCount",
                    "#LastUpdate"
                );
            }

            for (BrokerData brokerData : topicRouteData.getBrokerDatas()) {
                String brokerAddr = brokerData.selectBrokerAddr();
                if (null == brokerAddr) {
                    continue;
                }
                try {
                    GetLiteGroupInfoResponseBody body = defaultMQAdminExt.getLiteGroupInfo(brokerAddr, group, liteTopic, topK);
                    totalLagCount += body.getTotalLagCount() > 0 ? body.getTotalLagCount() : 0;
                    if (body.getEarliestUnconsumedTimestamp() > 0) {
                        earliestUnconsumedTimestamp = Math.min(earliestUnconsumedTimestamp, body.getEarliestUnconsumedTimestamp());
                    }
                    printOffsetWrapper(queryByLiteTopic, brokerData.getBrokerName(), body.getLiteTopicOffsetWrapper());
                    lagCountTopK.addAll(body.getLagCountTopK() != null ? body.getLagCountTopK() : Collections.emptyList());
                    lagTimestampTopK.addAll(body.getLagTimestampTopK() != null ? body.getLagTimestampTopK() : Collections.emptyList());
                } catch (Exception e) {
                    System.out.printf("[%s] error.%n", brokerData.getBrokerName());
                }
            }

            System.out.printf("Total Lag Count: %d%n", totalLagCount);
            long lagTime = System.currentTimeMillis() - earliestUnconsumedTimestamp;
            System.out.printf("Min Unconsumed Timestamp: %d (%d s ago)%n%n", earliestUnconsumedTimestamp, lagTime / 1000);

            if (queryByLiteTopic) {
                return;
            }

            // Sort and print topK lagCountTopK
            lagCountTopK.sort((o1, o2) -> Long.compare(o2.getLagCount(), o1.getLagCount()));
            System.out.printf("------TopK by lag count-----%n");
            System.out.printf("%-6s %-40s %-12s %-30s%n", "NO", "Lite Topic", "Lag Count", "UnconsumedTimestamp");
            for (int i = 0; i < lagCountTopK.size(); i++) {
                LiteLagInfo info = lagCountTopK.get(i);
                System.out.printf("%-6s %-40s %-12s %-30s%n",
                    i + 1, info.getLiteTopic(), info.getLagCount(), info.getEarliestUnconsumedTimestamp() > 0 ?
                        UtilAll.timeMillisToHumanString2(info.getEarliestUnconsumedTimestamp()) : "-");
            }

            // Sort and print topK lagTimestampTopK
            lagTimestampTopK.sort(Comparator.comparingLong(LiteLagInfo::getEarliestUnconsumedTimestamp));
            System.out.printf("%n------TopK by lag time------%n");
            System.out.printf("%-6s %-40s %-12s %-30s%n", "NO", "Lite Topic", "Lag Count", "UnconsumedTimestamp");
            for (int i = 0; i < lagTimestampTopK.size(); i++) {
                LiteLagInfo info = lagTimestampTopK.get(i);
                System.out.printf("%-6s %-40s %-12s %-30s%n",
                    i + 1, info.getLiteTopic(), info.getLagCount(), info.getEarliestUnconsumedTimestamp() > 0 ?
                        UtilAll.timeMillisToHumanString2(info.getEarliestUnconsumedTimestamp()) : "-");
            }

        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }

    /** 指定 -l 时输出单 Lite Topic 在各 Broker 上的偏移与堆积。 */
    private static void printOffsetWrapper(boolean queryByLiteTopic, String brokerName, OffsetWrapper offsetWrapper) {
        if (!queryByLiteTopic) {
            return;
        }
        if (null == offsetWrapper) {
            System.out.printf("%-50s %-16s %-16s %-16s %-30s%n",
                brokerName,
                "-",
                "-",
                "-",
                "-");
            return;
        }
        System.out.printf("%-50s %-16s %-16s %-16s %-30s%n",
            brokerName,
            offsetWrapper.getBrokerOffset(),
            offsetWrapper.getConsumerOffset(),
            offsetWrapper.getBrokerOffset() - offsetWrapper.getConsumerOffset(),
            offsetWrapper.getLastTimestamp() > 0
                ? UtilAll.timeMillisToHumanString2(offsetWrapper.getLastTimestamp()) : "-");
    }
}
