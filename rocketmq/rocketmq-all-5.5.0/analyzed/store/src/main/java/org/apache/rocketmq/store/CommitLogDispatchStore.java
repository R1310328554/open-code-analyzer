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

import org.rocksdb.RocksDBException;

/**
 * 需要参与 CommitLog 分发与恢复的存储抽象。
 * 实现类在加载时向 CommitLog 注册，恢复流程可自动遍历所有已注册存储。
 */
public interface CommitLogDispatchStore {

    /**
     * 获取本存储的分发起始物理偏移；大于该偏移的消息需重新分发（仅恢复阶段使用）。
     *
     * @param recoverNormally 上次 Broker 是否正常退出
     * @return 分发 phyOffset；未启用或无有效偏移时返回 null
     * @throws RocksDBException 访问 RocksDB 失败
     */
    Long getDispatchFromPhyOffset(boolean recoverNormally) throws RocksDBException;

    /**
     * 判断是否应从该 CommitLog MappedFile 开始执行 doDispatch。
     *
     * @param phyOffset 该文件首条消息的物理偏移
     * @param storeTimestamp 该文件首条消息的存储时间戳
     * @param recoverNormally 是否为正常恢复
     * @return 是否从该 MappedFile 开始恢复
     * @throws RocksDBException 访问 RocksDB 失败
     */
    boolean isMappedFileMatchedRecover(long phyOffset, long storeTimestamp,
        boolean recoverNormally) throws RocksDBException;
}

