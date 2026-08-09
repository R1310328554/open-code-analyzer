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
import java.util.Map;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * getControllerConfig 子命令：查询 Controller 集群的运行配置。
 * <p>支持传入多个 Controller 地址（分号分隔）。
 */
public class GetControllerConfigSubCommand implements SubCommand {
    @Override
    public String commandName() {
        return "getControllerConfig";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Get controller config.";
    }

    @Override
    public Options buildCommandlineOptions(final Options options) {
        Option opt = new Option("a", "controllerAddress", true, "Controller 地址列表，如 192.168.0.1:9878;192.168.0.2:9878");
        opt.setRequired(true);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 拉取各 Controller 配置并以键值对形式打印。 */
    public void execute(final CommandLine commandLine, final Options options,
        final RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        try {
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

            // 向各 Controller 拉取配置
            Map<String, Properties> controllerConfigs = defaultMQAdminExt.getControllerConfig(serverList);

            for (Map.Entry<String, Properties> controllerConfigEntry : controllerConfigs.entrySet()) {
                System.out.printf("============%s============\n",
                    controllerConfigEntry.getKey());
                for (Map.Entry<Object, Object> entry : controllerConfigEntry.getValue().entrySet()) {
                    System.out.printf("%-50s=  %s\n", entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}
