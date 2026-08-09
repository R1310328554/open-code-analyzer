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
package org.apache.rocketmq.tools.command.consumer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;
import org.apache.rocketmq.tools.monitor.DefaultMonitorListener;
import org.apache.rocketmq.tools.monitor.MonitorConfig;
import org.apache.rocketmq.tools.monitor.MonitorService;

/**
 * startMonitoring 子命令：启动 RocketMQ 集群监控服务。
 * <p>使用默认 {@link MonitorConfig} 与 {@link DefaultMonitorListener} 持续采集指标。
 */
public class StartMonitoringSubCommand implements SubCommand {

    @Override
    public String commandName() {
        return "startMonitoring";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Start Monitoring.";
    }

    @Override
    /** 本命令无需额外 CLI 参数。 */
    public Options buildCommandlineOptions(Options options) {
        return options;
    }

    @Override
    /** 创建并启动 {@link MonitorService}，阻塞运行直至异常退出。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        try {
            // 以默认配置与监听器启动监控服务
            MonitorService monitorService =
                new MonitorService(new MonitorConfig(), new DefaultMonitorListener(), rpcHook);

            monitorService.start();
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        }
    }
}
