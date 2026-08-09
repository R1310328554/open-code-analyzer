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
package org.apache.rocketmq.tools.command.connection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.body.Connection;
import org.apache.rocketmq.remoting.protocol.body.ProducerConnection;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * producerConnection 子命令：查询指定 Topic 下生产者组的在线连接与版本。
 */
public class ProducerConnectionSubCommand implements SubCommand {

    @Override
    /** 返回子命令名 producerConnection。 */
    public String commandName() {
        return "producerConnection";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Query producer's socket connection and client version.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("g", "producerGroup", true, "生产者组名");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("t", "topic", true, "Topic 名");
        opt.setRequired(true);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 拉取 {@link ProducerConnection} 并逐行打印客户端连接信息。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);

        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        if (commandLine.hasOption('n')) {
            defaultMQAdminExt.setNamesrvAddr(commandLine.getOptionValue('n').trim());
        }

        try {
            defaultMQAdminExt.start();

            String group = commandLine.getOptionValue('g').trim();
            String topic = commandLine.getOptionValue('t').trim();

            ProducerConnection pc = defaultMQAdminExt.examineProducerConnectionInfo(group, topic);

            int i = 1;
            for (Connection conn : pc.getConnectionSet()) {
                System.out.printf("%04d  %-32s %-22s %-8s %s%n",
                    i++,
                    conn.getClientId(),
                    conn.getClientAddr(),
                    conn.getLanguage(),
                    MQVersion.getVersionDesc(conn.getVersion())
                );
            }
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}
