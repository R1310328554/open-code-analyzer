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

package io.netty.util;

/**
 * @deprecated please use {@link ResourceLeakTracker} as it may lead to false-positives.
 *
 * <p>已废弃：请改用 {@link ResourceLeakTracker}，本接口可能导致误报。</p>
 */
@Deprecated
public interface ResourceLeak {
    /**
     * Records the caller's current stack trace so that the {@link ResourceLeakDetector} can tell where the leaked
     * resource was accessed lastly. This method is a shortcut to {@link #record(Object) record(null)}.
     *
     * <p>记录当前调用栈，供泄漏检测定位最近访问点；等价于 {@code record(null)}。</p>
     */
    void record();

    /**
     * Records the caller's current stack trace and the specified additional arbitrary information
     * so that the {@link ResourceLeakDetector} can tell where the leaked resource was accessed lastly.
     *
     * <p>记录调用栈并附带 hint 信息。</p>
     */
    void record(Object hint);

    /**
     * Close the leak so that {@link ResourceLeakDetector} does not warn about leaked resources.
     *
     * <p>关闭泄漏跟踪，表示资源已正确释放；首次调用返回 {@code true}。</p>
     *
     * @return {@code true} if called first time, {@code false} if called already
     */
    boolean close();
}
