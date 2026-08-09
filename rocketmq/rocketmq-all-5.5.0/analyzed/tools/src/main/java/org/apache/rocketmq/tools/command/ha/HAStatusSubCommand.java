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

package org.apache.rocketmq.tools.command.ha;

import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.body.HARuntimeInfo;
import org.apache.rocketmq.remoting.protocol.body.HARuntimeInfo.HAClientRuntimeInfo;
import org.apache.rocketmq.remoting.protocol.body.HARuntimeInfo.HAConnectionRuntimeInfo;
import org.apache.rocketmq.srvutil.ServerUtil;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * haStatus 子命令：查询 Broker 主从 HA 复制运行时状态与连接详情。
 */
public class HAStatusSubCommand implements SubCommand {

    @Override
    /** 返回子命令名 haStatus。 */
    public String commandName() {
        return "haStatus";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Fetch ha runtime status data.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("c", "clusterName", true, "目标集群名（与 -b 二选一）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("b", "brokerAddr", true, "目标 Broker 地址（与 -c 二选一）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("i", "interval", true, "轮询间隔（秒），指定后持续刷新输出");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 启动管理客户端，按 -i 决定是否循环拉取 HA 状态。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            if (commandLine.hasOption('i')) {
                String interval = commandLine.getOptionValue('i');
                int flushSecond = 3;
                if (interval != null && !interval.trim().equals("")) {
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

    /** 按 -b 单 Broker 或 -c 集群 Master 逐一打印 HA 状态。 */
    private void innerExec(CommandLine commandLine, Options options,
        DefaultMQAdminExt defaultMQAdminExt) throws Exception {
        if (commandLine.hasOption('b')) {
            String addr = commandLine.getOptionValue('b').trim();
            this.printStatus(addr, defaultMQAdminExt);
        } else if (commandLine.hasOption('c')) {

            String clusterName = commandLine.getOptionValue('c').trim();
            Set<String> masterSet = CommandUtil.fetchMasterAddrByClusterName(defaultMQAdminExt, clusterName);

            for (String addr : masterSet) {
                this.printStatus(addr, defaultMQAdminExt);
            }
        } else {
            ServerUtil.printCommandLineHelp("mqadmin " + this.commandName(), options);
        }

    }

    /** 区分 Master/Slave 角色，输出复制偏移、传输速率与同步状态。 */
    private void printStatus(String brokerAddr, DefaultMQAdminExt defaultMQAdminExt) throws Exception {
        HARuntimeInfo haRuntimeInfo = defaultMQAdminExt.getBrokerHAStatus(brokerAddr);

        if (haRuntimeInfo.isMaster()) {
            System.out.printf("\n#MasterAddr\t%s\n#MasterCommitLogMaxOffset\t%d\n#SlaveNum\t%d\n#InSyncSlaveNum\t%d\n", brokerAddr,
                haRuntimeInfo.getMasterCommitLogMaxOffset(), haRuntimeInfo.getHaConnectionInfo().size(), haRuntimeInfo.getInSyncSlaveNums());
            System.out.printf("%-32s  %-16s %16s %16s %16s %16s\n",
                "#SlaveAddr",
                "#SlaveAckOffset",
                "#Diff",
                "#TransferSpeed(KB/s)",
                "#Status",
                "#TransferFromWhere"
            );

            for (HAConnectionRuntimeInfo cInfo : haRuntimeInfo.getHaConnectionInfo()) {
                System.out.printf("%-32s  %-16d %16d %16.2f %16s %16d\n",
                    cInfo.getAddr(),
                    cInfo.getSlaveAckOffset(),
                    cInfo.getDiff(),
                    cInfo.getTransferredByteInSecond() / 1024.0,
                    cInfo.isInSync() ? "OK" : "Fall Behind",
                    cInfo.getTransferFromWhere());
            }
        } else {
            HAClientRuntimeInfo haClientRuntimeInfo = haRuntimeInfo.getHaClientRuntimeInfo();

            System.out.printf("\n#MasterAddr\t%s\n", haClientRuntimeInfo.getMasterAddr());
            System.out.printf("#CommitLogMaxOffset\t%d\n", haClientRuntimeInfo.getMaxOffset());
            System.out.printf("#TransferSpeed(KB/s)\t%.2f\n", haClientRuntimeInfo.getTransferredByteInSecond() / 1024.0);
            System.out.printf("#LastReadTime\t%s\n", UtilAll.timeMillisToHumanString2(haClientRuntimeInfo.getLastReadTimestamp()));
            System.out.printf("#LastWriteTime\t%s\n", UtilAll.timeMillisToHumanString2(haClientRuntimeInfo.getLastWriteTimestamp()));
            System.out.printf("#MasterFlushOffset\t%s\n", haClientRuntimeInfo.getMasterFlushOffset());
        }
    }

}
