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
 * updateBrokerConfig 子命令：在线更新 Broker 运行时配置项（键值对）。
 */
public class UpdateBrokerConfigSubCommand implements SubCommand {

    @Override
    /** 返回子命令名 updateBrokerConfig。 */
    public String commandName() {
        return "updateBrokerConfig";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Update broker's config.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("b", "brokerAddr", true, "目标 Broker 地址（与 -c 二选一）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("c", "clusterName", true, "目标集群名，批量更新各 Broker");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("a", "updateAllBroker", true, "是否包含 Slave 一并更新（与 -c 配合）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("k", "key", true, "配置项键名");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("v", "value", true, "配置项新值");
        opt.setRequired(true);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 构造 Properties 并在指定 Broker 或集群上调用 updateBrokerConfig。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);

        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            String key = commandLine.getOptionValue('k').trim();
            String value = commandLine.getOptionValue('v').trim();
            Properties properties = new Properties();
            properties.put(key, value);

            if (commandLine.hasOption('b')) {
                String brokerAddr = commandLine.getOptionValue('b').trim();

                defaultMQAdminExt.start();

                defaultMQAdminExt.updateBrokerConfig(brokerAddr, properties);
                System.out.printf("update broker config success, %s\n", brokerAddr);
                return;

            } else if (commandLine.hasOption('c')) {
                String clusterName = commandLine.getOptionValue('c').trim();

                defaultMQAdminExt.start();

                Set<String> brokerAddrSet;

                if (commandLine.hasOption('a')) {
                    brokerAddrSet = CommandUtil.fetchMasterAndSlaveAddrByClusterName(defaultMQAdminExt, clusterName);
                } else {
                    brokerAddrSet = CommandUtil.fetchMasterAddrByClusterName(defaultMQAdminExt, clusterName);
                }

                for (String brokerAddr : brokerAddrSet) {
                    try {
                        defaultMQAdminExt.updateBrokerConfig(brokerAddr, properties);
                        System.out.printf("update broker config success, %s\n", brokerAddr);
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
