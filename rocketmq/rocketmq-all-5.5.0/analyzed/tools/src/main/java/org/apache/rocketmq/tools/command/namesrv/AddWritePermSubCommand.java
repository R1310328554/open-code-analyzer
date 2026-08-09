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
package org.apache.rocketmq.tools.command.namesrv;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

import java.util.List;

/**
 * addWritePerm 子命令：为指定 Broker 在所有 NameServer 上恢复写权限。
 * <p>Broker 被禁止写入后，可通过此命令重新开放 Topic 写权限。
 */
public class AddWritePermSubCommand implements SubCommand {
    @Override
    public String commandName() {
        return "addWritePerm";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Add write perm of broker in all name server you defined in the -n param.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("b", "brokerName", true, "目标 Broker 名称");
        opt.setRequired(true);
        options.addOption(opt);
        return options;
    }

    @Override
    /** 遍历全部 NameServer 为 Broker 添加写权限。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        try {
            defaultMQAdminExt.start();
            String brokerName = commandLine.getOptionValue('b').trim();
            List<String> namesrvList = defaultMQAdminExt.getNameServerAddressList();
            if (namesrvList != null) {
                for (String namesrvAddr : namesrvList) {
                    try {
                        int addTopicCount = defaultMQAdminExt.addWritePermOfBroker(namesrvAddr, brokerName);
                        System.out.printf("add write perm of broker[%s] in name server[%s] OK, %d%n",
                                brokerName,
                                namesrvAddr,
                                addTopicCount
                        );
                    } catch (Exception e) {
                        System.out.printf("add write perm of broker[%s] in name server[%s] Failed%n",
                                brokerName,
                                namesrvAddr
                        );
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + "command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}
