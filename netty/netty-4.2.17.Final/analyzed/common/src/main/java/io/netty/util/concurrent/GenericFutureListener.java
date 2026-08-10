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

import java.util.EventListener;

/**
 * Listens to the result of a {@link Future}.  The result of the asynchronous operation is notified once this listener
 * is added by calling {@link Future#addListener(GenericFutureListener)}.
 *
 * <p>监听 {@link Future} 完成结果的回调接口。通过 {@link Future#addListener(GenericFutureListener)} 注册后，
 * 当 Future 完成（成功、失败或取消）时由关联的 {@link EventExecutor} 调用 {@link #operationComplete}。</p>
 */
public interface GenericFutureListener<F extends Future<?>> extends EventListener {

    /**
     * Invoked when the operation associated with the {@link Future} has been completed.
     *
     * @param future  the source {@link Future} which called this callback
     *
     * <p>Future 关联的异步操作完成时调用；可在此检查 {@link Future#isSuccess()}、{@link Future#cause()} 等。</p>
     */
    void operationComplete(F future) throws Exception;
}
