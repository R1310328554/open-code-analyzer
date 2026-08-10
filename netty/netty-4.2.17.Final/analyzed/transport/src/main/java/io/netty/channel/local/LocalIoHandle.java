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
package io.netty.channel.local;

import io.netty.channel.IoHandle;

/**
 * {@link IoHandle} sub-type that is used by the local transport internally.
 *
 * <p>本地传输内部使用的 {@link IoHandle} 子类型，扩展了立即关闭能力。</p>
 */
public interface LocalIoHandle extends IoHandle {
    /** 立即关闭关联的本地 channel，不等待异步流程。 */
    void closeNow();
}
