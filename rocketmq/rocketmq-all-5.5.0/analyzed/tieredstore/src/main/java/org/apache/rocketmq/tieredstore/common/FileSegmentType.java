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
package org.apache.rocketmq.tieredstore.common;

import java.util.Arrays;

/**
 * 分层存储文件段类型：CommitLog、ConsumeQueue 或 Index。
 */
public enum FileSegmentType {

    /** CommitLog 文件段。 */
    COMMIT_LOG(0),

    /** ConsumeQueue 文件段。 */
    CONSUME_QUEUE(1),

    /** Index 文件段。 */
    INDEX(2);

    private final int code;

    FileSegmentType(int code) {
        this.code = code;
    }

        /** 返回文件段类型编码。 */
    public int getCode() {
        return code;
    }

        /** 按编码解析文件段类型。 */
    public static FileSegmentType valueOf(int fileType)
        return Arrays.stream(FileSegmentType.values())
            .filter(segmentType -> segmentType.getCode() == fileType)
            .findFirst()
            .orElse(COMMIT_LOG);
    }
}