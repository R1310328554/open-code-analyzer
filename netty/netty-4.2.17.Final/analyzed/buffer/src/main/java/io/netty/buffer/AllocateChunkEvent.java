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
@Name(AllocateChunkEvent.NAME)
@Label("Chunk Allocation")
@Description("分配器为新内存块分配时触发")
final class AllocateChunkEvent extends AbstractChunkEvent {
    static final String NAME = "io.netty.AllocateChunk";
    private static final AllocateChunkEvent INSTANCE = new AllocateChunkEvent();

    /**
     * 静态检查此 JFR 事件是否已启用。
     */
    public static boolean isEventEnabled() {
        return INSTANCE.isEnabled();
    }

    @Description("该块是否池化，还是为单个缓冲区一次性分配")
    public boolean pooled;
    @Description("该块是否属于线程本地 magazine 或 arena")
    public boolean threadLocal;
}
