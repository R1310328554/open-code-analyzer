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
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * triggerLiteDispatch 子命令：手动触发 LMQ 消费组的 Lite 消息分发。
 */
public class TriggerLiteDispatchSubCommand implements SubCommand {

    @Override
    /** 返回子命令名 triggerLiteDispatch。 */
    public String commandName() {
        return "triggerLiteDispatch";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Trigger Lite Dispatch.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("p", "parentTopic", true, "LMQ 父 Topic 名");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("g", "group", true, "消费组名");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("c", "clientId", true, "可选，限定单个客户端 clientId");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("b", "brokerName", true, "可选，限定单个 Broker 名");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 遍历路由 Broker，调用 triggerLiteDispatch 触发 Lite 分发并打印结果。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            defaultMQAdminExt.start();

            String parentTopic = commandLine.getOptionValue('p').trim();
            String group = commandLine.getOptionValue('g').trim();
            String clientId = commandLine.hasOption('c') ? commandLine.getOptionValue('c').trim() : null;
            String brokerName = commandLine.hasOption('b') ? commandLine.getOptionValue('b').trim() : null;

            TopicRouteData topicRouteData = defaultMQAdminExt.examineTopicRouteInfo(parentTopic);
            System.out.printf("Group And Topic Info: [%s] [%s]%n%n", group, parentTopic);

            for (BrokerData brokerData : topicRouteData.getBrokerDatas()) {
                String brokerAddr = brokerData.selectBrokerAddr();
                if (null == brokerAddr) {
                    continue;
                }
                if (brokerName != null && !brokerName.equals(brokerData.getBrokerName())) {
                    continue;
                }
                boolean success = true;
                try {
                    defaultMQAdminExt.triggerLiteDispatch(brokerAddr, group, clientId);
                } catch (Exception e) {
                    success = false;
                }
                System.out.printf("%-30s %-12s%n", brokerData.getBrokerName(), success ? "dispatched" : "error");
            }
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }

}
