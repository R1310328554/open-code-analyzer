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
package io.netty.channel;

/**
 * An IO event that is dispatched to an {@link IoHandle} as a result of a previous submitted {@link IoOps}.
 *
 * Concrete {@link IoHandle} implementations support different concrete {@link IoEvent} implementations.
 * <p>先前提交的 {@link IoOps} 完成后分派给 {@link IoHandle} 的 I/O 事件标记接口。
 * 具体 {@link IoHandle} 实现对应不同的 {@link IoEvent} 子类型。</p>
 */
public interface IoEvent {
    // Marker interface.
    // 标记接口，无方法定义。
}
