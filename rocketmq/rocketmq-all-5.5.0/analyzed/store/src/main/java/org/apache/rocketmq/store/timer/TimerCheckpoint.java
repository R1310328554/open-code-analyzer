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
package org.apache.rocketmq.store.timer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.remoting.protocol.DataVersion;
import org.apache.rocketmq.store.logfile.DefaultMappedFile;

/**
 * 定时消息检查点：mmap 持久化读取进度、TimerLog 刷盘位与队列偏移。
 */
public class TimerCheckpoint {
    /** 存储模块日志。 */
    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);
    private final RandomAccessFile randomAccessFile;
    private final FileChannel fileChannel;
    private final MappedByteBuffer mappedByteBuffer;
    /** 最近一次扫描时间轮的时间（从节点时从主节点同步）。 */
    private volatile long lastReadTimeMs = 0;
    /** TimerLog 已刷盘物理位置。 */
    private volatile long lastTimerLogFlushPos = 0;
    /** TimerQueue 消费偏移。 */
    private volatile long lastTimerQueueOffset = 0;
    /** 主节点 TimerQueue 偏移（从节点读取）。 */
    private volatile long masterTimerQueueOffset = 0;
    /** 检查点数据版本。 */
    private final DataVersion dataVersion = new DataVersion();

        /** 空构造（decode 用）。 */
    public TimerCheckpoint() {
        this.randomAccessFile = null;
        this.fileChannel = null;
        this.mappedByteBuffer = null;
    }

        /** 打开或创建 mmap 检查点文件。 */
    public TimerCheckpoint(final String scpPath) throws IOException {
        File file = new File(scpPath);
        UtilAll.ensureDirOK(file.getParent());
        boolean fileExists = file.exists();

        this.randomAccessFile = new RandomAccessFile(file, "rw");
        this.fileChannel = this.randomAccessFile.getChannel();
        this.mappedByteBuffer = fileChannel.map(MapMode.READ_WRITE, 0, DefaultMappedFile.OS_PAGE_SIZE);

        if (fileExists) {
            log.info("timer checkpoint file exists, " + scpPath);
            this.lastReadTimeMs = this.mappedByteBuffer.getLong(0);
            this.lastTimerLogFlushPos = this.mappedByteBuffer.getLong(8);
            this.lastTimerQueueOffset = this.mappedByteBuffer.getLong(16);
            this.masterTimerQueueOffset = this.mappedByteBuffer.getLong(24);
            // new add to record dataVersion
            if (this.mappedByteBuffer.hasRemaining()) {
                dataVersion.setStateVersion(this.mappedByteBuffer.getLong(32));
                dataVersion.setTimestamp(this.mappedByteBuffer.getLong(40));
                dataVersion.setCounter(new AtomicLong(this.mappedByteBuffer.getLong(48)));
            }

            log.info("timer checkpoint file lastReadTimeMs " + this.lastReadTimeMs + ", "
                + UtilAll.timeMillisToHumanString(this.lastReadTimeMs));
            log.info("timer checkpoint file lastTimerLogFlushPos " + this.lastTimerLogFlushPos);
            log.info("timer checkpoint file lastTimerQueueOffset " + this.lastTimerQueueOffset);
            log.info("timer checkpoint file masterTimerQueueOffset " + this.masterTimerQueueOffset);
            log.info("timer checkpoint file data version state version " + this.dataVersion.getStateVersion());
            log.info("timer checkpoint file data version timestamp " + this.dataVersion.getTimestamp());
            log.info("timer checkpoint file data version counter " + this.dataVersion.getCounter());
        } else {
            log.info("timer checkpoint file not exists, " + scpPath);
        }
    }

    /** 刷盘并释放 mmap/文件句柄。 */
    public void shutdown() {

        try {
            this.flush();
        } catch (Throwable e) {
            log.error("Shutdown error in timer check point", e);
        }

        if (null != this.mappedByteBuffer) {
            // unmap mappedByteBuffer
            UtilAll.cleanBuffer(this.mappedByteBuffer);
        }

        if (null != this.fileChannel) {
            try {
                this.fileChannel.close();
            } catch (Throwable e) {
                log.error("Shutdown error in timer check point", e);
            }
        }
    }

    /** 将检查点字段写入 mmap 并 force。 */
    public void flush() {
        if (null == this.mappedByteBuffer) {
            return;
        }
        this.mappedByteBuffer.putLong(0, this.lastReadTimeMs);
        this.mappedByteBuffer.putLong(8, this.lastTimerLogFlushPos);
        this.mappedByteBuffer.putLong(16, this.lastTimerQueueOffset);
        this.mappedByteBuffer.putLong(24, this.masterTimerQueueOffset);
        // new add to record dataVersion
        this.mappedByteBuffer.putLong(32, this.dataVersion.getStateVersion());
        this.mappedByteBuffer.putLong(40, this.dataVersion.getTimestamp());
        this.mappedByteBuffer.putLong(48, this.dataVersion.getCounter().get());
        this.mappedByteBuffer.force();
    }

    /** 返回最近读取时间轮时间。 */
    public long getLastReadTimeMs() {
        return lastReadTimeMs;
    }

    /** 序列化检查点为 ByteBuffer。 */
    public static ByteBuffer encode(TimerCheckpoint another) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(56);
        byteBuffer.putLong(another.getLastReadTimeMs());
        byteBuffer.putLong(another.getLastTimerLogFlushPos());
        byteBuffer.putLong(another.getLastTimerQueueOffset());
        byteBuffer.putLong(another.getMasterTimerQueueOffset());
        // new add to record dataVersion
        byteBuffer.putLong(another.getDataVersion().getStateVersion());
        byteBuffer.putLong(another.getDataVersion().getTimestamp());
        byteBuffer.putLong(another.getDataVersion().getCounter().get());
        byteBuffer.flip();
        return byteBuffer;
    }

    /** 从 ByteBuffer 反序列化检查点。 */
    public static TimerCheckpoint decode(ByteBuffer byteBuffer) {
        TimerCheckpoint tmp = new TimerCheckpoint();
        tmp.setLastReadTimeMs(byteBuffer.getLong());
        tmp.setLastTimerLogFlushPos(byteBuffer.getLong());
        tmp.setLastTimerQueueOffset(byteBuffer.getLong());
        tmp.setMasterTimerQueueOffset(byteBuffer.getLong());
        // new add to record dataVersion
        if (byteBuffer.hasRemaining()) {
            tmp.getDataVersion().setStateVersion(byteBuffer.getLong());
            tmp.getDataVersion().setTimestamp(byteBuffer.getLong());
            tmp.getDataVersion().setCounter(new AtomicLong(byteBuffer.getLong()));
        }
        return tmp;
    }

    /** 设置最近读取时间轮时间。 */
    public void setLastReadTimeMs(long lastReadTimeMs) {
        this.lastReadTimeMs = lastReadTimeMs;
    }

    /** 返回 TimerLog 刷盘位置。 */
    public long getLastTimerLogFlushPos() {
        return lastTimerLogFlushPos;
    }

    /** 设置 TimerLog 刷盘位置。 */
    public void setLastTimerLogFlushPos(long lastTimerLogFlushPos) {
        this.lastTimerLogFlushPos = lastTimerLogFlushPos;
    }

    /** 返回 TimerQueue 偏移。 */
    public long getLastTimerQueueOffset() {
        return lastTimerQueueOffset;
    }

    /** 设置 TimerQueue 偏移。 */
    public void setLastTimerQueueOffset(long lastTimerQueueOffset) {
        this.lastTimerQueueOffset = lastTimerQueueOffset;
    }

    /** 返回主节点 TimerQueue 偏移。 */
    public long getMasterTimerQueueOffset() {
        return masterTimerQueueOffset;
    }

    /** 设置主节点 TimerQueue 偏移。 */
    public void setMasterTimerQueueOffset(final long masterTimerQueueOffset) {
        this.masterTimerQueueOffset = masterTimerQueueOffset;
    }

    /** 递增数据版本号。 */
    public void updateDataVersion(long stateVersion) {
        dataVersion.nextVersion(stateVersion);
    }

    /** 返回数据版本对象。 */
    public DataVersion getDataVersion() {
        return dataVersion;
    }
}
