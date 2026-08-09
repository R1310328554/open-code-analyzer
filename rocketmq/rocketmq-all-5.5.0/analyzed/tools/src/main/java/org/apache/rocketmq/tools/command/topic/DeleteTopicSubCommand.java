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
package org.apache.rocketmq.tools.command.topic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.srvutil.ServerUtil;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * deleteTopic 子命令：从 Broker 与 NameServer 删除 Topic。
 */
public class DeleteTopicSubCommand implements SubCommand {
    /** 从集群 Master Broker 与 NameServer 删除指定 Topic。 */
    public static void deleteTopic(final DefaultMQAdminExt adminExt,
        final String clusterName,
        final String topic
    ) throws InterruptedException, MQBrokerException, RemotingException, MQClientException {

        Set<String> masterBrokerAddressSet = CommandUtil.fetchMasterAddrByClusterName(adminExt, clusterName);
        adminExt.deleteTopicInBroker(masterBrokerAddressSet, topic);
        System.out.printf("delete topic [%s] from cluster [%s] success.%n", topic, clusterName);

        Set<String> nameServerSet = null;
        if (adminExt.getNamesrvAddr() != null) {
            String[] ns = adminExt.getNamesrvAddr().trim().split(";");
            nameServerSet = new HashSet(Arrays.asList(ns));
        }

        adminExt.deleteTopicInNameServer(nameServerSet, clusterName, topic);
        System.out.printf("delete topic [%s] from NameServer success.%n", topic);
    }

    @Override
    /** 返回子命令名 deleteTopic。 */
    public String commandName() {
        return "deleteTopic";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Delete topic from broker and NameServer.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("t", "topic", true, "待删除 Topic 名");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("c", "clusterName", true, "Topic 所在集群名");
        opt.setRequired(true);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 校验 -c 集群参数后调用 {@link #deleteTopic}。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt adminExt = new DefaultMQAdminExt(rpcHook);
        adminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        try {
            String topic = commandLine.getOptionValue('t').trim();

            if (commandLine.hasOption('c')) {
                String clusterName = commandLine.getOptionValue('c').trim();

                adminExt.start();
                deleteTopic(adminExt, clusterName, topic);
                return;
            }

            ServerUtil.printCommandLineHelp("mqadmin " + this.commandName(), options);
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            adminExt.shutdown();
        }
    }
}
