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

import com.google.common.base.Preconditions;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.store.logfile.MappedFile;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.ByteBuffer;

import static java.lang.String.format;

/**
 * 存储层工具类：物理内存探测、MappedFile 追加及消息解码等辅助方法。
 */
public class StoreUtil {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);

    /** JVM 可见的物理内存总量（字节）。 */
    public static final long TOTAL_PHYSICAL_MEMORY_SIZE = getTotalPhysicalMemorySize();

    @SuppressWarnings("restriction")
    /** 通过 OperatingSystemMXBean 获取物理内存，不可用时默认 24GB。 */
    public static long getTotalPhysicalMemorySize() {
        long physicalTotal = 1024 * 1024 * 1024 * 24L;
        OperatingSystemMXBean osmxb = ManagementFactory.getOperatingSystemMXBean();
        if (osmxb instanceof com.sun.management.OperatingSystemMXBean) {
            physicalTotal = ((com.sun.management.OperatingSystemMXBean) osmxb).getTotalPhysicalMemorySize();
        }

        return physicalTotal;
    }

    /** 向 MappedFile 追加数据，失败时抛出 RuntimeException。 */
    public static void fileAppend(MappedFile file, ByteBuffer data) {
        boolean success = file.appendMessage(data);
        if (!success) {
            throw new RuntimeException(format("fileAppend failed for file: %s and data remaining: %d", file, data.remaining()));
        }
    }

    /** 以末文件起始偏移为当前位置获取文件队列快照。 */
    public static FileQueueSnapshot getFileQueueSnapshot(MappedFileQueue mappedFileQueue) {
        return getFileQueueSnapshot(mappedFileQueue, mappedFileQueue.getLastMappedFile().getFileFromOffset());
    }

    /** 按指定 currentFile 偏移计算首尾文件索引与落后条数。 */
    public static FileQueueSnapshot getFileQueueSnapshot(MappedFileQueue mappedFileQueue, final long currentFile) {
        try {
            Preconditions.checkNotNull(mappedFileQueue, "file queue shouldn't be null");
            MappedFile firstFile = mappedFileQueue.getFirstMappedFile();
            MappedFile lastFile = mappedFileQueue.getLastMappedFile();
            int mappedFileSize = mappedFileQueue.getMappedFileSize();
            if (firstFile == null || lastFile == null) {
                return new FileQueueSnapshot(firstFile, -1, lastFile, -1, currentFile, -1, 0, false);
            }

            long firstFileIndex = 0;
            long lastFileIndex = (lastFile.getFileFromOffset() - firstFile.getFileFromOffset()) / mappedFileSize;
            long currentFileIndex = (currentFile - firstFile.getFileFromOffset()) / mappedFileSize;
            long behind = (lastFile.getFileFromOffset() - currentFile) / mappedFileSize;
            boolean exist = firstFile.getFileFromOffset() <= currentFile && currentFile <= lastFile.getFileFromOffset();
            return new FileQueueSnapshot(firstFile, firstFileIndex, lastFile, lastFileIndex, currentFile, currentFileIndex, behind, exist);
        } catch (Exception e) {
            log.error("[BUG] 获取文件队列快照失败. fileQueue: {}, currentFile: {}", mappedFileQueue, currentFile, e);
        }
        return new FileQueueSnapshot();
    }

    /** 按物理偏移与大小从 MessageStore 读取并解码单条消息。 */
    public static MessageExt getMessage(long offsetPy, int sizePy, MessageStore messageStore, ByteBuffer byteBuffer) {
        try {
            if (offsetPy < 0L || sizePy <= 0 || null == messageStore || null == byteBuffer) {
                return null;
            }
            byteBuffer.position(0);
            byteBuffer.limit(sizePy);
            if (!messageStore.getData(offsetPy, sizePy, byteBuffer)) {
                return null;
            }
            byteBuffer.flip();
            return MessageDecoder.decode(byteBuffer, true, false, false);
        } catch (Exception e) {
            log.error("getMessage error, offsetPy: {}, sizePy: {}, error: {}", offsetPy, sizePy, e.getMessage());
        }
        return null;
    }

}
