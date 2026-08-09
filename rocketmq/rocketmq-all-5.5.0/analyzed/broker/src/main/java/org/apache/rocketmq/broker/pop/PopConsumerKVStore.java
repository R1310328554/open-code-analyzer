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
package org.apache.rocketmq.broker.pop;

import java.util.List;

/**
 * POP 消费状态 KV 存储抽象：负责 POP 投递记录的持久化、删除与过期扫描。
 * 典型实现为 {@link PopConsumerRocksdbStore}。
 */
public interface PopConsumerKVStore {

    /** 启动存储服务。 */
    boolean start();

    /** 关闭存储服务。 */
    boolean shutdown();

    /**
     * 返回存储目录路径。
     * @return 存储文件路径
     */
    String getFilePath();

    /**
     * 批量写入 POP 消费记录。
     * @param consumerRecordList 待写入的记录列表
     */
    void writeRecords(List<PopConsumerRecord> consumerRecordList);

    /**
     * 批量删除 POP 消费记录。
     * @param consumerRecordList 待删除的记录列表
     */
    void deleteRecords(List<PopConsumerRecord> consumerRecordList);

    /**
     * 扫描可见性超时时间落在 [lowerTime, upperTime) 区间内的过期记录。
     * @param lowerTime 扫描下界（含），毫秒时间戳
     * @param upperTime 扫描上界（不含），毫秒时间戳
     * @param maxCount 最多返回条数
     * @return 过期记录列表；无匹配时返回空列表
     */
    List<PopConsumerRecord> scanExpiredRecords(long lowerTime, long upperTime, int maxCount);
}
