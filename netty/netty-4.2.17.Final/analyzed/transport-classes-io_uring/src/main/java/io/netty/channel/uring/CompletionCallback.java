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

import java.nio.ByteBuffer;

/**
 * io_uring 完成队列（CQE）事件回调接口。
 * <p>由 {@link CompletionQueue#process(CompletionCallback)} 逐条调用。</p>
 */
interface CompletionCallback {
    /**
     * Called for a completion event that was put into the {@link CompletionQueue}.
     *
     * @param res           the result of the completion event.
     * @param flags         the flags
     * @param udata         the user data that was provided as part of the submission
     * @param extraCqeData  the extra data for the CQE. This will only be non-null of the ring was setup with
     *                      {@code IORING_SETUP_CQE32} or {@code IORING_SETUP_CQE_MIXED} and {@code IORING_CQE_F_32} is
     *                      set in {@code flags}.
     * <p>扩展 CQE 数据；仅当 ring 启用 CQE32/混合模式且 flags 含 IORING_CQE_F_32 时非 null。</p>
     * @return              {@code true} if this completion represents a real I/O event that should be counted,
     *                      {@code false} for internal completions (e.g. eventfd, ring fd).
     * <p>返回 {@code true} 表示真实 I/O 完成应计入统计；{@code false} 为内部完成（如 eventfd、ring fd）。</p>
     */
    boolean handle(int res, int flags, long udata, ByteBuffer extraCqeData);
}
