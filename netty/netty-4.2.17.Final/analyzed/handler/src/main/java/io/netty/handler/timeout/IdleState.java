/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.timeout;

import io.netty.channel.Channel;


/**
 * 表示 {@link Channel} 空闲状态的 {@link Enum}。
 */
public enum IdleState {
    /**
     * 读空闲：一段时间内未收到数据。
     */
    READER_IDLE,
    /**
     * 写空闲：一段时间内未发送数据。
     */
    WRITER_IDLE,
    /**
     * 读写均空闲：一段时间内既未收也未发数据。
     */
    ALL_IDLE
}
