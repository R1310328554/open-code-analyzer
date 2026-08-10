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

import java.util.ArrayDeque;
import java.util.Deque;

import static io.netty.handler.codec.http2.Http2CodecUtil.DEFAULT_MIN_ALLOCATION_CHUNK;
import static io.netty.handler.codec.http2.Http2CodecUtil.streamableBytes;
import static io.netty.handler.codec.http2.Http2Error.INTERNAL_ERROR;
import static io.netty.handler.codec.http2.Http2Exception.connectionError;
import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static io.netty.util.internal.ObjectUtil.checkPositive;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * 忽略流优先级的 {@link StreamByteDistributor}：在所有有待写字节的流之间均匀分配配额。
 * <p>通过 {@link #minAllocationChunk(int)} 设置每流最小分配块，减少单次分配过小导致的吞吐损失。
 */
public final class UniformStreamByteDistributor implements StreamByteDistributor {
    private final Http2Connection.PropertyKey stateKey;
    /** 轮询队列：存有当前可分配字节的流。 */
    private final Deque<State> queue = new ArrayDeque<State>(4);

    /**
     * 每流单次至少分配的字节数，用于提升单流 goodput；默认见 {@link Http2CodecUtil#DEFAULT_MIN_ALLOCATION_CHUNK}。
     */
    private int minAllocationChunk = DEFAULT_MIN_ALLOCATION_CHUNK;
    /** 所有活跃流待写字节总数。 */
    private long totalStreamableBytes;

    public UniformStreamByteDistributor(Http2Connection connection) {
        // Add a state for the connection.
        stateKey = connection.newKey();
        Http2Stream connectionStream = connection.connectionStream();
        connectionStream.setProperty(stateKey, new State(connectionStream));

        // Register for notification of new streams.
        connection.addListener(new Http2ConnectionAdapter() {
            @Override
            public void onStreamAdded(Http2Stream stream) {
                stream.setProperty(stateKey, new State(stream));
            }

            @Override
            public void onStreamClosed(Http2Stream stream) {
                state(stream).close();
            }
        });
    }

    /**
     * 设置每流最小分配块（默认 1 KiB），须 &gt; 0。
     *
     * @param minAllocationChunk the minimum number of bytes that will be allocated to each stream.
     * Must be > 0.
     */
    public void minAllocationChunk(int minAllocationChunk) {
        checkPositive(minAllocationChunk, "minAllocationChunk");
        this.minAllocationChunk = minAllocationChunk;
    }

    @Override
    public void updateStreamableBytes(StreamState streamState) {
        state(streamState.stream()).updateStreamableBytes(streamableBytes(streamState),
                                                          streamState.hasFrame(),
                                                          streamState.windowSize());
    }

    @Override
    public void updateDependencyTree(int childStreamId, int parentStreamId, short weight, boolean exclusive) {
        // 均匀分配器不处理优先级依赖树
    }

    @Override
    public boolean distribute(int maxBytes, Writer writer) throws Http2Exception {
        final int size = queue.size();
        if (size == 0) {
            return totalStreamableBytes > 0;
        }

        final int chunkSize = max(minAllocationChunk, maxBytes / size);

        State state = queue.pollFirst();
        do {
            state.enqueued = false;
            if (state.windowNegative) {
                continue;
            }
            if (maxBytes == 0 && state.streamableBytes > 0) {
                // Stop at the first state that can't send. Add this state back to the head of the queue. Note
                // that empty frames at the head of the queue will always be written, assuming the stream window
                // is not negative.
                queue.addFirst(state);
                state.enqueued = true;
                break;
            }

            // Allocate as much data as we can for this stream.
            int chunk = min(chunkSize, min(maxBytes, state.streamableBytes));
            maxBytes -= chunk;

            // Write the allocated bytes and enqueue as necessary.
            state.write(chunk, writer);
        } while ((state = queue.pollFirst()) != null);

        return totalStreamableBytes > 0;
    }

    private State state(Http2Stream stream) {
        return checkNotNull(stream, "stream").getProperty(stateKey);
    }

    /**
     * The remote flow control state for a single stream.
     */
    private final class State {
        final Http2Stream stream;
        int streamableBytes;
        boolean windowNegative;
        boolean enqueued;
        boolean writing;

        State(Http2Stream stream) {
            this.stream = stream;
        }

        void updateStreamableBytes(int newStreamableBytes, boolean hasFrame, int windowSize) {
            assert hasFrame || newStreamableBytes == 0 :
                "hasFrame: " + hasFrame + " newStreamableBytes: " + newStreamableBytes;

            int delta = newStreamableBytes - streamableBytes;
            if (delta != 0) {
                streamableBytes = newStreamableBytes;
                totalStreamableBytes += delta;
            }
            // In addition to only enqueuing state when they have frames we enforce the following restrictions:
            // 1. If the window has gone negative. We never want to queue a state. However we also don't want to
            //    Immediately remove the item if it is already queued because removal from deque is O(n). So
            //    we allow it to stay queued and rely on the distribution loop to remove this state.
            // 2. If the window is zero we only want to queue if we are not writing. If we are writing that means
            //    we gave the state a chance to write zero length frames. We wait until updateStreamableBytes is
            //    called again before this state is allowed to write.
            windowNegative = windowSize < 0;
            if (hasFrame && (windowSize > 0 || windowSize == 0 && !writing)) {
                addToQueue();
            }
        }

        /**
         * Write any allocated bytes for the given stream and updates the streamable bytes,
         * assuming all of the bytes will be written.
         */
        void write(int numBytes, Writer writer) throws Http2Exception {
            writing = true;
            try {
                // Write the allocated bytes.
                writer.write(stream, numBytes);
            } catch (Throwable t) {
                throw connectionError(INTERNAL_ERROR, t, "byte distribution write error");
            } finally {
                writing = false;
            }
        }

        void addToQueue() {
            if (!enqueued) {
                enqueued = true;
                queue.addLast(this);
            }
        }

        void removeFromQueue() {
            if (enqueued) {
                enqueued = false;
                queue.remove(this);
            }
        }

        void close() {
            // Remove this state from the queue.
            removeFromQueue();

            // Clear the streamable bytes.
            updateStreamableBytes(0, false, 0);
        }
    }
}
