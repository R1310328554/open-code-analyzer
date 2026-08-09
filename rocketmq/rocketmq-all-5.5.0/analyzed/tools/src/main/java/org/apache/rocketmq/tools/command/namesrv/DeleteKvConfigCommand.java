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

/**
 * deleteKvConfig 子命令：从 NameServer 删除指定 KV 配置项。
 * <p>通过 namespace 与 key 定位待删除的配置。
 */
public class DeleteKvConfigCommand implements SubCommand {
    @Override
    public String commandName() {
        return "deleteKvConfig";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Delete KV config.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("s", "namespace", true, "KV 配置命名空间");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("k", "key", true, "配置项键名");
        opt.setRequired(true);
        options.addOption(opt);
        return options;
    }

    @Override
    /** 解析 namespace/key 并调用 deleteKvConfig 删除配置。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        try {
            // 解析命名空间与键名
            String namespace = commandLine.getOptionValue('s').trim();
            String key = commandLine.getOptionValue('k').trim();

            defaultMQAdminExt.start();
            defaultMQAdminExt.deleteKvConfig(namespace, key);
            System.out.printf("delete kv config from namespace success.%n");
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}
