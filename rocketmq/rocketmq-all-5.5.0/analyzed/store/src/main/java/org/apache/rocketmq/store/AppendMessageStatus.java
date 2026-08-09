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

/**
 * 向 CommitLog 写入消息时返回的状态码枚举。
 */
public enum AppendMessageStatus {
    /** 写入成功。 */
    PUT_OK,
    /** 当前文件剩余空间不足。 */
    END_OF_FILE,
    /** 消息体超过允许的最大尺寸。 */
    MESSAGE_SIZE_EXCEEDED,
    /** 消息属性总大小超限。 */
    PROPERTIES_SIZE_EXCEEDED,
    /** 未知错误。 */
    UNKNOWN_ERROR,
    /** RocksDB 存储层错误。 */
    ROCKSDB_ERROR,
}
