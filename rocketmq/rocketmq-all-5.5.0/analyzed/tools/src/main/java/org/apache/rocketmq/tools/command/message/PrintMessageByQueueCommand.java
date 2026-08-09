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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * printMsgByQueue 子命令：按指定 Broker 与队列 ID 拉取并打印 Topic 消息。
 * <p>支持时间范围过滤、Tag 统计及消息体打印。
 */
public class PrintMessageByQueueCommand implements SubCommand {

    /** 将毫秒时间戳或 yyyy-MM-dd#HH:mm:ss:SSS 格式字符串解析为 long。 */
    public static long timestampFormat(final String value) {
        long timestamp = 0;
        try {
            timestamp = Long.parseLong(value);
        } catch (NumberFormatException e) {

            timestamp = UtilAll.parseDate(value, UtilAll.YYYY_MM_DD_HH_MM_SS_SSS).getTime();
        }

        return timestamp;
    }

    /** 按 Tag 统计消息数量（calByTag 为 true 时生效）。 */
    private static void calculateByTag(final List<MessageExt> msgs, final Map<String, AtomicLong> tagCalmap,
        final boolean calByTag) {
        if (!calByTag)
            return;

        for (MessageExt msg : msgs) {
            String tag = msg.getTags();
            if (StringUtils.isNotBlank(tag)) {
                AtomicLong count = tagCalmap.get(tag);
                if (count == null) {
                    count = new AtomicLong();
                    tagCalmap.put(tag, count);
                }
                count.incrementAndGet();
            }
        }
    }

    /** 打印各 Tag 的消息计数汇总。 */
    private static void printCalculateByTag(final Map<String, AtomicLong> tagCalmap, final boolean calByTag) {
        if (!calByTag)
            return;

        List<TagCountBean> list = new ArrayList<>();
        for (Map.Entry<String, AtomicLong> entry : tagCalmap.entrySet()) {
            TagCountBean tagBean = new TagCountBean(entry.getKey(), entry.getValue());
            list.add(tagBean);
        }
        Collections.sort(list);

        for (TagCountBean tagCountBean : list) {
            System.out.printf("Tag: %-30s Count: %s%n", tagCountBean.getTag(), tagCountBean.getCount());
        }
    }

    /** 按指定字符集打印消息 ID、属性及可选消息体。 */
    public static void printMessage(final List<MessageExt> msgs, final String charsetName, boolean printMsg,
        boolean printBody) {
        if (!printMsg)
            return;

        for (MessageExt msg : msgs) {
            try {
                System.out.printf("MSGID: %s %s BODY: %s%n", msg.getMsgId(), msg,
                    printBody ? new String(msg.getBody(), charsetName) : "NOT PRINT BODY");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String commandName() {
        return "printMsgByQueue";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Print Message Detail by queueId.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("t", "topic", true, "Topic 名称");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("a", "brokerName ", true, "Broker 名称");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("i", "queueId ", true, "队列 ID");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("c", "charsetName ", true, "消息体字符集（如 UTF-8、GBK）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("s", "subExpression ", true, "订阅表达式（如 TagA || TagB）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("b", "beginTimestamp ", true, "起始时间戳（毫秒或 yyyy-MM-dd#HH:mm:ss:SSS）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("e", "endTimestamp ", true, "结束时间戳（毫秒或 yyyy-MM-dd#HH:mm:ss:SSS）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("p", "print msg", true, "是否打印消息详情（true/false，默认 false）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("d", "printBody ", true, "是否打印消息体（true/false，默认 false）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("f", "calculate", true, "是否按 Tag 统计消息数（true/false，默认 false）");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 拉取指定队列消息并按选项打印或统计。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer(MixAll.TOOLS_CONSUMER_GROUP, rpcHook);

        try {
            String charsetName =
                !commandLine.hasOption('c') ? "UTF-8" : commandLine.getOptionValue('c').trim();
            boolean printMsg =
                commandLine.hasOption('p') && Boolean.parseBoolean(commandLine.getOptionValue('p').trim());
            boolean printBody =
                commandLine.hasOption('d') && Boolean.parseBoolean(commandLine.getOptionValue('d').trim());
            boolean calByTag =
                commandLine.hasOption('f') && Boolean.parseBoolean(commandLine.getOptionValue('f').trim());
            String subExpression =
                !commandLine.hasOption('s') ? "*" : commandLine.getOptionValue('s').trim();

            String topic = commandLine.getOptionValue('t').trim();
            String brokerName = commandLine.getOptionValue('a').trim();
            int queueId = Integer.parseInt(commandLine.getOptionValue('i').trim());
            consumer.start();

            MessageQueue mq = new MessageQueue(topic, brokerName, queueId);
            long minOffset = consumer.minOffset(mq);
            long maxOffset = consumer.maxOffset(mq);

            if (commandLine.hasOption('b')) {
                String timestampStr = commandLine.getOptionValue('b').trim();
                long timeValue = timestampFormat(timestampStr);
                minOffset = consumer.searchOffset(mq, timeValue);
            }

            if (commandLine.hasOption('e')) {
                String timestampStr = commandLine.getOptionValue('e').trim();
                long timeValue = timestampFormat(timestampStr);
                maxOffset = consumer.searchOffset(mq, timeValue);
            }

            final Map<String, AtomicLong> tagCalmap = new HashMap<>();
            // 从 minOffset 逐批拉取直至 maxOffset
            READQ:
            for (long offset = minOffset; offset < maxOffset; ) {
                try {
                    PullResult pullResult = consumer.pull(mq, subExpression, offset, 32);
                    offset = pullResult.getNextBeginOffset();
                    switch (pullResult.getPullStatus()) {
                        case FOUND:
                            calculateByTag(pullResult.getMsgFoundList(), tagCalmap, calByTag);
                            printMessage(pullResult.getMsgFoundList(), charsetName, printMsg, printBody);
                            break;
                        case NO_MATCHED_MSG:
                        case NO_NEW_MSG:
                        case OFFSET_ILLEGAL:
                            break READQ;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            printCalculateByTag(tagCalmap, calByTag);
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            consumer.shutdown();
        }
    }

    /** Tag 消息计数条目，用于排序输出。 */
    static class TagCountBean implements Comparable<TagCountBean> {
        private String tag;
        private AtomicLong count;

        public TagCountBean(final String tag, final AtomicLong count) {
            this.tag = tag;
            this.count = count;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(final String tag) {
            this.tag = tag;
        }

        public AtomicLong getCount() {
            return count;
        }

        public void setCount(final AtomicLong count) {
            this.count = count;
        }

        @Override
        public int compareTo(final TagCountBean o) {
            return (int) (o.getCount().get() - this.count.get());
        }
    }
}
