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

package org.apache.rocketmq.tieredstore.index;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.apache.rocketmq.tieredstore.common.AppendResult;

/**
 * 分层存储索引服务：写入键、按时间范围查询与生命周期管理。
 */
public interface IndexService {

    /** 启动索引后台服务。 */
    void start();

    /**
     * 将消息键写入索引。
     *
     * @param topic Topic 名称
     * @param topicId Topic 内部 ID
     * @param queueId 队列 ID
     * @param keySet 待索引键集合
     * @param offset CommitLog 偏移
     * @param size 消息大小
     * @param timestamp 存储时间戳
     * @return 写入结果
     */
    /** {@inheritDoc} */
    AppendResult putKey(
        String topic, int topicId, int queueId, Set<String> keySet, long offset, int size, long timestamp);

    /**
     * 异步按 Topic、键与时间范围查询索引项。
     *
     * @param topic Topic 名称
     * @param key 查询键
     * @param maxCount 最大返回条数
     * @param beginTime 起始时间
     * @param endTime 结束时间
     * @return 匹配的 IndexItem 列表
     */
    /** {@inheritDoc} */
    CompletableFuture<List<IndexItem>> queryAsync(String topic, String key, int maxCount, long beginTime, long endTime);

    /** 强制上传索引文件（默认空实现）。 */
    default void forceUpload() {
    }

    /** 优雅关闭索引服务。 */
    /** {@inheritDoc} */
    void shutdown();

    /** 强制关闭索引服务。 */
    /** {@inheritDoc} 默认调用 shutdown。 */
    default void forceShutdown() {
        shutdown();
    };

    /** 销毁索引服务并释放全部资源。 */
    /** {@inheritDoc} */
    void destroy();
}
