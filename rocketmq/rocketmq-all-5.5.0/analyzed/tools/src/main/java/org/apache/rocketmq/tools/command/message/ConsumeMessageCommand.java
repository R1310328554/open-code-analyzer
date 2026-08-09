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

package org.apache.rocketmq.tools.command.message;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

import java.util.Set;

/**
 * consumeMessage 子命令：以 Pull 方式拉取并打印 Topic 消息（支持队列/偏移/时间过滤）。
 */
public class ConsumeMessageCommand implements SubCommand {

    /** 待消费的 Topic 名。 */
    private String topic = null;
    /** 最多拉取的消息条数，默认 128。 */
    private long messageCount = 128;
    /** Pull 消费者实例，可复用。 */
    private DefaultMQPullConsumer defaultMQPullConsumer;


    /** 消费模式：按 Topic 全队列、指定队列或指定偏移拉取。 */
    public enum ConsumeType {
        /**
         * 仅按 Topic 遍历全部队列
         */
        DEFAULT,
        /**
         * 指定 brokerName 与 queueId
         */
        BYQUEUE,
        /**
         * 指定 brokerName、queueId 与起始 offset
         */
        BYOFFSET
    }

    /** 解析毫秒时间戳或 yyyy-MM-dd#HH:mm:ss:SSS 格式字符串。 */
    private static long timestampFormat(final String value) {
        long timestamp;
        try {
            timestamp = Long.parseLong(value);
        } catch (NumberFormatException e) {
            timestamp = UtilAll.parseDate(value, UtilAll.YYYY_MM_DD_HH_MM_SS_SSS).getTime();
        }

        return timestamp;
    }
    @Override
    /** 返回子命令名 consumeMessage。 */
    public String commandName() {
        return "consumeMessage";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Consume message.";
    }

