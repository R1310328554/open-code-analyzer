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
package org.apache.rocketmq.tieredstore.provider;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import org.apache.rocketmq.tieredstore.stream.FileSegmentInputStream;

/**
 * 后端文件段 Provider 接口：定义路径、大小、读写与生命周期操作。
 */
public interface FileSegmentProvider {

    /**
     * 获取后端文件系统中的文件路径。
     *
     * @return 文件实际路径
     */
    String getPath();

    /**
     * 获取文件实际长度。
     * 文件不存在时返回 0，获取失败时返回 -1。
     *
     * @return 文件实际字节大小
     */
    long getSize();

    /**
     * 判断后端文件系统中文件是否存在。
     *
     * @return 存在返回 <code>true</code>，否则 <code>false</code>
     */
    boolean exists();

    /**
     * 在后端文件系统中创建文件。
     */
    void createFile();

    /**
     * 销毁后端文件系统中指定路径的文件。
     */
    void destroyFile();

    /**
     * 从后端文件系统读取数据。
     *
     * @param position 读取起始文件内偏移
     * @param length   读取字节数
     * @return 读取到的数据缓冲
     */
    CompletableFuture<ByteBuffer> read0(long position, int length);

    /**
     * 向后端文件系统写入数据。
     *
     * @param inputStream 待写入的数据流
     * @param position    写入起始文件内偏移（追加模式）
     * @param length      流中数据字节数
     * @param append      是否以追加方式写入
     * @return 写入成功返回 <code>true</code>，否则 <code>false</code>
     */
    CompletableFuture<Boolean> commit0(FileSegmentInputStream inputStream, long position, int length, boolean append);
}
