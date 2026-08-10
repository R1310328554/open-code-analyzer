/*
 * Copyright 2015 The Netty Project
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
 * Expose details for a {@link Thread}.
 *
 * <p>以不可变快照形式暴露 {@link Thread} 属性，便于跨线程安全查询 EventLoop 线程状态。</p>
 */
public interface ThreadProperties {
    /** 线程状态。 @see Thread#getState() */

    Thread.State state();

    /** 优先级。 @see Thread#getPriority() */

    int priority();

    /** 是否已中断。 @see Thread#isInterrupted() */

    boolean isInterrupted();

    /** 是否守护线程。 @see Thread#isDaemon() */

    boolean isDaemon();

    /** 线程名。 @see Thread#getName() */

    String name();

    /** 线程 id。 @see Thread#getId() */

    long id();

    /** 栈跟踪快照。 @see Thread#getStackTrace() */

    StackTraceElement[] stackTrace();

    /** 是否存活。 @see Thread#isAlive() */

    boolean isAlive();
}
