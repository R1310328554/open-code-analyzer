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
package org.apache.rocketmq.tieredstore.metadata;

import java.util.function.Consumer;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.tieredstore.common.FileSegmentType;
import org.apache.rocketmq.tieredstore.metadata.entity.FileSegmentMetadata;
import org.apache.rocketmq.tieredstore.metadata.entity.QueueMetadata;
import org.apache.rocketmq.tieredstore.metadata.entity.TopicMetadata;

/**
 * 分层元数据存储服务：管理 Topic、Queue、FileSegment 等元信息的 CRUD 与迭代。
 */
public interface MetadataStore {

    /**
     * 获取指定 Topic 的元数据。
     *
     * @param topic Topic 名称
     * @return 元数据，不存在则 null
     */
    /** {@inheritDoc} */
    TopicMetadata getTopic(String topic);

    /**
     * 新增 Topic 元数据。
     *
     * @param topic Topic 名称
     * @param reserveTime 保留时长
     * @return 新建的 TopicMetadata
     */
    /** {@inheritDoc} */
    TopicMetadata addTopic(String topic, long reserveTime);

    /** 更新 Topic 元数据。 */
    void updateTopic(TopicMetadata topicMetadata);

    /** 迭代全部 Topic 元数据。 */
    void iterateTopic(Consumer<TopicMetadata> callback);

    /** 删除 Topic 及其 Queue 元数据。 */
    void deleteTopic(String topic);

    /** 获取指定 MessageQueue 元数据。 */
    QueueMetadata getQueue(MessageQueue mq);

    /** 新增 Queue 元数据。 */
    QueueMetadata addQueue(MessageQueue mq, long baseOffset);

    /** 更新 Queue 元数据。 */
    void updateQueue(QueueMetadata queueMetadata);

    /** 迭代指定 Topic 下全部 Queue。 */
    void iterateQueue(String topic, Consumer<QueueMetadata> callback);

    /** 删除 Queue 元数据。 */
    void deleteQueue(MessageQueue mq);

    /** 获取文件段元数据。 */
    FileSegmentMetadata getFileSegment(String basePath, FileSegmentType fileType, long baseOffset);

    /** 更新或插入文件段元数据。 */
    void updateFileSegment(FileSegmentMetadata fileSegmentMetadata);

    /** 迭代全部类型文件段。 */
    void iterateFileSegment(Consumer<FileSegmentMetadata> callback);

    /** 迭代指定路径与类型的文件段。 */
    void iterateFileSegment(String basePath, FileSegmentType fileType, Consumer<FileSegmentMetadata> callback);

    /** 删除路径下全部文件段元数据。 */
    void deleteFileSegment(String basePath, FileSegmentType fileType);

    /** 删除指定 baseOffset 的文件段元数据。 */
    void deleteFileSegment(String basePath, FileSegmentType fileType, long baseOffset);

    /** 销毁全部元数据。 */
    void destroy();
}
