/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.naming.consistency.persistent.impl;

import com.alibaba.nacos.consistency.snapshot.Reader;
import com.alibaba.nacos.consistency.snapshot.SnapshotOperation;
import com.alibaba.nacos.consistency.snapshot.Writer;
import com.alibaba.nacos.core.distributed.raft.utils.RaftExecutor;
import com.alibaba.nacos.sys.utils.TimerContext;
import com.alibaba.nacos.naming.misc.Loggers;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;

/**
 * 持久化一致性快照操作的抽象基类。
 *
 * <p>封装 Raft 快照保存与加载时的写锁保护、异步执行与耗时统计，子类只需实现 {@link #writeSnapshot} 与 {@link #readSnapshot}。</p>
 *
 * @author xiweng.yy
 */
public abstract class AbstractSnapshotOperation implements SnapshotOperation {
    
    /** 快照校验和键名。 */
    protected static final String CHECK_SUM_KEY = "checksum";
    
    /** 快照读写时使用的写锁，保证与内存数据变更互斥。 */
    private final ReentrantReadWriteLock.WriteLock writeLock;
    
    /** 从读写锁中提取写锁供快照流程使用。 */
    public AbstractSnapshotOperation(ReentrantReadWriteLock lock) {
        this.writeLock = lock.writeLock();
    }
    
    @Override
    public void onSnapshotSave(Writer writer, BiConsumer<Boolean, Throwable> callFinally) {
        RaftExecutor.doSnapshot(() -> {
            TimerContext.start(getSnapshotSaveTag());
            final Lock lock = writeLock;
            lock.lock();
            try {
                callFinally.accept(writeSnapshot(writer), null);
            } catch (Throwable t) {
                Loggers.RAFT.error("Fail to compress snapshot, path={}, file list={}.",
                    writer.getPath(),
                    writer.listFiles(), t);
                callFinally.accept(false, t);
            } finally {
                lock.unlock();
                TimerContext.end(getSnapshotSaveTag(), Loggers.RAFT);
            }
        });
    }
    
    @Override
    public boolean onSnapshotLoad(Reader reader) {
        TimerContext.start(getSnapshotLoadTag());
        final Lock lock = writeLock;
        lock.lock();
        try {
            return readSnapshot(reader);
        } catch (final Throwable t) {
            Loggers.RAFT
                .error("Fail to load snapshot, path={}, file list={}.", reader.getPath(),
                    reader.listFiles(), t);
            return false;
        } finally {
            lock.unlock();
            TimerContext.end(getSnapshotLoadTag(), Loggers.RAFT);
        }
    }
    
    /**
     * 将内存状态写入快照文件。
     *
     * @param writer 快照写入器
     * @return 写入成功返回 {@code true}，否则 {@code false}
     * @throws Exception 写入过程中的任意异常
     */
    protected abstract boolean writeSnapshot(Writer writer) throws Exception;
    
    /**
     * 从快照文件恢复内存状态。
     *
     * @param reader 快照读取器
     * @return 加载成功返回 {@code true}，否则 {@code false}
     * @throws Exception 读取过程中的任意异常
     */
    protected abstract boolean readSnapshot(Reader reader) throws Exception;
    
    /**
     * 获取快照保存耗时统计标签。
     *
     * @return 快照保存标签
     */
    protected abstract String getSnapshotSaveTag();
    
    /**
     * 获取快照加载耗时统计标签。
     *
     * @return 快照加载标签
     */
    protected abstract String getSnapshotLoadTag();
}
