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
package org.apache.rocketmq.store.queue;

import org.apache.rocketmq.store.Swappable;

/**
 * 文件型消费队列生命周期接口：定义加载、恢复、刷盘与销毁等操作。
 */
public interface FileQueueLifeCycle extends Swappable {
    /** 从磁盘加载队列文件，成功返回 true。 */
    boolean load();

    /** 从文件恢复队列索引与偏移。 */
    void recover();

    /** 自检队列文件完整性。 */
    void checkSelf();

    /**
     * 将缓存刷入文件。
     *
     * @param flushLeastPages 至少刷盘的页数
     * @return 是否有数据被刷盘
     */
    boolean flush(int flushLeastPages);

    /** 销毁队列文件并释放资源。 */
    void destroy();

    /** 从 maxCommitLogPos 起截断脏逻辑文件。 */
    void truncateDirtyLogicFiles(long maxCommitLogPos);

    /**
     * 删除 minCommitLogPos 之前的过期文件。
     *
     * @param minCommitLogPos CommitLog 最小物理位置
     * @return 删除的文件数
     */
    int deleteExpiredFile(long minCommitLogPos);

    /**
     * 滚动到下一个队列文件。
     *
     * @param nextBeginOffset 下一文件起始逻辑偏移
     * @return 下一文件的起始偏移
     */
    long rollNextFile(final long nextBeginOffset);

    /** 首个队列文件是否可用。 */
    boolean isFirstFileAvailable();

    /** 首个队列文件是否存在。 */
    boolean isFirstFileExist();

    boolean shutdown();
}