    @Override
    public Options buildCommandlineOptions(final Options options) {
        Option opt = new Option("t", "topic", true, "Topic 名");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("b", "brokerName", true, "Broker 名（与 -i 配合）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("i", "queueId", true, "队列 ID（需先指定 -b）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("o", "offset", true, "起始队列偏移（需先指定 -i）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("g", "consumerGroup", true, "Pull 消费组名");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("s", "beginTimestamp ", true,
                "起始时间戳（毫秒或 yyyy-MM-dd#HH:mm:ss:SSS）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("e", "endTimestamp ", true,
                "结束时间戳（毫秒或 yyyy-MM-dd#HH:mm:ss:SSS）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("c", "MessageNumber", true, "最多消费的消息条数");
        opt.setRequired(false);
        options.addOption(opt);


        return options;

    }

    @Override
    /** 解析消费模式与时间范围，按 DEFAULT/BYQUEUE/BYOFFSET 拉取消息。 */
    public void execute(final CommandLine commandLine, final Options options, RPCHook rpcHook) throws SubCommandException {
        if (defaultMQPullConsumer == null) {
            defaultMQPullConsumer = new DefaultMQPullConsumer(MixAll.TOOLS_CONSUMER_GROUP, rpcHook);
        }
        defaultMQPullConsumer.setInstanceName(Long.toString(System.currentTimeMillis()));

        long offset = 0;
        long timeValueEnd = 0;
        long timeValueBegin = 0;
        String queueId = null;
        String brokerName = null;
        ConsumeType consumeType = ConsumeType.DEFAULT;

        try {
            /* Group name must be set before consumer start */
            if (commandLine.hasOption('g')) {
                String consumerGroup = commandLine.getOptionValue('g').trim();
                defaultMQPullConsumer.setConsumerGroup(consumerGroup);
            }

            defaultMQPullConsumer.start();

            topic = commandLine.getOptionValue('t').trim();

            if (commandLine.hasOption('c')) {
                messageCount = Long.parseLong(commandLine.getOptionValue('c').trim());
                if (messageCount <= 0) {
                    System.out.print("Please input a positive messageNumber!");
                    return;
                }
            }
            if (commandLine.hasOption('b')) {
                brokerName = commandLine.getOptionValue('b').trim();

            }
            if (commandLine.hasOption('i')) {
                if (!commandLine.hasOption('b')) {
                    System.out.print("Please set the brokerName before queueId!");
                    return;
                }
                queueId = commandLine.getOptionValue('i').trim();

                consumeType = ConsumeType.BYQUEUE;
            }
            if (commandLine.hasOption('o')) {
                if (consumeType != ConsumeType.BYQUEUE) {
                    System.out.print("Please set queueId before offset!");
                    return;
                }
                offset = Long.parseLong(commandLine.getOptionValue('o').trim());
                consumeType = ConsumeType.BYOFFSET;
            }

            long now = System.currentTimeMillis();
            if (commandLine.hasOption('s')) {
                String timestampStr = commandLine.getOptionValue('s').trim();
                timeValueBegin = timestampFormat(timestampStr);
                if (timeValueBegin > now) {
                    System.out.print("Please set the beginTimestamp before now!");
                    return;
                }
            }
            if (commandLine.hasOption('e')) {
                String timestampStr = commandLine.getOptionValue('e').trim();
                timeValueEnd = timestampFormat(timestampStr);
                if (timeValueEnd > now) {
                    System.out.print("Please set the endTimestamp before now!");
                    return;
                }
                if (timeValueBegin > timeValueEnd) {
                    System.out.print("Please make sure that the beginTimestamp is less than or equal to the endTimestamp");
                    return;
                }
            }

            switch (consumeType) {
                case DEFAULT:
                    executeDefault(timeValueBegin, timeValueEnd);
                    break;
                case BYOFFSET:
                    executeByCondition(brokerName, queueId, offset, timeValueBegin, timeValueEnd);
                    break;
                case BYQUEUE:
                    executeByCondition(brokerName, queueId, 0, timeValueBegin, timeValueEnd);
                    break;
                default:
                    System.out.print("Unknown type of consume!");
                    break;
            }

        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQPullConsumer.shutdown();
        }
    }

    /** 在指定偏移区间内循环 Pull 并打印消息内容。 */
    private void pullMessageByQueue(MessageQueue mq, long minOffset, long maxOffset) {
        READQ:
        for (long offset = minOffset; offset <= maxOffset; ) {
            PullResult pullResult = null;
            try {
                pullResult = defaultMQPullConsumer.pull(mq, "*", offset, (int)(maxOffset - offset + 1));
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            if (pullResult != null) {
                offset = pullResult.getNextBeginOffset();
                switch (pullResult.getPullStatus()) {
                    case FOUND:
                        System.out.print("Consume ok\n");
                        PrintMessageByQueueCommand.printMessage(pullResult.getMsgFoundList(), "UTF-8",
                            true, true);
                        break;
                    case NO_MATCHED_MSG:
                        System.out.printf("%s no matched msg. status=%s, offset=%s\n", mq, pullResult.getPullStatus(),
                            offset);
                        break;
                    case NO_NEW_MSG:
                    case OFFSET_ILLEGAL:
                        System.out.printf("%s print msg finished. status=%s, offset=%s\n", mq,
                            pullResult.getPullStatus(), offset);
                        break READQ;
                    default:
                        break;
                }
            }
        }
    }

    /** 遍历 Topic 全部队列，按时间与条数限制拉取消息。 */
    private void executeDefault(long timeValueBegin, long timeValueEnd) {
        try {
            Set<MessageQueue> mqs = defaultMQPullConsumer.fetchSubscribeMessageQueues(topic);
            long countLeft = messageCount;
            for (MessageQueue mq : mqs) {
                if (countLeft == 0) {
                    return;
                }
                long minOffset = defaultMQPullConsumer.minOffset(mq);
                long maxOffset = defaultMQPullConsumer.maxOffset(mq);
                if (timeValueBegin > 0) {
                    minOffset = defaultMQPullConsumer.searchOffset(mq, timeValueBegin);
                }
                if (timeValueEnd > 0) {
                    maxOffset = defaultMQPullConsumer.searchOffset(mq, timeValueEnd);
                }
                if (maxOffset - minOffset > countLeft) {
                    System.out.printf("The older %d message of the %d queue will be provided\n", countLeft, mq.getQueueId());
                    maxOffset = minOffset + countLeft - 1;
                    countLeft = 0;
                } else {
                    countLeft = countLeft - (maxOffset - minOffset) - 1;
                }

                pullMessageByQueue(mq, minOffset, maxOffset);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 在指定 Broker/队列/偏移上按时间窗口拉取消息。 */
    private void executeByCondition(String brokerName, String queueId, long offset, long timeValueBegin, long timeValueEnd) {
        MessageQueue mq = new MessageQueue(topic, brokerName, Integer.parseInt(queueId));
        try {
            long minOffset = defaultMQPullConsumer.minOffset(mq);
            long maxOffset = defaultMQPullConsumer.maxOffset(mq);
            if (timeValueBegin > 0) {
                minOffset = defaultMQPullConsumer.searchOffset(mq, timeValueBegin);
            }
            if (timeValueEnd > 0) {
                maxOffset = defaultMQPullConsumer.searchOffset(mq, timeValueEnd);
            }
            if (offset > maxOffset) {
                System.out.printf("%s no matched msg, offset=%s\n", mq, offset);
                return;
            }
            minOffset = minOffset > offset ? minOffset : offset;
            if (maxOffset - minOffset > messageCount) {
                System.out.printf("The oldler %d message will be provided\n", messageCount);
                maxOffset = minOffset + messageCount - 1;
            }

            pullMessageByQueue(mq, minOffset, maxOffset);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}