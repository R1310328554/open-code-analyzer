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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
 * createAcl 子命令：在指定集群或 Broker 上创建 ACL 访问控制规则。
 */
public class CreateAclSubCommand implements SubCommand {

    @Override
    public String commandName() {
        return "createAcl";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Create acl to cluster.";
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

        opt = new Option("s", "subject", true, "ACL 主体（用户名或角色）");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("r", "resources", true, "ACL 资源列表（逗号分隔）");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("a", "actions", true, "ACL 操作权限列表（逗号分隔）");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("i", "sourceIp", true, "来源 IP 白名单（逗号分隔，可选）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("d", "decision", true, "ACL 决策（Allow/Deny）");
        opt.setRequired(true);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 解析 CLI 参数并在目标 Broker 上调用 createAcl。 */
    public void execute(CommandLine commandLine, Options options,
        RPCHook rpcHook) throws SubCommandException {

        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            String subject = null;
            if (commandLine.hasOption('s')) {
                subject = StringUtils.trim(commandLine.getOptionValue('s'));
            }
            List<String> resources = null;
            if (commandLine.hasOption('r')) {
                resources = Arrays.stream(StringUtils.split(commandLine.getOptionValue('r'), ','))
                    .map(StringUtils::trim).collect(Collectors.toList());
            }
            List<String> actions = null;
            if (commandLine.hasOption('a')) {
                actions = Arrays.stream(StringUtils.split(commandLine.getOptionValue('a'), ','))
                    .map(StringUtils::trim).collect(Collectors.toList());
            }

            List<String> sourceIps = null;
            if (commandLine.hasOption('i')) {
                sourceIps = Arrays.stream(StringUtils.split(commandLine.getOptionValue('i'), ','))
                    .map(StringUtils::trim).collect(Collectors.toList());
            }

            String decision = null;
            if (commandLine.hasOption('d')) {
                decision = StringUtils.trim(commandLine.getOptionValue('d'));
            }

            if (commandLine.hasOption('b')) {
                String addr = StringUtils.trim(commandLine.getOptionValue('b'));

                defaultMQAdminExt.start();
                defaultMQAdminExt.createAcl(addr, subject, resources, actions, sourceIps, decision);

                System.out.printf("create acl to %s success.%n", addr);
                return;
            } else if (commandLine.hasOption('c')) {
                String clusterName = StringUtils.trim(commandLine.getOptionValue('c'));

                defaultMQAdminExt.start();
                Set<String> brokerAddrSet =
                    CommandUtil.fetchMasterAndSlaveAddrByClusterName(defaultMQAdminExt, clusterName);
                for (String addr : brokerAddrSet) {
                    defaultMQAdminExt.createAcl(addr, subject, resources, actions, sourceIps, decision);
                    System.out.printf("create acl to %s success.%n", addr);
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
