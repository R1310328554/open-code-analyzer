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

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * updateControllerConfig 子命令：更新 Controller 集群的运行配置项。
 * <p>通过 key/value 指定单个配置项，支持多个 Controller 地址。
 */
public class UpdateControllerConfigSubCommand implements SubCommand {
    @Override
    public String commandName() {
        return "updateControllerConfig";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Update controller config.";
    }

    @Override
    public Options buildCommandlineOptions(final Options options) {
        Option opt = new Option("a", "controllerAddress", true, "Controller 地址列表，如 192.168.0.1:9878;192.168.0.2:9878");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("k", "key", true, "配置项键名");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("v", "value", true, "配置项值");
        opt.setRequired(true);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 解析 key/value 并调用 updateControllerConfig 更新配置。 */
    public void execute(final CommandLine commandLine, final Options options,
        final RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        try {
            // 解析配置键名
            String key = commandLine.getOptionValue('k').trim();
            // key name
            String value = commandLine.getOptionValue('v').trim();
            Properties properties = new Properties();
            properties.put(key, value);

            // 解析分号分隔的 Controller 地址列表
            String servers = commandLine.getOptionValue('a');
            List<String> serverList = null;
            if (servers != null && servers.length() > 0) {
                String[] serverArray = servers.trim().split(";");

                if (serverArray.length > 0) {
                    serverList = Arrays.asList(serverArray);
                }
            }

            defaultMQAdminExt.start();

            defaultMQAdminExt.updateControllerConfig(properties, serverList);

            System.out.printf("update controller config success!%s\n%s : %s\n",
                serverList == null ? "" : serverList, key, value);
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}
