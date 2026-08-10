/*
 * Copyright 2016 The Netty Project
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
package io.netty.util;

import org.jetbrains.annotations.Nullable;

/**
 * 资源泄漏跟踪器：在 {@link ResourceLeakDetector#track(Object)} 返回的非 null 实例上
 * 记录访问栈并在 {@link #close(Object)} 时标记资源已释放。
 *
 * <p>替代已废弃的 {@link ResourceLeak}，close 时需传入被跟踪对象以校验 identity hash。</p>
 */
public interface ResourceLeakTracker<T>  {

    /**
     * Records the caller's current stack trace so that the {@link ResourceLeakDetector} can tell where the leaked
     * resource was accessed lastly. This method is a shortcut to {@link #record(Object) record(null)}.
     *
     * <p>记录当前访问栈；等价于 {@code record(null)}。</p>
     */
    void record();

    /**
     * Records the caller's current stack trace and the specified additional arbitrary information
     * so that the {@link ResourceLeakDetector} can tell where the leaked resource was accessed lastly.
     *
     * <p>记录访问栈并附带 hint（可为 {@link ResourceLeakHint}）。</p>
     */
    void record(Object hint);

    /**
     * Close the leak so that {@link ResourceLeakTracker} does not warn about leaked resources.
     * After this method is called a leak associated with this ResourceLeakTracker should not be reported.
     *
     * <p>关闭跟踪；{@code trackedObject} 须与 track 时对象一致（identity hash 校验）。</p>
     *
     * @return {@code true} if called first time, {@code false} if called already
     */
    boolean close(T trackedObject);

    /**
     * Get a {@link Throwable} representing the stack trace of the original {@link #close(Object)} call.
     * If this tracker hasn't been cloesd, or close tracking isn't supported or enabled,
     * then this method returns {@code null}.
     *
     * <p>若启用了 close 栈跟踪，返回成功 close 时的栈；否则 {@code null}。</p>
     *
     * @return A throwable with the stack trace of the successful close call, or {@code null}.
     */
    default @Nullable Throwable getCloseStackTraceIfAny() {
        return null;
    }
}
