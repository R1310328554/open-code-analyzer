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
 * addBroker 子命令：向 Broker 容器（Container）动态添加 Broker 实例。
 * <p>通过指定配置文件路径启动新的 Broker 进程。
 */
public class AddBrokerSubCommand implements SubCommand {
    @Override
    public String commandName() {
        return "addBroker";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Add a broker to specified container.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("c", "brokerContainerAddr", true, "Broker 容器地址");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("b", "brokerConfigPath", true, "新 Broker 配置文件路径");
        opt.setRequired(true);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 解析 CLI 参数并调用 addBrokerToContainer 动态添加 Broker。 */
    public void execute(CommandLine commandLine, Options options,
        RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            defaultMQAdminExt.start();
            String brokerContainerAddr = commandLine.getOptionValue('c').trim();
            String brokerConfigPath = commandLine.getOptionValue('b').trim();
            defaultMQAdminExt.addBrokerToContainer(brokerContainerAddr, brokerConfigPath);
            System.out.printf("add broker to %s success%n", brokerContainerAddr);
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}
