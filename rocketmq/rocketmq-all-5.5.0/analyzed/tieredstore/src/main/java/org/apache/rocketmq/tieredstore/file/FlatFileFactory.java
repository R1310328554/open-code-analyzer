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

package org.apache.rocketmq.tieredstore.file;

import com.google.common.annotations.VisibleForTesting;
import org.apache.rocketmq.tieredstore.MessageStoreConfig;
import org.apache.rocketmq.tieredstore.MessageStoreExecutor;
import org.apache.rocketmq.tieredstore.common.FileSegmentType;
import org.apache.rocketmq.tieredstore.metadata.MetadataStore;
import org.apache.rocketmq.tieredstore.provider.FileSegmentFactory;

/**
 * 分层扁平文件工厂：创建 CommitLog、ConsumeQueue 与 Index 三类 FlatAppendFile。
 */
public class FlatFileFactory {

    /** 元数据存储。 */
    private final MetadataStore metadataStore;
    /** 消息存储配置。 */
    private final MessageStoreConfig storeConfig;
    /** 底层文件段工厂。 */
    private final FileSegmentFactory fileSegmentFactory;

    /** 测试用构造，使用默认 MessageStoreExecutor。 */
    @VisibleForTesting
    public FlatFileFactory(MetadataStore metadataStore, MessageStoreConfig storeConfig) {
        this.metadataStore = metadataStore;
        this.storeConfig = storeConfig;
        this.fileSegmentFactory = new FileSegmentFactory(metadataStore, storeConfig, new MessageStoreExecutor());
    }

    /** 指定 executor 的完整构造。 */
    public FlatFileFactory(MetadataStore metadataStore,
        MessageStoreConfig storeConfig, MessageStoreExecutor executor) {

        this.metadataStore = metadataStore;
        this.storeConfig = storeConfig;
        this.fileSegmentFactory = new FileSegmentFactory(metadataStore, storeConfig, executor);
    }

    /** 返回存储配置。 */
    public MessageStoreConfig getStoreConfig() {
        return storeConfig;
    }

    /** 返回元数据存储。 */
    public MetadataStore getMetadataStore() {
        return metadataStore;
    }

    /** 创建 CommitLog 扁平文件。 */
    public FlatCommitLogFile createFlatFileForCommitLog(String filePath) {
        return new FlatCommitLogFile(this.fileSegmentFactory, filePath);
    }

    /** 创建 ConsumeQueue 扁平文件。 */
    public FlatConsumeQueueFile createFlatFileForConsumeQueue(String filePath) {
        return new FlatConsumeQueueFile(this.fileSegmentFactory, filePath);
    }

    /** 创建 Index 扁平文件。 */
    public FlatAppendFile createFlatFileForIndexFile(String filePath) {
        return new FlatAppendFile(this.fileSegmentFactory, FileSegmentType.INDEX, filePath);
    }
}
