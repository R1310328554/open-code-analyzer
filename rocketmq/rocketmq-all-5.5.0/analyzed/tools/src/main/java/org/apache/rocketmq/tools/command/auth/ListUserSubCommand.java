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

import java.util.List;
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
 * listUser 子命令：列出指定 Broker 或集群上的用户账户。
 */
public class ListUserSubCommand implements SubCommand {

    /** 用户列表表格输出格式。 */
    private static final String FORMAT = "%-16s  %-22s  %-22s  %-22s%n";

    @Override
    public String commandName() {
        return "listUser";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "List user from cluster.";
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

        opt = new Option("f", "filter", true, "用户名过滤条件（可选）");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 拉取用户列表并以表格形式打印。 */
    public void execute(CommandLine commandLine, Options options,
        RPCHook rpcHook) throws SubCommandException {

        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            String filter = StringUtils.trim(commandLine.getOptionValue('f'));

            if (commandLine.hasOption('b')) {
                String addr = StringUtils.trim(commandLine.getOptionValue('b'));
                defaultMQAdminExt.start();

                List<UserInfo> userInfos = defaultMQAdminExt.listUser(addr, filter);
                if (CollectionUtils.isNotEmpty(userInfos)) {
                    printUsers(userInfos);
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
                for (String masterAddr : masterSet) {
                    List<UserInfo> userInfos = defaultMQAdminExt.listUser(masterAddr, filter);
                    if (CollectionUtils.isNotEmpty(userInfos)) {
                        printUsers(userInfos);
                        // 集群模式：首个有数据的 Master 即返回
                        System.out.printf("get user from %s success.%n", masterAddr);
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

    /** 格式化输出用户列表。 */
    private void printUsers(List<UserInfo> users) {
        System.out.printf(FORMAT, "#UserName", "#Password", "#UserType", "#UserStatus");
        users.forEach(user -> System.out.printf(FORMAT, user.getUsername(), user.getPassword(), user.getUserType(), user.getUserStatus()));
    }
}
