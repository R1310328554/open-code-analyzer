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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.common.KeyBuilder;
import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.admin.OffsetWrapper;
import org.apache.rocketmq.remoting.protocol.body.Connection;
import org.apache.rocketmq.remoting.protocol.body.ConsumerConnection;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.remoting.protocol.heartbeat.ConsumeType;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * consumerProgress 子命令：查询消费组各队列偏移、堆积量与消费 TPS。
 */
public class ConsumerProgressSubCommand implements SubCommand {
    /** 日志记录器。 */
    private static final Logger log = LoggerFactory.getLogger(ConsumerProgressSubCommand.class);

    @Override
    /** 返回子命令名 consumerProgress。 */
    public String commandName() {
        return "consumerProgress";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Query consumer's progress, speed.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("g", "groupName", true, "消费组名（缺省则扫描全部重试组）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("t", "topicName", true, "可选，限定 Topic 的消费进度");
        opt.setRequired(false);
        options.addOption(opt);

        Option optionShowClientIP = new Option("s", "showClientIP", true, "是否显示各队列分配的客户端 IP");
        optionShowClientIP.setRequired(false);
        options.addOption(optionShowClientIP);

        opt = new Option("c", "cluster", true, "集群名或 LMQ 父 Topic，用于路由查询");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    /** 汇总各消费实例当前分配的 {@link MessageQueue} 与客户端 IP。 */
    private Map<MessageQueue, String> getMessageQueueAllocationResult(DefaultMQAdminExt defaultMQAdminExt,
        String groupName) {
        Map<MessageQueue, String> results = new HashMap<>();
        try {
            ConsumerConnection consumerConnection = defaultMQAdminExt.examineConsumerConnectionInfo(groupName);
            for (Connection connection : consumerConnection.getConnectionSet()) {
                String clientId = connection.getClientId();
                ConsumerRunningInfo consumerRunningInfo = defaultMQAdminExt.getConsumerRunningInfo(groupName, clientId,
                    false, false);
                for (MessageQueue messageQueue : consumerRunningInfo.getMqTable().keySet()) {
                    results.put(messageQueue, clientId.split("@")[0]);
                }
            }
        } catch (Exception e) {
            log.error("getMqAllocationsResult error, ", e);
        }
        return results;
    }

    @Override
    /** 按 -g 查询单组进度，或扫描全部 %RETRY% 消费组汇总列表。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        if (commandLine.hasOption('n')) {
            defaultMQAdminExt.setNamesrvAddr(commandLine.getOptionValue('n').trim());
        }

        try {
            defaultMQAdminExt.start();

            boolean showClientIP = commandLine.hasOption('s')
                && "true".equalsIgnoreCase(commandLine.getOptionValue('s'));

            String clusterName = commandLine.hasOption('c') ? commandLine.getOptionValue('c').trim() : null;

            if (commandLine.hasOption('g')) {
                String consumerGroup = commandLine.getOptionValue('g').trim();
                String topicName = commandLine.hasOption('t') ? commandLine.getOptionValue('t').trim() : null;
                ConsumeStats consumeStats;
                if (topicName == null) {
                    consumeStats = defaultMQAdminExt.examineConsumeStats(consumerGroup);
                } else {
                    consumeStats = defaultMQAdminExt.examineConsumeStats(clusterName, consumerGroup, topicName);
                }
                List<MessageQueue> mqList = new LinkedList<>(consumeStats.getOffsetTable().keySet());
                Collections.sort(mqList);

                Map<MessageQueue, String> messageQueueAllocationResult = null;
                if (showClientIP) {
                    messageQueueAllocationResult = getMessageQueueAllocationResult(defaultMQAdminExt, consumerGroup);
                }
                if (showClientIP) {
                    System.out.printf("%-64s  %-32s  %-4s  %-20s  %-20s  %-20s %-20s %-20s%s%n",
                            "#Topic",
                            "#Broker Name",
                            "#QID",
                            "#Broker Offset",
                            "#Consumer Offset",
                            "#Client IP",
                            "#Diff",
                            "#Inflight",
                            "#LastTime");
                } else {
                    System.out.printf("%-64s  %-32s  %-4s  %-20s  %-20s  %-20s %-20s%s%n",
                            "#Topic",
                            "#Broker Name",
                            "#QID",
                            "#Broker Offset",
                            "#Consumer Offset",
                            "#Diff",
                            "#Inflight",
                            "#LastTime");
                }
                long diffTotal = 0L;
                long inflightTotal = 0L;
                for (MessageQueue mq : mqList) {
                    OffsetWrapper offsetWrapper = consumeStats.getOffsetTable().get(mq);
                    long diff = offsetWrapper.getBrokerOffset() - offsetWrapper.getConsumerOffset();
                    long inflight = offsetWrapper.getPullOffset() - offsetWrapper.getConsumerOffset();
                    diffTotal += diff;
                    inflightTotal += inflight;
                    String lastTime = "";
                    try {
                        if (offsetWrapper.getLastTimestamp() == 0) {
                            lastTime = "N/A";
                        } else {
                            lastTime = UtilAll.formatDate(new Date(offsetWrapper.getLastTimestamp()), UtilAll.YYYY_MM_DD_HH_MM_SS);
                        }
                    } catch (Exception e) {
                        // 忽略时间戳格式化异常
                    }

                    String clientIP = null;
                    if (showClientIP) {
                        clientIP = messageQueueAllocationResult.get(mq);
                    }
                    if (showClientIP) {
                        System.out.printf("%-64s  %-32s  %-4d  %-20d  %-20d  %-20s %-20d %-20d %s%n",
                                UtilAll.frontStringAtLeast(mq.getTopic(), 64),
                                UtilAll.frontStringAtLeast(mq.getBrokerName(), 32),
                                mq.getQueueId(),
                                offsetWrapper.getBrokerOffset(),
                                offsetWrapper.getConsumerOffset(),
                                null != clientIP ? clientIP : "N/A",
                                diff,
                                inflight,
                                lastTime
                        );
                    } else {
                        System.out.printf("%-64s  %-32s  %-4d  %-20d  %-20d  %-20d %-20d %s%n",
                                UtilAll.frontStringAtLeast(mq.getTopic(), 64),
                                UtilAll.frontStringAtLeast(mq.getBrokerName(), 32),
                                mq.getQueueId(),
                                offsetWrapper.getBrokerOffset(),
                                offsetWrapper.getConsumerOffset(),
                                diff,
                                inflight,
                                lastTime
                        );
                    }
                }

                System.out.printf("%n");
                System.out.printf("Consume TPS: %.2f%n", consumeStats.getConsumeTps());
                System.out.printf("Consume Diff Total: %d%n", diffTotal);
                System.out.printf("Consume Inflight Total: %d%n", inflightTotal);
            } else {
                System.out.printf("%-64s  %-6s  %-24s %-5s  %-14s  %-7s  %s%n",
                    "#Group",
                    "#Count",
                    "#Version",
                    "#Type",
                    "#Model",
                    "#TPS",
                    "#Diff Total"
                );
                TopicList topicList = defaultMQAdminExt.fetchAllTopicList();
                for (String topic : topicList.getTopicList()) {
                    if (topic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)) {
                        String consumerGroup = KeyBuilder.parseGroup(topic);
                        try {
                            ConsumeStats consumeStats = null;
                            try {
                                consumeStats = defaultMQAdminExt.examineConsumeStats(consumerGroup);
                            } catch (Exception e) {
                                log.warn("examineConsumeStats exception, " + consumerGroup, e);
                            }

                            ConsumerConnection cc = null;
                            try {
                                cc = defaultMQAdminExt.examineConsumerConnectionInfo(consumerGroup);
                            } catch (Exception e) {
                                log.warn("examineConsumerConnectionInfo exception, " + consumerGroup, e);
                            }

                            GroupConsumeInfo groupConsumeInfo = new GroupConsumeInfo();
                            groupConsumeInfo.setGroup(consumerGroup);

                            if (consumeStats != null) {
                                groupConsumeInfo.setConsumeTps((int) consumeStats.getConsumeTps());
                                groupConsumeInfo.setDiffTotal(consumeStats.computeTotalDiff());
                            }

                            if (cc != null) {
                                groupConsumeInfo.setCount(cc.getConnectionSet().size());
                                groupConsumeInfo.setMessageModel(cc.getMessageModel());
                                groupConsumeInfo.setConsumeType(cc.getConsumeType());
                                groupConsumeInfo.setVersion(cc.computeMinVersion());
                            }

                            System.out.printf("%-64s  %-6d  %-24s %-5s  %-14s  %-7d  %d%n",
                                UtilAll.frontStringAtLeast(groupConsumeInfo.getGroup(), 64),
                                groupConsumeInfo.getCount(),
                                groupConsumeInfo.getCount() > 0 ? groupConsumeInfo.versionDesc() : "OFFLINE",
                                groupConsumeInfo.consumeTypeDesc(),
                                groupConsumeInfo.messageModelDesc(),
                                groupConsumeInfo.getConsumeTps(),
                                groupConsumeInfo.getDiffTotal()
                            );
                        } catch (Exception e) {
                            log.warn("examineConsumeStats or examineConsumerConnectionInfo exception, " + consumerGroup, e);
                        }
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

/**
 * 消费组汇总信息：用于 consumerProgress 无 -g 时批量展示各重试组状态。
 */
class GroupConsumeInfo implements Comparable<GroupConsumeInfo> {
    /** 消费组名。 */
    private String group;
    /** 组内最低客户端协议版本号。 */
    private int version;
    /** 在线消费实例数量。 */
    private int count;
    /** 消费类型（Push/Pull）。 */
    private ConsumeType consumeType;
    /** 消息模式（集群/广播）。 */
    private MessageModel messageModel;
    /** 消费 TPS。 */
    private int consumeTps;
    /** 总堆积量（BrokerOffset - ConsumerOffset 之和）。 */
    private long diffTotal;

    /** 返回消费组名。 */
    public String getGroup() {
        return group;
    }

    /** 设置消费组名。 */
    public void setGroup(String group) {
        this.group = group;
    }

    /** 返回 PULL/PUSH 描述；离线时返回空串。 */
    public String consumeTypeDesc() {
        if (this.count != 0) {
            return this.getConsumeType() == ConsumeType.CONSUME_ACTIVELY ? "PULL" : "PUSH";
        }
        return "";
    }

    public ConsumeType getConsumeType() {
        return consumeType;
    }

    public void setConsumeType(ConsumeType consumeType) {
        this.consumeType = consumeType;
    }

    /** Push 模式下返回消息模型描述。 */
    public String messageModelDesc() {
        if (this.count != 0 && this.getConsumeType() == ConsumeType.CONSUME_PASSIVELY) {
            return this.getMessageModel().toString();
        }
        return "";
    }

    public MessageModel getMessageModel() {
        return messageModel;
    }

    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }

    /** 返回最低客户端版本描述。 */
    public String versionDesc() {
        if (this.count != 0) {
            return MQVersion.getVersionDesc(this.version);
        }
        return "";
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getDiffTotal() {
        return diffTotal;
    }

    public void setDiffTotal(long diffTotal) {
        this.diffTotal = diffTotal;
    }

    /** 按在线数、堆积量降序排序。 */
    @Override
    public int compareTo(GroupConsumeInfo o) {
        if (this.count != o.count) {
            return o.count - this.count;
        }

        return (int) (o.diffTotal - diffTotal);
    }

    public int getConsumeTps() {
        return consumeTps;
    }

    public void setConsumeTps(int consumeTps) {
        this.consumeTps = consumeTps;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
