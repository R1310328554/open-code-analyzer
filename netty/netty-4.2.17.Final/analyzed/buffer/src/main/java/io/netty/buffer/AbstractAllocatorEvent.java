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

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Enabled;
import jdk.jfr.Event;
import jdk.jfr.Label;

/**
 * 内存分配器相关 JFR 事件的抽象基类。
 * <p>
 * 默认禁用（{@code @Enabled(false)}），子类在需要时单独启用；
 * 记录触发事件的 {@link AbstractByteBufAllocator} 具体类型。
 */
@Enabled(false)
@Category("Netty")
@SuppressWarnings("Since15")
abstract class AbstractAllocatorEvent extends Event {
    /** 产生该事件的分配器类型 */
    @Label("Allocator type")
    @Description("The type of allocator this event is for")
    public Class<? extends AbstractByteBufAllocator> allocatorType;
}
