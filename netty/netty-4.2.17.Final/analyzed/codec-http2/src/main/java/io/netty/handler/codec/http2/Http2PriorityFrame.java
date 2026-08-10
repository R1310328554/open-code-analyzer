/*
 * Copyright 2020 The Netty Project
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
package io.netty.handler.codec.http2;

/**
 * HTTP/2 {@code PRIORITY} 帧对象：调整流的依赖树与权重，影响多路复用下的调度优先级。
 */
public interface Http2PriorityFrame extends Http2StreamFrame {

    /**
     * 父流 id；{@code 0} 表示直接依赖连接（虚拟根）。
     */
    int streamDependency();

    /**
     * 流权重，范围 1–256，与同父流下其他子流相对分配带宽。
     */
    short weight();

    /**
     * {@code true} 表示本流成为父流的独占依赖（其他兄弟流改挂到本流之下）。
     */
    boolean exclusive();

    @Override
    Http2PriorityFrame stream(Http2FrameStream stream);

}
