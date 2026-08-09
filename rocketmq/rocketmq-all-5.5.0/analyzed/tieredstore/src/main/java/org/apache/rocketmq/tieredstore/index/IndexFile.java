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

import java.nio.ByteBuffer;

/**
 * 索引文件接口：扩展 {@link IndexService}，提供时间范围、状态与 compaction 能力。
 */
public interface IndexFile extends IndexService {

    /** 索引文件生命周期状态。 */
    /** 索引文件生命周期状态枚举。 */
    enum IndexStatusEnum {
        /** 已关闭 */ SHUTDOWN, /** 未封存 */ UNSEALED, /** 已封存 */ SEALED, /** 已上传 */ UPLOAD
    }

    /** 索引文件起始时间戳。 */
    long getTimestamp();

    /** 索引文件结束时间戳。 */
    long getEndTimestamp();

    /** 当前文件状态。 */
    IndexStatusEnum getFileStatus();

    /** 执行索引 compaction 并返回压缩后缓冲区。 */
    ByteBuffer doCompaction();
}
