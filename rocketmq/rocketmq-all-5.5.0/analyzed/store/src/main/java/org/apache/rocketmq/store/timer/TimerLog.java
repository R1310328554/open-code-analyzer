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

import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.store.RunningFlags;
import org.apache.rocketmq.store.logfile.MappedFile;
import org.apache.rocketmq.store.MappedFileQueue;
import org.apache.rocketmq.store.SelectMappedBufferResult;

import java.nio.ByteBuffer;

/**
 * 定时消息日志：基于 MappedFileQueue 追加/读取 Timer 单元。
 */
public class TimerLog {
    private static Logger log = LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);
    /** TimerLog 文件尾空白块魔数。 */
    public final static int BLANK_MAGIC_CODE = 0xBBCCDDEE ^ 1880681586 + 8;
    private final static int MIN_BLANK_LEN = 4 + 8 + 4;
    /** 单条 Timer 单元字节长度。 */
    public final static int UNIT_SIZE = 4  //size
            + 8 //prev pos
            + 4 //magic value
            + 8 //curr write time, for trace
            + 4 //delayed time, for check
            + 8 //offsetPy
            + 4 //sizePy
            + 4 //hash code of real topic
            + 8; //reserved value, just in case of
    /** 消息类 Timer 单元前缀长度。 */
    public final static int UNIT_PRE_SIZE_FOR_MSG = 28;
    /** 指标类 Timer 单元前缀长度。 */
    public final static int UNIT_PRE_SIZE_FOR_METRIC = 40;
    /** TimerLog 映射文件队列。 */
    private final MappedFileQueue mappedFileQueue;

    /** 单个映射文件大小。 */
    private final int fileSize;

        /** 指定路径与文件大小。 */
    public TimerLog(final String storePath, final int fileSize) {
        this(storePath, fileSize, null, false);
    }

        /** 完整构造 TimerLog。 */
    public TimerLog(final String storePath, final int fileSize, RunningFlags runningFlags, boolean writeWithoutMmap) {
        this.fileSize = fileSize;
        this.mappedFileQueue = new MappedFileQueue(storePath, fileSize, null, runningFlags, writeWithoutMmap);
    }

    /** 加载 TimerLog 映射文件队列。 */
    public boolean load() {
        return this.mappedFileQueue.load();
    }

    /** 追加字节数组到 TimerLog。 */
    public long append(byte[] data) {
        return append(data, 0, data.length);
    }

    /** 追加字节数组指定区间到 TimerLog。 */
    public long append(byte[] data, int pos, int len) {
        MappedFile mappedFile = this.mappedFileQueue.getLastMappedFile();
        if (null == mappedFile || mappedFile.isFull()) {
            mappedFile = this.mappedFileQueue.getLastMappedFile(0);
        }
        if (null == mappedFile) {
            log.error("Create mapped file1 error for timer log");
            return -1;
        }
        if (len + MIN_BLANK_LEN > mappedFile.getFileSize() - mappedFile.getWrotePosition()) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(MIN_BLANK_LEN);
            byteBuffer.putInt(mappedFile.getFileSize() - mappedFile.getWrotePosition());
            byteBuffer.putLong(0);
            byteBuffer.putInt(BLANK_MAGIC_CODE);
            if (mappedFile.appendMessage(byteBuffer.array())) {
                //need to set the wrote position
                mappedFile.setWrotePosition(mappedFile.getFileSize());
            } else {
                log.error("Append blank error for timer log");
                return -1;
            }
            mappedFile = this.mappedFileQueue.getLastMappedFile(0);
            if (null == mappedFile) {
                log.error("create mapped file2 error for timer log");
                return -1;
            }
        }
        long currPosition = mappedFile.getFileFromOffset() + mappedFile.getWrotePosition();
        if (!mappedFile.appendMessage(data, pos, len)) {
            log.error("Append error for timer log");
            return -1;
        }
        return currPosition;
    }

    /** 按物理偏移读取 Timer 消息体。 */
    public SelectMappedBufferResult getTimerMessage(long offsetPy) {
        MappedFile mappedFile = mappedFileQueue.findMappedFileByOffset(offsetPy);
        if (null == mappedFile)
            return null;
        return mappedFile.selectMappedBuffer((int) (offsetPy % mappedFile.getFileSize()));
    }

    /** 返回包含 offset 的整文件映射缓冲。 */
    public SelectMappedBufferResult getWholeBuffer(long offsetPy) {
        MappedFile mappedFile = mappedFileQueue.findMappedFileByOffset(offsetPy);
        if (null == mappedFile)
            return null;
        return mappedFile.selectMappedBuffer(0);
    }

    /** 返回底层 MappedFileQueue。 */
    public MappedFileQueue getMappedFileQueue() {
        return mappedFileQueue;
    }

    /** 刷盘并清理全部映射资源。 */
    public void shutdown() {
        try {
            this.mappedFileQueue.flush(0);
        } catch (Throwable e) {
            log.error("flush error when shutdown", e);
        }

        this.mappedFileQueue.cleanResourcesAll();
    }

    // be careful.
    // if the format of timerlog changed, this offset has to be changed too
    // so does the batch writing
    /** 计算当前文件最后一个 Timer 单元的写入偏移（格式变更时需同步修改）。 */
    public int getOffsetForLastUnit() {

        return fileSize - (fileSize - MIN_BLANK_LEN) % UNIT_SIZE - MIN_BLANK_LEN - UNIT_SIZE;
    }

}
