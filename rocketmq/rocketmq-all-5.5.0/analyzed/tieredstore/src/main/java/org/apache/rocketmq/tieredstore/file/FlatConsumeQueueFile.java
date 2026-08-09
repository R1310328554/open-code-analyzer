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

import org.apache.rocketmq.tieredstore.common.FileSegmentType;
import org.apache.rocketmq.tieredstore.provider.FileSegmentFactory;

/**
 * ConsumeQueue 分层扁平文件，继承 {@link FlatAppendFile} 的段管理与读写能力。
 */
public class FlatConsumeQueueFile extends FlatAppendFile {

    /** 构造指定路径的 ConsumeQueue 文件。 */
    public FlatConsumeQueueFile(FileSegmentFactory fileSegmentFactory, String filePath) {
        super(fileSegmentFactory, FileSegmentType.CONSUME_QUEUE, filePath);
    }
}
