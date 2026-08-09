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

package org.apache.rocketmq.tools.command.container;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * removeBroker 子命令：从 Broker 容器中移除指定 Broker 实例。
 * <p>通过 clusterName:brokerName:brokerId 三元组标识目标 Broker。
 */
public class RemoveBrokerSubCommand implements SubCommand {
    @Override
    public String commandName() {
        return "removeBroker";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Remove a broker from specified container.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("c", "brokerContainerAddr", true, "Broker 容器地址");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("b", "brokerIdentity", true, "Broker 标识：clusterName:brokerName:brokerId（dLedger 用 dLedgerId）");
        opt.setRequired(true);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 解析 Broker 标识并调用 removeBrokerFromContainer 移除实例。 */
    public void execute(CommandLine commandLine, Options options,
        RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            defaultMQAdminExt.start();
            String brokerContainerAddr = commandLine.getOptionValue('c').trim();
            // 解析 clusterName:brokerName:brokerId 三元组
            String[] brokerIdentities = commandLine.getOptionValue('b').trim().split(":");
            String clusterName = brokerIdentities[0].trim();
            String brokerName = brokerIdentities[1].trim();
            long brokerId;
            try {
                brokerId = Long.parseLong(brokerIdentities[2].trim());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return;
            }
            if (brokerId < 0) {
                System.out.printf("brokerId can't be negative%n");
                return;
            }
            defaultMQAdminExt.removeBrokerFromContainer(brokerContainerAddr, clusterName, brokerName, brokerId);
            System.out.printf("remove broker from %s success%n", brokerContainerAddr);
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}
