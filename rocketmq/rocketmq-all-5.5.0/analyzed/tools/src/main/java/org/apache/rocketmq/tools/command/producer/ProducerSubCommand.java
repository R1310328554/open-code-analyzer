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
package org.apache.rocketmq.tools.command.producer;

import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.body.ProducerInfo;
import org.apache.rocketmq.remoting.protocol.body.ProducerTableInfo;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.MQAdminStartup;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * producer 子命令：查询 Broker 上已连接的生产者实例与状态。
 */
public class ProducerSubCommand implements SubCommand {

    /** 本地调试入口，默认连接 127.0.0.1:9876 与指定 Broker。 */
    public static void main(String[] args) {
        System.setProperty(MixAll.NAMESRV_ADDR_PROPERTY, "127.0.0.1:9876");
        MQAdminStartup.main(new String[]{new ProducerSubCommand().commandName(), "-b", "127.0.0.1:10911"});
    }

    @Override
    /** 返回子命令名 producer。 */
    public String commandName() {
        return "producer";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Query producer's instances, connection, status, etc.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("b", "broker", true, "目标 Broker 地址");
        opt.setRequired(true);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 拉取 Broker 全部生产者组并逐实例打印连接信息。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            defaultMQAdminExt.start();
            String brokerAddr = commandLine.getOptionValue('b').trim();
            ProducerTableInfo cc = defaultMQAdminExt.getAllProducerInfo(brokerAddr);
            if (cc != null && cc.getData() != null && !cc.getData().isEmpty()) {
                for (String group : cc.getData().keySet()) {
                    List<ProducerInfo> list = cc.getData().get(group);
                    if (list == null || list.isEmpty()) {
                        System.out.printf("producer group (%s) instances are empty\n", group);
                        continue;
                    }
                    for (ProducerInfo producer : list) {
                        System.out.printf("producer group (%s) instance : %s\n", group, producer.toString());
                    }
                }
            }
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}
