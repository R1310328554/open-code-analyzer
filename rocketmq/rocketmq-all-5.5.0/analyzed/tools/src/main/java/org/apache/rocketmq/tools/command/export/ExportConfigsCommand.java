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
package org.apache.rocketmq.tools.command.export;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * exportConfigs 子命令：导出指定集群的 Broker 运行配置与规模信息。
 * <p>输出 configs.json，含 brokerConfigs 与 clusterScale。
 */
public class ExportConfigsCommand implements SubCommand {
    @Override
    public String commandName() {
        return "exportConfigs";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Export configs.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("c", "clusterName", true, "待导出的集群名");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("f", "filePath", true,
            "导出目录路径，默认 /tmp/rocketmq/export");
        opt.setRequired(false);
        options.addOption(opt);
        return options;
    }

    @Override
    /** 收集 NameServer 与 Broker 配置并写入 configs.json。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook)
        throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            String clusterName = commandLine.getOptionValue('c').trim();
            String filePath = !commandLine.hasOption('f') ? "/tmp/rocketmq/export" : commandLine.getOptionValue('f')
                .trim();

            defaultMQAdminExt.start();
            Map<String, Object> result = new HashMap<>();
            // 获取 NameServer 地址列表
            List<String> nameServerAddressList = defaultMQAdminExt.getNameServerAddressList();

            // 遍历集群 Master/Slave 并收集 Broker 配置
            int masterBrokerSize = 0;
            int slaveBrokerSize = 0;
            Map<String, Properties> brokerConfigs = new HashMap<>();
            Map<String, List<String>> masterAndSlaveMap
                = CommandUtil.fetchMasterAndSlaveDistinguish(defaultMQAdminExt, clusterName);
            for (Entry<String, List<String>> masterAndSlaveEntry : masterAndSlaveMap.entrySet()) {
                Properties masterProperties = defaultMQAdminExt.getBrokerConfig(masterAndSlaveEntry.getKey());
                masterBrokerSize++;
                slaveBrokerSize += masterAndSlaveEntry.getValue().size();

                brokerConfigs.put(masterProperties.getProperty("brokerName"), needBrokerProprties(masterProperties));
            }

            Map<String, Integer> clusterScaleMap = new HashMap<>();
            clusterScaleMap.put("namesrvSize", nameServerAddressList.size());
            clusterScaleMap.put("masterBrokerSize", masterBrokerSize);
            clusterScaleMap.put("slaveBrokerSize", slaveBrokerSize);

            result.put("brokerConfigs", brokerConfigs);
            result.put("clusterScale", clusterScaleMap);

            String path = filePath + "/configs.json";
            MixAll.string2FileNotSafe(JSON.toJSONString(result, JSONWriter.Feature.PrettyFormat), path);
            System.out.printf("export %s success", path);
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }


    /** 从完整 Broker 配置中筛选需要导出的关键属性。 */
    private Properties needBrokerProprties(Properties properties) {
        List<String> propertyKeys = Arrays.asList(
                "brokerClusterName",
                "brokerId",
                "brokerName",
                "brokerRole",
                "fileReservedTime",
                "filterServerNums",
                "flushDiskType",
                "maxMessageSize",
                "messageDelayLevel",
                "msgTraceTopicName",
                "slaveReadEnable",
                "traceOn",
                "traceTopicEnable",
                "useTLS",
                "autoCreateTopicEnable",
                "autoCreateSubscriptionGroup"
        );

        Properties newProperties = new Properties();
        propertyKeys.stream()
                .filter(key -> properties.getProperty(key) != null)
                .forEach(key -> newProperties.setProperty(key, properties.getProperty(key)));

        return newProperties;
    }

}
