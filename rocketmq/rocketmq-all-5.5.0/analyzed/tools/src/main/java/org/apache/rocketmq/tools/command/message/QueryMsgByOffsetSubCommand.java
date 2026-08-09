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

import java.nio.charset.Charset;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * queryMsgByOffset 子命令：按队列位点精确查询单条消息。
 * <p>支持 LMQ routeTopic 路由及消息体格式化输出。
 */
public class QueryMsgByOffsetSubCommand implements SubCommand {

    @Override
    public String commandName() {
        return "queryMsgByOffset";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Query Message by offset.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("t", "topic", true, "Topic 名称");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("b", "brokerName", true, "Broker 名称");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("i", "queueId", true, "队列 ID");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("o", "offset", true, "队列消费位点");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("f", "bodyFormat", true, "消息体字符集格式（如 UTF-8）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("r", "routeTopic", true, "用于查找路由的 Topic（LMQ 场景）");
        opt.setRequired(false);
        options.addOption(opt);
        return options;
    }

    @Override
    /** 按位点拉取单条消息并打印详情。 */
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        DefaultMQPullConsumer defaultMQPullConsumer = new DefaultMQPullConsumer(MixAll.TOOLS_CONSUMER_GROUP, rpcHook);

        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        defaultMQPullConsumer.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            String topic = commandLine.getOptionValue('t').trim();
            String brokerName = commandLine.getOptionValue('b').trim();
            String queueId = commandLine.getOptionValue('i').trim();
            String offset = commandLine.getOptionValue('o').trim();
            String routeTopic = commandLine.hasOption('r') ? commandLine.getOptionValue('r').trim() : null;
            Charset msgBodyCharset = null;
            if (commandLine.hasOption('f')) {
                msgBodyCharset = Charset.forName(commandLine.getOptionValue('f').trim());
            }

            MessageQueue mq = new MessageQueue();
            mq.setTopic(topic);
            mq.setBrokerName(brokerName);
            mq.setQueueId(Integer.parseInt(queueId));

            defaultMQPullConsumer.start();
            defaultMQAdminExt.start();

            if (StringUtils.isNotEmpty(routeTopic) && !routeTopic.equals(topic)) {
                // 通过 routeTopic 预拉取以刷新 LMQ 路由信息
                defaultMQPullConsumer.pull(new MessageQueue(routeTopic, brokerName, 0), "*", 0, 1);
            }
            PullResult pullResult = defaultMQPullConsumer.pull(mq, "*", Long.parseLong(offset), 1);
            if (pullResult != null) {
                switch (pullResult.getPullStatus()) {
                    case FOUND:
                        QueryMsgByIdSubCommand.printMsg(defaultMQAdminExt, pullResult.getMsgFoundList().get(0), msgBodyCharset);
                        break;
                    case NO_MATCHED_MSG:
                    case NO_NEW_MSG:
                    case OFFSET_ILLEGAL:
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQPullConsumer.shutdown();
            defaultMQAdminExt.shutdown();
        }
    }
}
