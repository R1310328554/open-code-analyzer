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

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * getBrokerConfig 子命令：查询 Broker 运行时配置项（Properties）。
 */
public class GetBrokerConfigCommand implements SubCommand {
    @Override
    public String commandName() {
        return "getBrokerConfig";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Get broker config by cluster or special broker.";
    }

    @Override
    public Options buildCommandlineOptions(final Options options) {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("b", "brokerAddr", true, "目标 Broker 地址（与 -c 二选一）"));
        group.addOption(new Option("c", "clusterName", true, "目标集群名（与 -b 二选一）"));
        group.setRequired(true);
        options.addOptionGroup(group);

        return options;
    }

    @Override
    /** 拉取 Broker 配置并按 key=value 格式打印。 */
    public void execute(final CommandLine commandLine, final Options options,
        final RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);

        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {

            if (commandLine.hasOption('b')) {
                String brokerAddr = commandLine.getOptionValue('b').trim();
                defaultMQAdminExt.start();

                getAndPrint(defaultMQAdminExt,
                    String.format("============%s============\n", brokerAddr),
                    brokerAddr);

            } else if (commandLine.hasOption('c')) {
                String clusterName = commandLine.getOptionValue('c').trim();
                defaultMQAdminExt.start();

                // 集群模式：分别拉取 Master 及其 Slave 的配置
                Map<String, List<String>> masterAndSlaveMap
                    = CommandUtil.fetchMasterAndSlaveDistinguish(defaultMQAdminExt, clusterName);

                for (String masterAddr : masterAndSlaveMap.keySet()) {

                    if (masterAddr == null) {
                        continue;
                    }

                    getAndPrint(
                        defaultMQAdminExt,
                        String.format("============Master: %s============\n", masterAddr),
                        masterAddr
                    );

                    for (String slaveAddr : masterAndSlaveMap.get(masterAddr)) {

                        if (slaveAddr == null) {
                            continue;
                        }

                        getAndPrint(
                            defaultMQAdminExt,
                            String.format("============My Master: %s=====Slave: %s============\n", masterAddr, slaveAddr),
                            slaveAddr
                        );
                    }
                }
            }

        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }

    /** 拉取指定 Broker 配置并以 key=value 格式输出。 */
    protected void getAndPrint(final MQAdminExt defaultMQAdminExt, final String printPrefix, final String addr)
        throws InterruptedException, RemotingConnectException,
        UnsupportedEncodingException, RemotingTimeoutException,
        MQBrokerException, RemotingSendRequestException {

        System.out.print(printPrefix);

        // 占位符表示该 Broker 无 Master，跳过
        if (addr.equals(CommandUtil.NO_MASTER_PLACEHOLDER)) {
            return;
        }

        Properties properties = defaultMQAdminExt.getBrokerConfig(addr);
        if (properties == null) {
            System.out.printf("Broker[%s] has no config property!\n", addr);
            return;
        }

        for (Entry<Object, Object> entry : properties.entrySet()) {
            System.out.printf("%-50s=  %s\n", entry.getKey(), entry.getValue());
        }

        System.out.printf("%n");
    }
}
