/*
 * Copyright 2024 The Netty Project
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
package io.netty.channel.uring;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.StringJoiner;

/**
 * Completion queue implementation for io_uring.
 * <p>io_uring 完成队列（CQ）实现：通过 VarHandle 读写内核共享的 head/tail，逐条消费 CQE。</p>
 * <p>支持 CQE32/混合模式下的扩展 CQE 数据切片。</p>
 */
final class CompletionQueue {
    private static final VarHandle INT_HANDLE =
            MethodHandles.byteBufferViewVarHandle(int[].class, ByteOrder.nativeOrder());

    // 访问 CQE 各字段的偏移（参见 liburing io_uring.h）
    private static final int CQE_USER_DATA_FIELD = 0;
    private static final int CQE_RES_FIELD = 8;
    private static final int CQE_FLAGS_FIELD = 12;

    // 与内核共享的无符号整数指针，通过 VarHandle 读写
    private final ByteBuffer khead;
    private final ByteBuffer ktail;
    private final ByteBuffer kflags;
    private final ByteBuffer completionQueueArray;
    private final ByteBuffer[] extraCqeData;

    final int ringSize;
    final long ringAddress;
    final int ringFd;
    final int ringEntries;
    final int ringCapacity;
    private final int cqeLength;

    private final int ringMask;
    private int ringHead;
    private boolean closed;

    CompletionQueue(ByteBuffer kHead, ByteBuffer kTail, int ringMask, int ringEntries, ByteBuffer kflags,
                    ByteBuffer completionQueueArray, int ringSize, long ringAddress,
                    int ringFd, int ringCapacity, int cqeLength, boolean extraCqeDataNeeded) {
        this.khead = kHead;
        this.ktail = kTail;
        this.completionQueueArray = completionQueueArray;
        this.ringSize = ringSize;
        this.ringAddress = ringAddress;
        this.ringFd = ringFd;
        this.ringCapacity = ringCapacity;
        this.cqeLength = cqeLength;
        this.ringEntries = ringEntries;
        this.kflags = kflags;
        this.ringMask = ringMask;
        ringHead = (int) INT_HANDLE.getVolatile(kHead, 0);

        if (extraCqeDataNeeded) {
            // 预先切片以降低 GC 压力并限制用户无法逃逸内存范围；支持 CQE32/混合模式
            this.extraCqeData = new ByteBuffer[ringEntries];
            for (int i = 0; i < ringEntries; i++) {
                int position = i * cqeLength;
                completionQueueArray.position(position).limit(position + Native.CQE_SIZE);
                extraCqeData[i] = completionQueueArray.slice();
                completionQueueArray.clear();
            }
        } else {
            this.extraCqeData = null;
        }
    }

    void close() {
        closed = true;
    }

    int flags() {
        if (closed) {
            return 0;
        }
        // 读取 flags 仅需 memory_order_relaxed
        return (int) INT_HANDLE.getOpaque(kflags, 0);
    }

    /**
     * Returns {@code true} if any completion event is ready to be processed by
     * <p>是否有可处理的完成事件。</p>
     * {@link #process(CompletionCallback)}, {@code false} otherwise.
     */
    boolean hasCompletions() {
        return !closed && ringHead != (int) INT_HANDLE.getVolatile(ktail, 0);
    }

    int count() {
        if (closed) {
            return 0;
        }
        return (int) INT_HANDLE.getVolatile(ktail, 0) - ringHead;
    }

    /**
     * Process the completion events in the {@link CompletionQueue} and return the number of processed
     * <p>处理完成队列中的事件并返回已处理数量。</p>
     * events.
     */
    // 返回值打包：高 32 位为总完成数，低 32 位为真实 I/O 完成数
    long process(CompletionCallback callback) {
        if (closed) {
            return 0;
        }
        int tail = (int) INT_HANDLE.getVolatile(ktail, 0);
        try {
            int total = 0;
            int realIo = 0;
            while (ringHead != tail) {
                int cqeIdx = cqeIdx(ringHead, ringMask);
                int cqePosition = cqeIdx * cqeLength;

                long udata = completionQueueArray.getLong(cqePosition + CQE_USER_DATA_FIELD);
                int res = completionQueueArray.getInt(cqePosition + CQE_RES_FIELD);
                int flags = completionQueueArray.getInt(cqePosition + CQE_FLAGS_FIELD);

                ringHead++;
                final ByteBuffer extraCqeData;
                if ((flags & Native.IORING_CQE_F_32) != 0) {
                    extraCqeData = extraCqeData(cqeIdx + 1);
                    // 混合模式下 32 字节 CQE，head 需再递增一次
                    ringHead++;
                } else if (cqeLength == Native.CQE32_SIZE) {
                    extraCqeData = extraCqeData(cqeIdx + 1);
                } else {
                    extraCqeData = null;
                }
                // 检查是否应跳过（IORING_CQE_F_SKIP）
                if ((flags & Native.IORING_CQE_F_SKIP) == 0) {
                    total++;
                    if (callback.handle(res, flags, udata, extraCqeData)) {
                        realIo++;
                    }
                }

                if (ringHead == tail) {
                    // 完成可能触发提交导致 tail 变化，需重新读取
                    tail = (int) INT_HANDLE.getVolatile(ktail, 0);
                }
            }
            return ((long) total << 32) | (realIo & 0xFFFFFFFFL);
        } finally {
            // 确保内核在 CQE 读取完毕后才看到新的 head（setRelease）
            INT_HANDLE.setRelease(khead, 0, ringHead);
        }
    }

    private ByteBuffer extraCqeData(int cqeIdx) {
        if (extraCqeData == null) {
            return null;
        }
        ByteBuffer buffer = extraCqeData[cqeIdx];
        buffer.clear();
        return buffer;
    }

    @Override
    public String toString() {
        StringJoiner sb = new StringJoiner(", ", "CompletionQueue [", "]");
        if (closed) {
            sb.add("closed");
        } else {
            int tail = (int) INT_HANDLE.getVolatile(ktail, 0);
            int head = ringHead;
            while (head != tail) {
                int cqePosition = cqeIdx(head++, ringMask) * cqeLength;
                long udata = completionQueueArray.getLong(cqePosition + CQE_USER_DATA_FIELD);
                int res = completionQueueArray.getInt(cqePosition + CQE_RES_FIELD);
                int flags = completionQueueArray.getInt(cqePosition + CQE_FLAGS_FIELD);
                if ((flags & Native.IORING_CQE_F_32) != 0) {
                    // We used mixed mode and this was a 32 byte CQE, let's increment the head once more.
                    head++;
                }
                sb.add("(res=" + res).add(", flags=" + flags).add(", udata=" + udata).add(")");
            }
        }
        return sb.toString();
    }

    private static int cqeIdx(int ringHead, int ringMask) {
        return ringHead & ringMask;
    }
}
