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

package org.apache.rocketmq.tools.command.controller;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.body.BrokerMemberGroup;
import org.apache.rocketmq.remoting.protocol.header.controller.ElectMasterResponseHeader;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * electMaster 子命令：通过 Controller 触发指定 Broker 副本 Master 选举。
 * <p>输出新 Master 地址、Epoch 及同步状态集信息。
 */
public class ReElectMasterSubCommand implements SubCommand {

    @Override
    public String commandName() {
        return "electMaster";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Re-elect the specified broker as master.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("a", "controllerAddress", true, "Controller 节点地址");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("b", "brokerId", true, "期望成为 Master 的 Broker ID");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("bn", "brokerName", true, "待操作的 Broker 副本组名称");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("c", "clusterName", true, "Broker 所属集群名");
        opt.setRequired(true);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 调用 electMaster 触发 Master 选举并打印结果。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {

        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        String controllerAddress = commandLine.getOptionValue("a").trim();
        String clusterName = commandLine.getOptionValue('c').trim();
        String brokerName = commandLine.getOptionValue("bn").trim();
        Long brokerId = Long.valueOf(commandLine.getOptionValue("b").trim());

        try {
            defaultMQAdminExt.start();
            // 向 Controller 发起 Master 选举请求
            final Pair<ElectMasterResponseHeader, BrokerMemberGroup> pair = defaultMQAdminExt.electMaster(controllerAddress, clusterName, brokerName, brokerId);
            final ElectMasterResponseHeader metaData = pair.getObject1();
            final BrokerMemberGroup brokerMemberGroup = pair.getObject2();
            System.out.printf("\n#ClusterName\t%s", clusterName);
            System.out.printf("\n#BrokerName\t%s", brokerName);
            System.out.printf("\n#BrokerMasterAddr\t%s", metaData.getMasterAddress());
            System.out.printf("\n#MasterEpoch\t%s", metaData.getMasterEpoch());
            System.out.printf("\n#SyncStateSetEpoch\t%s\n", metaData.getSyncStateSetEpoch());
            if (null != brokerMemberGroup && null != brokerMemberGroup.getBrokerAddrs()) {
                brokerMemberGroup.getBrokerAddrs().forEach((key, value) -> System.out.printf("\t#Broker\t%d\t%s\n", key, value));
            }
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }

    }
}
