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

import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * checkMsgSendRT 子命令：向指定 Topic 循环发送测试消息并统计 RT。
 */
public class CheckMsgSendRTCommand implements SubCommand {
    /** 最近一次发送命中的 Broker 名。 */
    private static String brokerName = "";
    /** 最近一次发送命中的队列 ID。 */
    private static int queueId = 0;

    @Override
    /** 返回子命令名 checkMsgSendRT。 */
    public String commandName() {
        return "checkMsgSendRT";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Check message send response time.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("t", "topic", true, "目标 Topic 名");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("a", "amount", true, "发送条数，默认 100");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("s", "size", true, "消息体大小（字节），默认 128");
        opt.setRequired(false);
        options.addOption(opt);
        return options;
    }

    @Override
    /** 轮询队列发送测试消息，逐条打印 RT 并计算平均耗时。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        DefaultMQProducer producer = new DefaultMQProducer(rpcHook);
        producer.setProducerGroup(Long.toString(System.currentTimeMillis()));

        try {
            producer.start();
            long start = 0;
            long end = 0;
            long timeElapsed = 0;
            boolean sendSuccess = false;
            String topic = commandLine.getOptionValue('t').trim();
            long amount = !commandLine.hasOption('a') ? 100 : Long.parseLong(commandLine
                .getOptionValue('a').trim());
            long msgSize = !commandLine.hasOption('s') ? 128 : Long.parseLong(commandLine
                .getOptionValue('s').trim());
            Message msg = new Message(topic, getStringBySize(msgSize).getBytes(MixAll.DEFAULT_CHARSET));

            System.out.printf("%-32s  %-4s  %-20s    %s%n",
                "#Broker Name",
                "#QID",
                "#Send Result",
                "#RT"
            );
            for (int i = 0; i < amount; i++) {
                start = System.currentTimeMillis();
                try {
                    producer.send(msg, new MessageQueueSelector() {
                        @Override
                        public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                            int queueIndex = (Integer) arg % mqs.size();
                            MessageQueue queue = mqs.get(queueIndex);
                            brokerName = queue.getBrokerName();
                            queueId = queue.getQueueId();
                            return queue;
                        }
                    }, i);
                    sendSuccess = true;
                    end = System.currentTimeMillis();
                } catch (Exception e) {
                    sendSuccess = false;
                    end = System.currentTimeMillis();
                }

                if (i != 0) {
                    timeElapsed += end - start;
                }

                System.out.printf("%-32s  %-4s  %-20s    %s%n",
                    brokerName,
                    queueId,
                    sendSuccess,
                    end - start
                );
            }

            double rt = (double) timeElapsed / (amount - 1);
            System.out.printf("Avg RT: %s%n", String.format("%.2f", rt));
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            producer.shutdown();
        }
    }

    /** 生成指定长度的测试字符串。 */
    public String getStringBySize(long size) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < size; i++) {
            res.append('a');
        }
        return res.toString();
    }
}
