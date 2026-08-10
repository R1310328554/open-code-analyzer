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

package io.netty.util.concurrent;

/**
 * {@link GenericFutureListener} 的渐进式扩展，在操作完成前可多次收到进度回调。
 *
 * <p>实现此接口的监听器会在 {@link ProgressivePromise#setProgress} 或
 * {@link ProgressivePromise#tryProgress} 更新进度时被调用，适用于大文件传输、
 * 批量处理等需要向调用方报告进度的场景。</p>
 */
public interface GenericProgressiveFutureListener<F extends ProgressiveFuture<?>> extends GenericFutureListener<F> {
    /**
     * Invoked when the operation has progressed.
     *
     * @param progress the progress of the operation so far (cumulative)
     * @param total the number that signifies the end of the operation when {@code progress} reaches at it.
     *              {@code -1} if the end of operation is unknown.
     *
     * <p>操作取得新进度时调用；{@code progress} 为累计值，{@code total} 为预期总量，
     * {@code -1} 表示总量未知。</p>
     */
    void operationProgressed(F future, long progress, long total) throws Exception;
}
