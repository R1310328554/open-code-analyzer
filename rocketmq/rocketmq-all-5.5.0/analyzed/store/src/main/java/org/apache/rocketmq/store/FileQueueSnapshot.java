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

import org.apache.rocketmq.store.logfile.MappedFile;

/**
 * CommitLog 文件队列快照：记录首尾 MappedFile、当前写入位置及落后条数等元信息。
 */
public class FileQueueSnapshot {
    /** 队列中首个 MappedFile。 */
    private MappedFile firstFile;
    /** 首个文件在队列中的索引。 */
    private long firstFileIndex;
    /** 队列中最后一个 MappedFile。 */
    private MappedFile lastFile;
    /** 末位文件在队列中的索引。 */
    private long lastFileIndex;
    /** 当前正在写入的文件偏移或标识。 */
    private long currentFile;
    /** 当前写入文件在队列中的索引。 */
    private long currentFileIndex;
    /** 相对消费进度落后的消息条数。 */
    private long behindCount;
    /** 快照对应队列是否存在。 */
    private boolean exist;

    /** 无参构造，用于序列化或占位。 */
    public FileQueueSnapshot() {
    }

    /** 构造完整文件队列快照。 */
    public FileQueueSnapshot(MappedFile firstFile, long firstFileIndex, MappedFile lastFile, long lastFileIndex, long currentFile, long currentFileIndex, long behindCount, boolean exist) {
        this.firstFile = firstFile;
        this.firstFileIndex = firstFileIndex;
        this.lastFile = lastFile;
        this.lastFileIndex = lastFileIndex;
        this.currentFile = currentFile;
        this.currentFileIndex = currentFileIndex;
        this.behindCount = behindCount;
        this.exist = exist;
    }

    /** 返回首个 MappedFile。 */
    public MappedFile getFirstFile() {
        return firstFile;
    }

    /** 返回首个文件索引。 */
    public long getFirstFileIndex() {
        return firstFileIndex;
    }

    /** 返回末位 MappedFile。 */
    public MappedFile getLastFile() {
        return lastFile;
    }

    /** 返回末位文件索引。 */
    public long getLastFileIndex() {
        return lastFileIndex;
    }

    /** 返回当前写入文件标识。 */
    public long getCurrentFile() {
        return currentFile;
    }

    /** 返回当前写入文件索引。 */
    public long getCurrentFileIndex() {
        return currentFileIndex;
    }

    /** 返回落后条数。 */
    public long getBehindCount() {
        return behindCount;
    }

    /** 队列是否存在。 */
    public boolean isExist() {
        return exist;
    }

    /** 返回调试字符串。 */
    @Override
    public String toString() {
        return "FileQueueSnapshot{" +
                "firstFile=" + firstFile +
                ", firstFileIndex=" + firstFileIndex +
                ", lastFile=" + lastFile +
                ", lastFileIndex=" + lastFileIndex +
                ", currentFile=" + currentFile +
                ", currentFileIndex=" + currentFileIndex +
                ", behindCount=" + behindCount +
                ", exist=" + exist +
                '}';
    }
}
