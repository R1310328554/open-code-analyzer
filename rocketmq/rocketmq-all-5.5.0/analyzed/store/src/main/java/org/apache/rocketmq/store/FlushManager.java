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

package org.apache.rocketmq.store;

import java.util.concurrent.CompletableFuture;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * 刷盘管理器接口：负责 CommitLog 落盘、唤醒刷盘/提交线程及异步刷盘回调。
 */
public interface FlushManager {

    /** 启动刷盘相关线程。 */
    void start();

    /** 关闭刷盘服务。 */
    void shutdown();

    /** 唤醒刷盘线程立即执行。 */
    void wakeUpFlush();

    /** 唤醒提交线程立即执行。 */
    void wakeUpCommit();

    /** 同步处理磁盘刷盘并更新 PutMessageResult。 */
    void handleDiskFlush(AppendMessageResult result, PutMessageResult putMessageResult, MessageExt messageExt);

    /** 异步处理磁盘刷盘，返回 Future 状态。 */
    CompletableFuture<PutMessageStatus> handleDiskFlush(AppendMessageResult result, MessageExt messageExt);
}
