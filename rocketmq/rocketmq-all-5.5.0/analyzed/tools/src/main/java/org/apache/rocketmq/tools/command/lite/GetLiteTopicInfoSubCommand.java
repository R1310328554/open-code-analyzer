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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.lite.LiteUtil;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.body.GetLiteTopicInfoResponseBody;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * getLiteTopicInfo 子命令：查询 LMQ 父 Topic 下单个 Lite Topic 的偏移与订阅者信息。
 */
public class GetLiteTopicInfoSubCommand implements SubCommand {

    @Override
    /** 返回子命令名 getLiteTopicInfo。 */
    public String commandName() {
        return "getLiteTopicInfo";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Get lite topic info.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("p", "parentTopic", true, "LMQ 父 Topic 名");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("l", "liteTopic", true, "Lite Topic 名");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("s", "showClientId", false, "是否列出全部订阅 clientId@group");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 遍历路由上各 Broker，输出 Lite Topic 偏移区间与分片/订阅数。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            defaultMQAdminExt.start();
            String parentTopic = commandLine.getOptionValue('p').trim();
            String liteTopic = commandLine.getOptionValue('l').trim();
            boolean showClientId = commandLine.hasOption('s');

            TopicRouteData topicRouteData = defaultMQAdminExt.examineTopicRouteInfo(parentTopic);
            System.out.printf("Lite Topic Info: [%s] [%s] [%s]%n",
                parentTopic, liteTopic, LiteUtil.toLmqName(parentTopic, liteTopic));
            System.out.printf("%-50s %-14s %-14s %-30s %-12s %-18s %n",
                "#Broker Name",
                "#MinOffset",
                "#MaxOffset",
                "#LastUpdate",
                "#Sharding",
                "#SubClientCount"
            );
            for (BrokerData brokerData : topicRouteData.getBrokerDatas()) {
                String brokerAddr = brokerData.selectBrokerAddr();
                if (null == brokerAddr) {
                    continue;
                }
                GetLiteTopicInfoResponseBody body;
                try {
                    body = defaultMQAdminExt.getLiteTopicInfo(brokerAddr, parentTopic, liteTopic);
                    if (null == body.getSubscriber()) {
                        body.setSubscriber(Collections.emptySet());
                    }
                } catch (Exception e) {
                    System.out.printf("[%s] error.%n", brokerData.getBrokerName());
                    continue;
                }
                System.out.printf("%-50s %-14s %-14s %-30s %-12s %-18s %n",
                    UtilAll.frontStringAtLeast(brokerData.getBrokerName(), 40),
                    body.getTopicOffset().getMinOffset(),
                    body.getTopicOffset().getMaxOffset(),
                    body.getTopicOffset().getLastUpdateTimestamp() > 0
                        ? UtilAll.timeMillisToHumanString2(body.getTopicOffset().getLastUpdateTimestamp()) : "-",
                    body.isShardingToBroker(),
                    body.getSubscriber().size()
                );
                if (showClientId) {
                    List<String> displayList = body.getSubscriber().stream()
                        .map(clientGroup -> clientGroup.clientId + "@" + clientGroup.group)
                        .collect(Collectors.toList());
                    System.out.printf("%s%n", displayList);
                }
            }
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}
