/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.rocketmq;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/** RocketMQ 同步生产演示：向 {@link Constants#TEST_TOPIC_NAME} 发送 1000 条消息。 */
public class SyncProducer {

    public static void main(String[] args) throws Exception {
        // 指定生产组并连接 NameServer
        DefaultMQProducer producer = new DefaultMQProducer(Constants.TEST_GROUP_NAME);
        producer.setNamesrvAddr(Constants.TEST_NAMESRV_ADDR);
        // 启动 Producer
        producer.start();
        for (int i = 0; i < 1000; i++) {
            // 构造消息：Topic、Tag 与 body
            Message msg = new Message(Constants.TEST_TOPIC_NAME, "TagA",
                ("Hello RocketMQ From Sentinel " + i).getBytes(RemotingHelper.DEFAULT_CHARSET)
            );

            try {
                // 同步发送消息
                SendResult sendResult = producer.send(msg);
                System.out.printf("%s%n", sendResult);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 关闭 Producer
        producer.shutdown();
    }
}