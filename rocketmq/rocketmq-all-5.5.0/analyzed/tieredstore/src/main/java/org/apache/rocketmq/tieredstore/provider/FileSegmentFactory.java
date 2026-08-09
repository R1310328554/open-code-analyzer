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

package org.apache.rocketmq.tieredstore.provider;

import java.lang.reflect.Constructor;
import org.apache.rocketmq.tieredstore.MessageStoreConfig;
import org.apache.rocketmq.tieredstore.MessageStoreExecutor;
import org.apache.rocketmq.tieredstore.common.FileSegmentType;
import org.apache.rocketmq.tieredstore.metadata.MetadataStore;

/**
 * 文件段工厂：按配置反射创建 {@link FileSegment} 实现实例。
 */
public class FileSegmentFactory {

    /** 元数据存储引用。 */
    private final MetadataStore metadataStore;
    /** 分层存储配置。 */
    private final MessageStoreConfig storeConfig;
    /** 异步 I/O 执行器。 */
    private final MessageStoreExecutor executor;
    /** 反射得到的 FileSegment 构造器。 */
    private final Constructor<? extends FileSegment> fileSegmentConstructor;

    public FileSegmentFactory(MetadataStore metadataStore,
        MessageStoreConfig storeConfig, MessageStoreExecutor executor) {

        try {
            this.storeConfig = storeConfig;
            this.metadataStore = metadataStore;
            this.executor = executor;
            Class<? extends FileSegment> clazz =
                Class.forName(storeConfig.getTieredBackendServiceProvider()).asSubclass(FileSegment.class);
            fileSegmentConstructor = clazz.getConstructor(
                MessageStoreConfig.class, FileSegmentType.class, String.class, Long.TYPE, MessageStoreExecutor.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 返回关联的元数据存储。 */
    public MetadataStore getMetadataStore() {
        return metadataStore;
    }

    /** 返回分层存储配置。 */
    public MessageStoreConfig getStoreConfig() {
        return storeConfig;
    }

    /** 创建指定类型与偏移的文件段。 */
    public FileSegment createSegment(FileSegmentType fileType, String filePath, long baseOffset) {
        try {
            return fileSegmentConstructor.newInstance(this.storeConfig, fileType, filePath, baseOffset, executor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 创建 CommitLog 文件段。 */
    public FileSegment createCommitLogFileSegment(String filePath, long baseOffset) {
        return this.createSegment(FileSegmentType.COMMIT_LOG, filePath, baseOffset);
    }

    /** 创建 ConsumeQueue 文件段。 */
    public FileSegment createConsumeQueueFileSegment(String filePath, long baseOffset) {
        return this.createSegment(FileSegmentType.CONSUME_QUEUE, filePath, baseOffset);
    }

    /** 创建 Index 文件段。 */
    public FileSegment createIndexServiceFileSegment(String filePath, long baseOffset) {
        return this.createSegment(FileSegmentType.INDEX, filePath, baseOffset);
    }
}
