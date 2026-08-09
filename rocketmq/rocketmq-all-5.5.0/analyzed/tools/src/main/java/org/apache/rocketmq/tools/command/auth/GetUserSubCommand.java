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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.body.UserInfo;
import org.apache.rocketmq.srvutil.ServerUtil;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * getUser 子命令：查询指定 Broker 或集群上的用户账户信息。
 */
public class GetUserSubCommand implements SubCommand {

    /** 用户信息表格输出格式。 */
    private static final String FORMAT = "%-16s  %-22s  %-22s  %-22s%n";

    @Override
    public String commandName() {
        return "getUser";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Get user from cluster.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        OptionGroup optionGroup = new OptionGroup();

        Option opt = new Option("b", "brokerAddr", true, "查询目标 Broker 地址（与 -c 二选一）");
        optionGroup.addOption(opt);

        opt = new Option("c", "clusterName", true, "查询目标集群名（与 -b 二选一）");
        optionGroup.addOption(opt);

        optionGroup.setRequired(true);
        options.addOptionGroup(optionGroup);

        opt = new Option("u", "username", true, "待查询的用户名（可选）");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 拉取用户信息并以表格形式打印。 */
    public void execute(CommandLine commandLine, Options options,
        RPCHook rpcHook) throws SubCommandException {

        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            String username = StringUtils.trim(commandLine.getOptionValue('u'));

            if (commandLine.hasOption('b')) {
                String addr = StringUtils.trim(commandLine.getOptionValue('b'));
                defaultMQAdminExt.start();

                UserInfo userInfo = defaultMQAdminExt.getUser(addr, username);
                if (userInfo != null) {
                    printUser(userInfo);
                }
                return;
            } else if (commandLine.hasOption('c')) {
                String clusterName = StringUtils.trim(commandLine.getOptionValue('c'));

                defaultMQAdminExt.start();

                Set<String> masterSet =
                    CommandUtil.fetchMasterAddrByClusterName(defaultMQAdminExt, clusterName);
                if (CollectionUtils.isEmpty(masterSet)) {
                    throw new SubCommandException(this.getClass().getSimpleName() + " command failed, there is no broker in cluster.");
                }
                // 集群模式：遍历 Master 直到找到匹配用户
                for (String masterAddr : masterSet) {
                    UserInfo userInfo = defaultMQAdminExt.getUser(masterAddr, username);
                    if (userInfo != null) {
                        printUser(userInfo);
                        break;
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

    /** 格式化输出单个用户信息。 */
    private void printUser(UserInfo user) {
        if (user == null) {
            return;
        }
        System.out.printf(FORMAT, "#UserName", "#Password", "#UserType", "#UserStatus");
        System.out.printf(FORMAT, user.getUsername(), user.getPassword(), user.getUserType(), user.getUserStatus());
    }
}
