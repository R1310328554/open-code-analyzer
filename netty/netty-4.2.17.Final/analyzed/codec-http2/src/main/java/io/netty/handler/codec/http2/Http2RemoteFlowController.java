/*
 * Copyright 2014 The Netty Project
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

import io.netty.channel.ChannelHandlerContext;

/**
 * 出站流控控制器：管理发往远端的 {@code DATA} 帧，按连接/流窗口与优先级调度写入。
 * <p>实现 {@link Http2FlowController}；payload 先入队，由 {@link #writePendingBytes()} 在窗口允许时刷出。
 */
public interface Http2RemoteFlowController extends Http2FlowController {
    /**
     * 返回应用流控的 {@link ChannelHandlerContext}；仅供 {@link FlowControlled} 内部使用。
     * @return The {@link ChannelHandlerContext} for which to apply flow control on.
     */
    ChannelHandlerContext channelHandlerContext();

    /**
     * 将待发送 payload 加入流控队列；实际写入需调用 {@link #writePendingBytes()}。
     * <p>写入时机与分帧策略由控制器决定，调用方无法保证立即发出。
     *
     * @param stream the subject stream. Must not be the connection stream object.
     * @param payload payload to write subject to flow-control accounting and ordering rules.
     */
    void addFlowControlled(Http2Stream stream, FlowControlled payload);

    /**
     * 判断 {@code stream} 是否仍有排队中的 {@link FlowControlled} 帧。
     * @param stream the stream to check if it has flow controlled frames.
     * @return {@code true} if {@code stream} has any {@link FlowControlled} frames currently queued.
     */
    boolean hasFlowControlled(Http2Stream stream);

    /**
     * 在流控窗口允许范围内写出所有排队数据。
     *
     * @throws Http2Exception throws if a protocol-related error occurred.
     */
    void writePendingBytes() throws Http2Exception;

    /**
     * 设置流控监听器；{@code listener} 可为 {@code null} 以取消监听。
     *
     * @param listener to notify when the a write occurs, can be {@code null}.
     */
    void listener(Listener listener);

    /**
     * 判断 {@code stream} 是否仍有可用发送窗口且底层 channel 可写。
     * <p>同时考虑 HTTP/2 窗口与 Netty channel writability。
     *
     * @param stream The stream to test.
     * @return {@code true} if the {@code stream} has bytes remaining for use in the flow control window and the
     * channel is writable, {@code false} otherwise.
     */
    boolean isWritable(Http2Stream stream);

    /**
     * 底层 channel 可写状态变化时的回调；可能触发排队数据的写出。
     * @throws Http2Exception If any writes occur as a result of this call and encounter errors.
     */
    void channelWritabilityChanged() throws Http2Exception;

    /**
     * 显式更新优先级依赖树，与流状态变迁解耦调用。
     * @param childStreamId The stream identifier associated with the child stream.
     * @param parentStreamId The stream identifier associated with the parent stream. May be {@code 0},
     *                       to make {@code childStreamId} and immediate child of the connection.
     * @param weight The weight which is used relative to other child streams for {@code parentStreamId}. This value
     *               must be between 1 and 256 (inclusive).
     * @param exclusive If {@code childStreamId} should be the exclusive dependency of {@code parentStreamId}.
     */
    void updateDependencyTree(int childStreamId, int parentStreamId, short weight, boolean exclusive);

    /**
     * 可分块写出、参与流控计费的出站 payload 抽象。
     * <p>{@link #write} 至少调用一次且 {@link #size()} 归零后视为完全写出。
     */
    interface FlowControlled {
        /**
         * 计入流控窗口的字节数；{@code HEADERS} 等帧返回 0，{@code DATA} 含 payload 与 padding。
         */
        int size();

        /**
         * 写出完成前发生错误时的回调；控制器对同一对象只会调用本方法或 {@link #writeComplete()} 之一。
         *
         * @param ctx The context to use if any communication needs to occur as a result of the error.
         * This may be {@code null} if an exception occurs when the connection has not been established yet.
         * @param cause of the error.
         */
        void error(ChannelHandlerContext ctx, Throwable cause);

        /**
         * payload 全部成功写出后的回调。
         */
        void writeComplete();

        /**
         * 在不超过 {@code allowedBytes} 的前提下写出部分 payload；{@code allowedBytes} 为 0 时
         * 仍可用于写出 size==0 的帧。控制器可能多次调用直至 {@link #size()} 为 0。
         *
         * @param ctx The context to use for writing.
         * @param allowedBytes an upper bound on the number of bytes the payload can write at this time.
         */
        void write(ChannelHandlerContext ctx, int allowedBytes);

        /**
         * 尝试将 {@code next} 合并进当前 payload，合并后可一次 {@code DATA} 帧发出以减少开销。
         *
         * @return {@code true} if {@code next} was successfully merged and does not need to be enqueued,
         *     {@code false} otherwise.
         */
        boolean merge(ChannelHandlerContext ctx, FlowControlled next);
    }

    /**
     * 监听各流可写状态变化（窗口或 channel writability 改变时触发）。
     */
    interface Listener {
        /**
         * {@link #isWritable(Http2Stream)} 结果变化时的通知；实现不应抛异常。
         * @param stream The stream which writability has changed for.
         */
        void writabilityChanged(Http2Stream stream);
    }
}
