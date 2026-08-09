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
package org.apache.rocketmq.store.logfile;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import org.apache.rocketmq.common.message.MessageExtBatch;
import org.apache.rocketmq.common.message.MessageExtBrokerInner;
import org.apache.rocketmq.store.AppendMessageCallback;
import org.apache.rocketmq.store.AppendMessageResult;
import org.apache.rocketmq.store.CompactionAppendMsgCallback;
import org.apache.rocketmq.store.PutMessageContext;
import org.apache.rocketmq.store.RunningFlags;
import org.apache.rocketmq.store.SelectMappedBufferResult;
import org.apache.rocketmq.store.TransientStorePool;
import org.apache.rocketmq.store.config.FlushDiskType;

/**
 * 内存映射文件接口：CommitLog/ConsumeQueue 等持久化文件的读写、刷盘与生命周期管理。
 */
public interface MappedFile {
    /**
     * 返回 MappedFile 的文件名。
     *
     * @return 文件名
     */
    String getFileName();

    /**
     * 修改 MappedFile 的文件名。
     *
     * @param fileName 新文件名
     */
    boolean renameTo(String fileName);

    /**
     * 返回 MappedFile 的文件大小。
     *
     * @return 文件大小（字节）
     */
    int getFileSize();

    /**
     * 返回 MappedFile 底层的 FileChannel。
     *
     * @return 文件通道
     */
    FileChannel getFileChannel();

    /**
     * 文件是否已满且无法再追加消息。
     *
     * @return 已满返回 true
     */
    boolean isFull();

    /**
     * 文件是否仍可用（未 shutdown 或 destroy）。
     *
     * @return 可用返回 true
     */
    boolean isAvailable();

    /**
     * 通过回调将单条消息追加到当前 MappedFile。
     *
     * @param message 待追加消息
     * @param messageCallback 执行实际写入的回调
     * @param putMessageContext 写消息上下文
     * @return 追加结果
     */
    AppendMessageResult appendMessage(MessageExtBrokerInner message, AppendMessageCallback messageCallback, PutMessageContext putMessageContext);

    /**
     * 通过回调将批量消息追加到当前 MappedFile。
     *
     * @param message 批量消息
     * @param messageCallback 执行实际写入的回调
     * @param putMessageContext 写消息上下文
     * @return 追加结果
     */
    AppendMessageResult appendMessages(MessageExtBatch message, AppendMessageCallback messageCallback, PutMessageContext putMessageContext);

    AppendMessageResult appendMessage(final ByteBuffer byteBufferMsg, final CompactionAppendMsgCallback cb);

    /**
     * 通过 MappedByteBuffer 追加字节数组原始消息数据。
     *
     * @param data 待追加字节数组
     * @return 成功返回 true
     */
    boolean appendMessage(byte[] data);


    /**
     * 通过 FileChannel 追加字节数组原始消息数据。
     *
     * @param data 待追加字节数组
     * @return 成功返回 true
     */
    boolean appendMessageUsingFileChannel(byte[] data);

    /**
     * 追加 ByteBuffer 原始消息数据。
     *
     * @param data 待追加缓冲区
     * @return 成功返回 true
     */
    boolean appendMessage(ByteBuffer data);

    /**
     * 从字节数组指定偏移处追加一段原始消息数据。
     *
     * @param data 字节数组
     * @param offset 起始偏移
     * @param length 读取长度
     * @return 成功返回 true
     */
    boolean appendMessage(byte[] data, int offset, int length);

    /**
     * 返回当前文件的全局起始偏移（通常由文件名解析）。
     *
     * @return 文件起始偏移
     */
    long getFileFromOffset();

    /**
     * 将缓存数据刷入磁盘。
     *
     * @param flushLeastPages 最少刷盘页数
     * @return 刷盘后的位置
     */
    int flush(int flushLeastPages);

    /**
     * 将二级缓存提交到页缓存或磁盘（TransientStorePool 场景）。
     *
     * @param commitLeastPages 最少提交页数
     * @return 提交后的位置
     */
    int commit(int commitLeastPages);

    /**
     * 从指定位置选取 MappedByteBuffer 子区域。
     *
     * @param pos 起始位置
     * @param size 子区域大小
     * @return 包含选中切片的 SelectMappedBufferResult
     */
    SelectMappedBufferResult selectMappedBuffer(int pos, int size);

    /**
     * 从指定位置选取 MappedByteBuffer 子区域至文件末尾。
     *
     * @param pos 起始位置
     * @return 包含选中切片的 SelectMappedBufferResult
     */
    SelectMappedBufferResult selectMappedBuffer(int pos);

