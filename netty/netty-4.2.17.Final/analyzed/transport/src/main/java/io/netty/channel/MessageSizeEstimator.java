/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel;

/**
 * 负责估算消息占用内存的大小。
 * <p>
 * 返回值近似表示该消息在内存中可能占用的字节数，用于流控、
 * {@link Channel#isWritable()} 判断以及待发送字节统计等场景。
 * </p>
 */
public interface MessageSizeEstimator {

    /**
     * 创建新的估算句柄；实际计算由 {@link Handle} 完成。
     */
    Handle newHandle();

    /**
     * 消息大小估算句柄，通常与单个 Channel 或 Pipeline 绑定使用。
     */
    interface Handle {

        /**
         * 计算给定消息的大小。
         *
         * @param msg       待估算的消息对象
         * @return size     字节数，必须 {@code >= 0}
         */
        int size(Object msg);
    }
}
