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
package org.apache.rocketmq.client.trace;

import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.client.exception.MQClientException;
import java.io.IOException;

/**
 * 轨迹异步传输接口：负责启动、追加轨迹上下文、刷盘与关闭。
 * 由 {@link org.apache.rocketmq.client.trace.AsyncTraceDispatcher} 等实现。
 */
public interface TraceDispatcher {
    /** 轨迹分发器角色：生产者侧或消费者侧。 */
    enum Type {
        PRODUCE,
        CONSUME
    }
    /**
     * 初始化异步轨迹传输模块，连接 NameServer 并注册客户端。
     */
    void start(String nameSrvAddr, AccessChannel accessChannel) throws MQClientException;

    /**
     * 追加一条轨迹上下文到发送队列。
     * @param ctx 轨迹上下文（通常为 {@link TraceContext}）
     * @return 是否成功入队
     */
    boolean append(Object ctx);

    /**
     * 强制刷盘/发送缓冲中的轨迹数据。
     *
     * @throws IOException 网络或 IO 异常
     */
    void flush() throws IOException;

    /** 关闭轨迹分发器，释放线程与网络资源。 */
    void shutdown();
}
