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
import org.apache.rocketmq.common.message.MessageRequestMode;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.srvutil.ServerUtil;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;


/**
 * setConsumeMode 子命令：为指定 Topic 与消费组设置消息拉取模式（PULL/POP）。
 * <p>POP 模式可配置共享队列数量 popShareQueueNum。
 */
public class SetConsumeModeSubCommand implements SubCommand {
    @Override
    public String commandName() {
        return "setConsumeMode";
    }


    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Set consume message mode. pull/pop etc.";
    }


    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("b", "brokerAddr", true, "目标 Broker 地址（与 -c 二选一）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("c", "clusterName", true, "目标集群名（与 -b 二选一）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("t", "topicName", true, "Topic 名称");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("g", "groupName", true, "消费组名称");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("m", "mode", true, "消费模式：PULL 或 POP");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("q", "popShareQueueNum", true, "POP 模式下共享队列数量（可选）");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }


    @Override
    /** 解析 CLI 参数并调用 setMessageRequestMode 设置消费模式。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook)
            throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);

        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        defaultMQAdminExt.setVipChannelEnabled(false);

        try {

            String topicName = commandLine.getOptionValue('t').trim();
            String groupName = commandLine.getOptionValue('g').trim();

            MessageRequestMode mode = MessageRequestMode.valueOf(commandLine.getOptionValue('m').trim());


            int popShareQueueNum = 0;
            if (commandLine.hasOption('q')) {
                popShareQueueNum = Integer.parseInt(commandLine.getOptionValue('q')
                        .trim());
            }


            if (commandLine.hasOption('b')) {
                String addr = commandLine.getOptionValue('b').trim();
                defaultMQAdminExt.start();

                defaultMQAdminExt.setMessageRequestMode(addr, topicName, groupName, mode, popShareQueueNum, 5000);
                System.out.printf("set consume mode to %s success.%n", addr);
                System.out.printf("topic[%s] group[%s] consume mode[%s] popShareQueueNum[%d]",
                        topicName, groupName, mode.toString(), popShareQueueNum);
                return;

            } else if (commandLine.hasOption('c')) {
                String clusterName = commandLine.getOptionValue('c').trim();

                defaultMQAdminExt.start();
                // 集群模式：遍历各 Master Broker 设置消费模式
                Set<String> masterSet =
                        CommandUtil.fetchMasterAddrByClusterName(defaultMQAdminExt, clusterName);
                for (String addr : masterSet) {
                    try {
                        defaultMQAdminExt.setMessageRequestMode(addr, topicName, groupName, mode, popShareQueueNum, 5000);
                        System.out.printf("set consume mode to %s success.%n", addr);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Thread.sleep(1000 * 1);
                    }
                }
                System.out.printf("topic[%s] group[%s] consume mode[%s] popShareQueueNum[%d]",
                        topicName, groupName, mode.toString(), popShareQueueNum);
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
