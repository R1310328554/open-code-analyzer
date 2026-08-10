/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.netty.handler.codec.http2;

/**
 * 远程流量控制使用的字节分配策略：决定在连接级可写字节中，各流应获得多少份额。
 */
public interface StreamByteDistributor {

    /**
     * 流的可写状态快照，供 {@link #updateStreamableBytes(StreamState)} 更新分配器内部队列。
     */
    interface StreamState {
        /**
         * 关联的 HTTP/2 流。
         */
        Http2Stream stream();

        /**
         * 该流待发送的字节数；实际写入量不得超过 {@link #windowSize()}。
         * @return The amount of bytes this stream has pending to send.
         * @see Http2CodecUtil#streamableBytes(StreamState)
         */
        long pendingBytes();

        /**
         * 是否仍有待写的帧（含仅 HEADERS 无 DATA 的情况）。
         */
        boolean hasFrame();

        /**
         * 该流的流量控制窗口大小（字节）；分配字节数不得超过此值。
         * <p>窗口为 0 或负值时分配器应避免无效分配；也为「仅发空帧」等优化提供依据。
         * @return the size of the stream's flow control window.
         * @see Http2CodecUtil#streamableBytes(StreamState)
         */
        int windowSize();
    }

    /**
     * 分配完成后，由分配器回调以执行实际写出。
     */
    interface Writer {
        /**
         * 为指定流写出已分配的字节数。
         * <p>
         * 抛出任何 {@link Throwable} 视为编程错误，将触发 GOAWAY 并关闭连接。
         * @param stream the stream for which to perform the write.
         * @param numBytes the number of bytes to write.
         */
        void write(Http2Stream stream, int numBytes);
    }

    /**
     * 流的可写字节数发生变化时调用；首次调用前假定该流无可写字节。
     */
    void updateStreamableBytes(StreamState state);

    /**
     * 显式更新优先级依赖树（与流状态变更独立触发）。
     * @param childStreamId The stream identifier associated with the child stream.
     * @param parentStreamId The stream identifier associated with the parent stream. May be {@code 0},
     *                       to make {@code childStreamId} and immediate child of the connection.
     * @param weight The weight which is used relative to other child streams for {@code parentStreamId}. This value
     *               must be between 1 and 256 (inclusive).
     * @param exclusive If {@code childStreamId} should be the exclusive dependency of {@code parentStreamId}.
     */
    void updateDependencyTree(int childStreamId, int parentStreamId, short weight, boolean exclusive);

    /**
     * 在不超过 {@code maxBytes} 的前提下，按策略遍历各流并触发 {@link Writer#write}。
     * <p>遍历顺序由具体实现决定；调用后不会自动扣减 streamable bytes，需调用方再调
     * {@link #updateStreamableBytes(StreamState)}。
     *
     * @param maxBytes the maximum number of bytes to write.
     * @return {@code true} if there are still streamable bytes that have not yet been written,
     * otherwise {@code false}.
     * @throws Http2Exception If an internal exception occurs and internal connection state would otherwise be
     * corrupted.
     */
    boolean distribute(int maxBytes, Writer writer) throws Http2Exception;
}
