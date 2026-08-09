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
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.body.SubscriptionGroupWrapper;
import org.apache.rocketmq.remoting.protocol.body.TopicConfigSerializeWrapper;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;
import org.apache.rocketmq.srvutil.ServerUtil;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * exportMetadata 子命令：导出 Topic 与订阅组元数据。
 * <p>支持按 Broker 或集群导出，可仅导出 Topic 或订阅组。
 */
public class ExportMetadataCommand implements SubCommand {

    /** 默认导出目录路径。 */
    private static final String DEFAULT_FILE_PATH = "/tmp/rocketmq/export";

    @Override
    public String commandName() {
        return "exportMetadata";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Export metadata.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("c", "clusterName", true, "待导出的集群名（与 -b 二选一）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("b", "brokerAddr", true, "待导出的 Broker 地址（与 -c 二选一）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("f", "filePath", true, "导出目录路径，默认 /tmp/rocketmq/export");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("t", "topic", false, "仅导出 Topic 元数据");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("g", "subscriptionGroup", false, "仅导出订阅组元数据");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("s", "specialTopic", false, "是否包含重试 Topic 与死信 Topic");
        opt.setRequired(false);
        options.addOption(opt);
        return options;
    }

    @Override
    /** 按 Broker 或集群导出 Topic/订阅组元数据到 JSON 文件。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook)
        throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);

        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            defaultMQAdminExt.start();

            String filePath = !commandLine.hasOption('f') ? DEFAULT_FILE_PATH : commandLine.getOptionValue('f')
                .trim();

            boolean specialTopic = commandLine.hasOption('s');

            if (commandLine.hasOption('b')) {
                final String brokerAddr = commandLine.getOptionValue('b').trim();

                if (commandLine.hasOption('t')) {
                    filePath = filePath + "/topic.json";
                    TopicConfigSerializeWrapper topicConfigSerializeWrapper = defaultMQAdminExt.getUserTopicConfig(
                        brokerAddr, specialTopic, 10000L);
                    MixAll.string2FileNotSafe(JSON.toJSONString(topicConfigSerializeWrapper, JSONWriter.Feature.PrettyFormat), filePath);
                    System.out.printf("export %s success", filePath);
                } else if (commandLine.hasOption('g')) {
                    filePath = filePath + "/subscriptionGroup.json";
                    SubscriptionGroupWrapper subscriptionGroupWrapper = defaultMQAdminExt.getUserSubscriptionGroup(
                        brokerAddr, 10000L);
                    MixAll.string2FileNotSafe(JSON.toJSONString(subscriptionGroupWrapper, JSONWriter.Feature.PrettyFormat), filePath);
                    System.out.printf("export %s success", filePath);
                }
            } else if (commandLine.hasOption('c')) {
                String clusterName = commandLine.getOptionValue('c').trim();

                // 集群模式：遍历各 Master 合并 Topic 与订阅组元数据
                Set<String> masterSet =
                    CommandUtil.fetchMasterAddrByClusterName(defaultMQAdminExt, clusterName);

                Map<String, TopicConfig> topicConfigMap = new HashMap<>();
                Map<String, SubscriptionGroupConfig> subGroupConfigMap = new HashMap<>();
                Map<String, Object> result = new HashMap<>();

                for (String addr : masterSet) {
                    TopicConfigSerializeWrapper topicConfigSerializeWrapper = defaultMQAdminExt.getUserTopicConfig(
                        addr, specialTopic, 10000L);

                    SubscriptionGroupWrapper subscriptionGroupWrapper = defaultMQAdminExt.getUserSubscriptionGroup(
                        addr, 10000);

                    for (Map.Entry<String, TopicConfig> entry : topicConfigSerializeWrapper.getTopicConfigTable()
                        .entrySet()) {
                        TopicConfig topicConfig = topicConfigMap.get(entry.getKey());
                        if (null != topicConfig) {
                            entry.getValue().setWriteQueueNums(
                                topicConfig.getWriteQueueNums() + entry.getValue().getWriteQueueNums());
                            entry.getValue().setReadQueueNums(
                                topicConfig.getReadQueueNums() + entry.getValue().getReadQueueNums());
                        }
                        topicConfigMap.put(entry.getKey(), entry.getValue());
                    }

                    for (Map.Entry<String, SubscriptionGroupConfig> entry : subscriptionGroupWrapper.getSubscriptionGroupTable()
                        .entrySet()) {

                        SubscriptionGroupConfig subscriptionGroupConfig = subGroupConfigMap.get(entry.getKey());
                        if (null != subscriptionGroupConfig) {
                            entry.getValue().setRetryQueueNums(
                                subscriptionGroupConfig.getRetryQueueNums() + entry.getValue().getRetryQueueNums());
                        }
                        subGroupConfigMap.put(entry.getKey(), entry.getValue());
                    }

                }

                String exportPath;
                if (commandLine.hasOption('t')) {
                    result.put("topicConfigTable", topicConfigMap);
                    exportPath = filePath + "/topic.json";
                } else if (commandLine.hasOption('g')) {
                    result.put("subscriptionGroupTable", subGroupConfigMap);
                    exportPath = filePath + "/subscriptionGroup.json";
                } else {
                    result.put("topicConfigTable", topicConfigMap);
                    result.put("subscriptionGroupTable", subGroupConfigMap);
                    exportPath = filePath + "/metadata.json";
                }
                result.put("exportTime", System.currentTimeMillis());
                MixAll.string2FileNotSafe(JSON.toJSONString(result, JSONWriter.Feature.PrettyFormat), exportPath);
                System.out.printf("export %s success%n", exportPath);

            } else {
                ServerUtil.printCommandLineHelp("mqadmin " + this.commandName(), options);
            }
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}

