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

package org.apache.rocketmq.proxy.remoting.common;

import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * Remoting 消息转换工具：单例模式，将 {@link MessageExt} 编码为字节数组。
 */
public class RemotingConverter {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.PROXY_LOGGER_NAME);

    protected static final Object INSTANCE_CREATE_LOCK = new Object();
    /** 双重检查锁定的单例实例。 */
    protected static volatile RemotingConverter instance;

    /** 获取全局唯一 RemotingConverter 实例。 */
    public static RemotingConverter getInstance() {
        if (instance == null) {
            synchronized (INSTANCE_CREATE_LOCK) {
                if (instance == null) {
                    instance = new RemotingConverter();
                }
            }
        }
        return instance;
    }

    /** 重置 storeSize 后编码消息体，供 Remoting 请求携带。 */
    public byte[] convertMsgToBytes(final MessageExt msg) throws Exception {
        // 置零 storeSize 以便重新计算存储大小
        msg.setStoreSize(0);
        // Topic 长度超过 byte 上限时记录警告
        if (msg.getTopic().length() > Byte.MAX_VALUE) {
            log.warn("Topic length is too long, topic: {}", msg.getTopic());
        }
        return MessageDecoder.encode(msg, false);
    }
}
