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
package org.apache.rocketmq.store.config;

import java.io.File;

/**
 * 存储路径配置辅助类：根据根目录拼接消费队列、索引、事务等子路径。
 */
public class StorePathConfigHelper {

    /** 返回消费队列（ConsumeQueue）存储目录。 */
    public static String getStorePathConsumeQueue(final String rootDir) {
        return rootDir + File.separator + "consumequeue";
    }

    /** 返回扩展消费队列目录路径。 */
    public static String getStorePathConsumeQueueExt(final String rootDir) {
        return rootDir + File.separator + "consumequeue_ext";
    }
    /** 返回批量消费队列目录路径。 */
    public static String getStorePathBatchConsumeQueue(final String rootDir) {
        return rootDir + File.separator + "batchconsumequeue";
    }

    /** 返回基于 RocksDB 的消费队列目录路径。 */
    public static String getStorePathRocksDBConsumeQueue(final String rootDir) {
        return rootDir + File.separator + "consumequeue_rocksdb";
    }

    /** 返回消息索引文件目录路径。 */
    public static String getStorePathIndex(final String rootDir) {
        return rootDir + File.separator + "index";
    }

    /** 返回存储检查点（checkpoint）目录路径。 */
    public static String getStoreCheckpoint(final String rootDir) {
        return rootDir + File.separator + "checkpoint";
    }

    /** 返回异常退出标记文件路径。 */
    public static String getAbortFile(final String rootDir) {
        return rootDir + File.separator + "abort";
    }

    /** 返回存储实例锁文件路径。 */
    public static String getLockFile(final String rootDir) {
        return rootDir + File.separator + "lock";
    }

    /** 返回延迟消息偏移量持久化文件路径。 */
    public static String getDelayOffsetStorePath(final String rootDir) {
        return rootDir + File.separator + "config" + File.separator + "delayOffset.json";
    }

    /** 返回事务状态表存储目录路径。 */
    public static String getTranStateTableStorePath(final String rootDir) {
        return rootDir + File.separator + "transaction" + File.separator + "statetable";
    }

    /** 返回事务重做日志（redo log）存储目录路径。 */
    public static String getTranRedoLogStorePath(final String rootDir) {
        return rootDir + File.separator + "transaction" + File.separator + "redolog";
    }

}
