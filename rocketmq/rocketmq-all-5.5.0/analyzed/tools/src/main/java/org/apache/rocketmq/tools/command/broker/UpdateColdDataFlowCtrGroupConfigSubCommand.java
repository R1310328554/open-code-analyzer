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


import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.srvutil.ServerUtil;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * updateColdDataFlowCtrGroupConfig 子命令：新增或更新消费组冷数据读流控阈值。
 */
public class UpdateColdDataFlowCtrGroupConfigSubCommand implements SubCommand {

    @Override
    /** 返回子命令名 updateColdDataFlowCtrGroupConfig。 */
    public String commandName() {
        return "updateColdDataFlowCtrGroupConfig";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Add or update cold data flow ctr group config.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("b", "brokerAddr", true, "目标 Broker 地址（与 -c 二选一）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("c", "clusterName", true, "目标集群名，批量更新各 Master");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("g", "consumerGroup", true, "消费组名（作为配置键）");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("v", "threshold", true, "冷读流控阈值");
        opt.setRequired(true);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 将消费组与阈值写入 Properties，在 Broker 或集群上更新冷流控配置。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        try {
            String key = commandLine.getOptionValue('g').trim();
            String value = commandLine.getOptionValue('v').trim();
            Properties properties = new Properties();
            properties.put(key, value);

            if (commandLine.hasOption('b')) {
                String brokerAddr = commandLine.getOptionValue('b').trim();
                defaultMQAdminExt.start();
                defaultMQAdminExt.updateColdDataFlowCtrGroupConfig(brokerAddr, properties);
                System.out.printf("updateColdDataFlowCtrGroupConfig success, %s\n", brokerAddr);
                return;
            } else if (commandLine.hasOption('c')) {
                String clusterName = commandLine.getOptionValue('c').trim();
                defaultMQAdminExt.start();
                Set<String> masterSet = CommandUtil.fetchMasterAddrByClusterName(defaultMQAdminExt, clusterName);
                for (String brokerAddr : masterSet) {
                    try {
                        defaultMQAdminExt.updateColdDataFlowCtrGroupConfig(brokerAddr, properties);
                        System.out.printf("updateColdDataFlowCtrGroupConfig success, %s\n", brokerAddr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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