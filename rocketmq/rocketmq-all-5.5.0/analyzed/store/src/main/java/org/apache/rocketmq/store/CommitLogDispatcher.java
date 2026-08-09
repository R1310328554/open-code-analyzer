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
 * CommitLog 消息分发器：将已持久化消息派发到 ConsumeQueue、索引等结构。
 */
public interface CommitLogDispatcher {

    /**
     * 根据 DispatchRequest 构建 ConsumeQueue、索引与过滤数据
     * @param request 分发请求
     * @throws RocksDBException 仅 RocksDB 模式下可能抛出
     */
    void dispatch(final DispatchRequest request) throws RocksDBException;
}
