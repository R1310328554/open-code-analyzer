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

import java.util.List;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.EpochEntry;
import org.apache.rocketmq.remoting.protocol.body.EpochEntryCache;
import org.apache.rocketmq.srvutil.ServerUtil;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * getBrokerEpoch 子命令：拉取 Broker 的 Epoch 条目，用于 HA 复制与偏移区间校验。
 */
public class GetBrokerEpochSubCommand implements SubCommand {
    @Override
    /** 返回子命令名 getBrokerEpoch。 */
    public String commandName() {
        return "getBrokerEpoch";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Fetch broker epoch entries.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("c", "clusterName", true, "目标集群名（与 -b 二选一）"));
        group.addOption(new Option("b", "brokerName", true, "目标 Broker 名（与 -c 二选一）"));
        group.setRequired(true);
        options.addOptionGroup(group);

        Option opt = new Option("i", "interval", true, "轮询间隔（秒），指定后持续刷新输出");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 启动管理客户端，按 -i 决定是否循环拉取 Epoch 信息。 */
    public void execute(CommandLine commandLine, Options options,
        RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            if (commandLine.hasOption('i')) {
                String interval = commandLine.getOptionValue('i');
                int flushSecond = 3;
                if (interval != null && !interval.trim().isEmpty()) {
                    flushSecond = Integer.parseInt(interval);
                }

                defaultMQAdminExt.start();

                while (true) {
                    this.innerExec(commandLine, options, defaultMQAdminExt);
                    Thread.sleep(flushSecond * 1000);
                }
            } else {
                defaultMQAdminExt.start();

                this.innerExec(commandLine, options, defaultMQAdminExt);
            }
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }

    /** 按 -b 或 -c 解析目标 Broker 并打印 Epoch 数据。 */
    private void innerExec(CommandLine commandLine, Options options,
        DefaultMQAdminExt defaultMQAdminExt) throws Exception {
        if (commandLine.hasOption('b')) {
            String brokerName = commandLine.getOptionValue('b').trim();
            final Set<String> brokers = CommandUtil.fetchMasterAndSlaveAddrByBrokerName(defaultMQAdminExt, brokerName);
            printData(brokers, defaultMQAdminExt);
        } else if (commandLine.hasOption('c')) {
            String clusterName = commandLine.getOptionValue('c').trim();
            Set<String> brokers = CommandUtil.fetchMasterAndSlaveAddrByClusterName(defaultMQAdminExt, clusterName);
            printData(brokers, defaultMQAdminExt);
        } else {
            ServerUtil.printCommandLineHelp("mqadmin " + this.commandName(), options);
        }
    }

    /** 遍历 Broker 地址，拉取 {@link EpochEntryCache} 并格式化输出各 Epoch 区间。 */
    private void printData(Set<String> brokers, DefaultMQAdminExt defaultMQAdminExt) throws Exception {
        for (String brokerAddr : brokers) {
            final EpochEntryCache epochCache = defaultMQAdminExt.getBrokerEpochCache(brokerAddr);
            System.out.printf("\n#clusterName\t%s\n#brokerName\t%s\n#brokerAddr\t%s\n#brokerId\t%d",
                epochCache.getClusterName(), epochCache.getBrokerName(), brokerAddr, epochCache.getBrokerId());
            final List<EpochEntry> epochList = epochCache.getEpochList();
            for (int i = 0; i < epochList.size(); i++) {
                final EpochEntry epochEntry = epochList.get(i);
                if (i == epochList.size() - 1) {
                    epochEntry.setEndOffset(epochCache.getMaxOffset());
                }
                System.out.printf("\n#Epoch: %s", epochEntry.toString());
            }
            System.out.print("\n");
        }
    }
}
