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

import jdk.jfr.DataAmount;
import jdk.jfr.Description;
import jdk.jfr.Label;
import jdk.jfr.Name;

@SuppressWarnings("Since15")
@Label("Buffer Reallocation")
@Name(ReallocateBufferEvent.NAME)
@Description("分配器因扩容重新分配缓冲区时触发；随后通常会有 AllocateBufferEvent")
final class ReallocateBufferEvent extends AbstractBufferEvent {
    static final String NAME = "io.netty.ReallocateBuffer";
    private static final ReallocateBufferEvent INSTANCE = new ReallocateBufferEvent();

    /**
     * 静态检查此 JFR 事件是否已启用。
     */
    public static boolean isEventEnabled() {
        return INSTANCE.isEnabled();
    }

    @DataAmount
    @Description("目标缓冲区容量")
    public int newCapacity;
}
