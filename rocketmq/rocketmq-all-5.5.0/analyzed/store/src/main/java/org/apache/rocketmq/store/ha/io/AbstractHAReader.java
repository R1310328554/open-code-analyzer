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

package org.apache.rocketmq.store.ha.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * HA 读操作抽象基类：循环读取 Socket 数据并通过钩子回调。
 */
public abstract class AbstractHAReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);
    /** 已注册的读钩子列表。 */
    protected final List<HAReadHook> readHookList = new ArrayList<>();

    /** 从 Socket 读取数据直至缓冲区满或连续三次零字节。 */
    public boolean read(SocketChannel socketChannel, ByteBuffer byteBufferRead) {
        int readSizeZeroTimes = 0;
        while (byteBufferRead.hasRemaining()) {
            try {
                int readSize = socketChannel.read(byteBufferRead);
                for (HAReadHook readHook : readHookList) {
                    readHook.afterRead(readSize);
                }
                if (readSize > 0) {
                    readSizeZeroTimes = 0;
                    boolean result = processReadResult(byteBufferRead);
                    if (!result) {
                        LOGGER.error("Process read result failed");
                        return false;
                    }
                } else if (readSize == 0) {
                    if (++readSizeZeroTimes >= 3) {
                        break;
                    }
                } else {
                    LOGGER.info("Read socket < 0");
                    return false;
                }
            } catch (IOException e) {
                LOGGER.info("Read socket exception", e);
                return false;
            }
        }

        return true;
    }

    /** 注册读完成后的回调钩子。 */
    public void registerHook(HAReadHook readHook) {
        readHookList.add(readHook);
    }

    /** 清空所有已注册的读钩子。 */
    public void clearHook() {
        readHookList.clear();
    }

    /**
     * 处理读到的字节缓冲区内容。
     *
     * @param byteBufferRead 读取结果缓冲区
     * @return 处理成功返回 true，否则 false
     */
    protected abstract boolean processReadResult(ByteBuffer byteBufferRead);
}
