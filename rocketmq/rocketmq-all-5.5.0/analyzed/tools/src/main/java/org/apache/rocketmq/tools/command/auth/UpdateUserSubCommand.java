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
package org.apache.rocketmq.tools.command.auth;

import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.srvutil.ServerUtil;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * updateUser 子命令：更新指定集群或 Broker 上的用户账户信息。
 */
public class UpdateUserSubCommand implements SubCommand {

    @Override
    public String commandName() {
        return "updateUser";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Update user to cluster.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        OptionGroup optionGroup = new OptionGroup();

        Option opt = new Option("c", "clusterName", true, "目标集群名（与 -b 二选一）");
        optionGroup.addOption(opt);

        opt = new Option("b", "brokerAddr", true, "目标 Broker 地址（与 -c 二选一）");
        optionGroup.addOption(opt);

        optionGroup.setRequired(true);
        options.addOptionGroup(optionGroup);

        opt = new Option("u", "username", true, "待更新的用户名");
        opt.setRequired(true);
        options.addOption(opt);

        optionGroup = new OptionGroup();
        opt = new Option("p", "password", true, "新密码（与 -t/-s 三选一）");
        optionGroup.addOption(opt);

        opt = new Option("t", "userType", true, "用户类型（与 -p/-s 三选一）");
        optionGroup.addOption(opt);

        opt = new Option("s", "userStatus", true, "用户状态（与 -p/-t 三选一）");
        optionGroup.addOption(opt);
        optionGroup.setRequired(true);

        options.addOptionGroup(optionGroup);

        return options;
    }

    @Override
    /** 解析 CLI 参数并在目标 Broker 上调用 updateUser。 */
    public void execute(CommandLine commandLine, Options options,
        RPCHook rpcHook) throws SubCommandException {

        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            String username = StringUtils.trim(commandLine.getOptionValue('u'));
            String password = StringUtils.trim(commandLine.getOptionValue('p'));
            String userType = StringUtils.trim(commandLine.getOptionValue('t'));
            String userStatus = StringUtils.trim(commandLine.getOptionValue('s'));

            if (commandLine.hasOption('b')) {
                String addr = commandLine.getOptionValue('b').trim();

                defaultMQAdminExt.start();
                defaultMQAdminExt.updateUser(addr, username, password, userType, userStatus);

                System.out.printf("update user to %s success.%n", addr);
                return;
            } else if (commandLine.hasOption('c')) {
                String clusterName = commandLine.getOptionValue('c').trim();

                defaultMQAdminExt.start();
                // 集群模式：遍历主从 Broker 逐一更新用户
                Set<String> brokerAddrSet =
                    CommandUtil.fetchMasterAndSlaveAddrByClusterName(defaultMQAdminExt, clusterName);
                for (String addr : brokerAddrSet) {
                    defaultMQAdminExt.updateUser(addr, username, password, userType, userStatus);
                    System.out.printf("update user to %s success.%n", addr);
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
