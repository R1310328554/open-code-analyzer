/*
 * Copyright 2013 The Netty Project
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

import io.netty.util.concurrent.GenericProgressiveFutureListener;

import java.util.EventListener;

/**
 * An {@link EventListener} listener which will be called once the sending task associated with future is
 * being transferred.
 * <p>{@link ChannelProgressiveFuture} 的渐进式进度监听器：在关联发送任务传输过程中
 * 周期性回调 {@link GenericProgressiveFutureListener#operationProgressed} 报告进度。</p>
 */
public interface ChannelProgressiveFutureListener extends GenericProgressiveFutureListener<ChannelProgressiveFuture> {
    // 仅为类型别名，便于语义化注册渐进式通道 future 监听器
}
