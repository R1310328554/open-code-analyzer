/*
 * Copyright 2025 The Netty Project
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
package io.netty.buffer;

import jdk.jfr.Description;
import jdk.jfr.Label;
import jdk.jfr.Name;

@SuppressWarnings("Since15")
@Label("Buffer Allocation")
@Name(AllocateBufferEvent.NAME)
@Description("Triggered when a buffer is allocated (or reallocated) from an allocator")
/**
 * 缓冲区分配（或扩容后重新分配）时触发的 JFR 事件。
 * <p>
 * 记录 Chunk 是否来自池化、是否线程本地等分配路径信息。
 */
final class AllocateBufferEvent extends AbstractBufferEvent {
    static final String NAME = "io.netty.AllocateBuffer";
    private static final AllocateBufferEvent INSTANCE = new AllocateBufferEvent();

    /** 静态检查该 JFR 事件是否已启用 */
    public static boolean isEventEnabled() {
        return INSTANCE.isEnabled();
    }

    /** Chunk 是否来自池化（否则为一次性分配） */
    @Description("Is this chunk pooled, or is it a one-off allocation for this buffer?")
    public boolean chunkPooled;
    /** Chunk 是否属于线程本地的 Magazine/Arena */
    @Description("Is this buffer's chunk part of a thread-local magazine or arena?")
    public boolean chunkThreadLocal;
}