    /**
     * 返回底层 MappedByteBuffer。
     *
     * @return 映射缓冲区
     */
    MappedByteBuffer getMappedByteBuffer();

    /**
     * 返回 MappedByteBuffer 的 slice 视图。
     *
     * @return 缓冲区切片
     */
    ByteBuffer sliceByteBuffer();

    /**
     * 返回最后一条消息的存储时间戳。
     *
     * @return 存储时间戳
     */
    long getStoreTimestamp();

    /**
     * 返回文件最后修改时间戳。
     *
     * @return 最后修改时间
     */
    long getLastModifiedTimestamp();

    /**
     * 从指定偏移读取指定长度数据到 ByteBuffer。
     *
     * @param pos 起始偏移
     * @param size 数据长度
     * @param byteBuffer 目标缓冲区
     * @return 有数据返回 true
     */
    boolean getData(int pos, int size, ByteBuffer byteBuffer);

    /**
     * 销毁文件并从文件系统删除。
     *
     * @param intervalForcibly 强制释放剩余引用的等待毫秒数
     * @return 成功返回 true
     */
    boolean destroy(long intervalForcibly);

    /**
     * 关闭文件并标记为不可用。
     *
     * @param intervalForcibly 强制释放剩余引用的等待毫秒数
     */
    void shutdown(long intervalForcibly);

    /** 引用计数减 1，归零时清理 MappedFile。 */
    void release();

    /**
     * 引用计数加 1。
     *
     * @return 成功返回 true
     */
    boolean hold();

    /**
     * 当前文件是否为某消费队列的首个 MappedFile。
     *
     * @return 是则 true
     */
    boolean isFirstCreateInQueue();

    /**
     * 设置是否为消费队列首个 MappedFile。
     *
     * @param firstCreateInQueue 标志值
     */
    void setFirstCreateInQueue(boolean firstCreateInQueue);

    /**
     * 返回已刷盘位置。
     *
     * @return 刷盘位置
     */
    int getFlushedPosition();

    /**
     * 设置已刷盘位置。
     *
     * @param flushedPosition 刷盘位置
     */
    void setFlushedPosition(int flushedPosition);

    /**
     * 返回已写入位置。
     *
     * @return 写入位置
     */
    int getWrotePosition();

    /**
     * 设置已写入位置。
     *
     * @param wrotePosition 写入位置
     */
    void setWrotePosition(int wrotePosition);

    /**
     * 返回当前最大可读位置。
     *
     * @return 可读位置
     */
    int getReadPosition();

    /**
     * 设置已提交位置。
     *
     * @param committedPosition 提交位置
     */
    void setCommittedPosition(int committedPosition);

    /** 锁定 MappedByteBuffer（mlock）。 */
    void mlock();

    /** 解锁 MappedByteBuffer（munlock）。 */
    void munlock();

    /**
     * 预热 MappedByteBuffer 页。
     * @param type 刷盘类型
     * @param pages 预热页数
     */
    void warmMappedFile(FlushDiskType type, int pages);

    /** 交换内存映射（swapMap）。 */
    boolean swapMap();

    /** 清理已换出的页表映射。 */
    void cleanSwapedMap(boolean force);

    void cleanResources();

    /** 返回最近一次 swapMap 时间戳。 */
    long getRecentSwapMapTime();

    /** 返回自上次 swap 以来的 MappedByteBuffer 访问次数。 */
    long getMappedByteBufferAccessCountSinceLastSwap();

    /**
     * 返回底层 File 对象。
     * @return 文件
     */
    File getFile();

    /** 重命名文件并追加 .delete 后缀。 */
    void renameToDelete();

    /**
     * 将文件移动到父目录。
     * @throws IOException IO 异常
     */
    void moveToParent() throws IOException;

    /**
     * 返回最后一次刷盘时间。
     * @return 刷盘时间
     */
    long getLastFlushTime();

    /**
     * 初始化 MappedFile。
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param transientStorePool 瞬态存储池
     * @throws IOException IO 异常
     */
    void init(String fileName, int fileSize, RunningFlags runningFlags, TransientStorePool transientStorePool) throws IOException;

    Iterator<SelectMappedBufferResult> iterator(int pos);

    /**
     * 检查指定范围数据是否已加载到内存。
     * @param position 起始偏移
     * @param size 数据大小
     * @return 在内存中返回 true
     */
    boolean isLoaded(long position, int size);
}
