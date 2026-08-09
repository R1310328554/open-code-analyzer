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
@Label("Chunk Free")
@Name(FreeChunkEvent.NAME)
@Description("Triggered when a memory chunk is freed from an allocator")
/**
 * 内存 Chunk 从分配器释放时触发的 JFR 事件。
 * <p>
 * 记录 Chunk 是否来自池化，便于区分池化回收与一次性分配释放。
 */
final class FreeChunkEvent extends AbstractChunkEvent {
    static final String NAME = "io.netty.FreeChunk";
    private static final FreeChunkEvent INSTANCE = new FreeChunkEvent();

    /** 静态检查该 JFR 事件是否已启用 */
    public static boolean isEventEnabled() {
        return INSTANCE.isEnabled();
    }

    /** Chunk 是否来自池化（否则为单 buffer 一次性分配） */
    @Description("Was this chunk pooled, or was it a one-off allocation for a single buffer?")
    public boolean pooled;
}
