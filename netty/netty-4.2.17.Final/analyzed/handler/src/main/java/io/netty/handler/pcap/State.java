/*
 * Copyright 2023 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.pcap;

/**
 * The state of the {@link PcapWriteHandler}.
 *
 * <p>{@link PcapWriteHandler} 的生命周期状态，控制是否向 PCAP 流写入数据。</p>
 */
enum State {

    /**
     * The handler is not active.
     *
     * <p>初始状态，尚未开始捕获写入。</p>
     */
    INIT,

    /**
     * The handler is active and actively writing Pcap data.
     *
     * <p>正在写入 PCAP 数据包。</p>
     */
    WRITING,

    /**
     * The handler is paused. No Pcap data will be written.
     *
     * <p>已暂停，暂时不向 PCAP 写入任何数据。</p>
     */
    PAUSED,

    /**
     * The handler is closed.
     *
     * <p>已关闭，后续写入请求将被忽略。</p>
     */
    CLOSED
}
