/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *    Copyright 2009 - 2017, Red Hat Inc. and/or its affiliates.
 *
 *    All files in this repository or distribution are licensed under the
 *    Apache License, Version 2.0 (the "License");
 *    you may not use any files in this repository or distribution except
 *    in compliance with the License.
 *
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.redisson.misc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 基于 {@link java.util.concurrent.locks.AbstractQueuedSynchronizer} 的可重复开关线程门闩。
 * <p>
 * 与 {@link java.util.concurrent.CountDownLatch} 不同，可多次 {@link #open()} / {@link #close()}；
 * 打开时等待线程放行，关闭时新到达的线程阻塞。
 *
 * @author Manik Surtani (<a href="mailto:manik@jboss.org">manik@jboss.org</a>)
 * @since 4.0
 */
@SuppressWarnings({"MultipleVariableDeclarations", "AvoidInlineConditionals", "UpperEll"})
public class ReclosableLatch extends AbstractQueuedSynchronizer {

   private static final long serialVersionUID = 1744280161777661090l;

   /** AQS 共享模式下的两种状态：打开 / 关闭。 */
   private static final int OPEN_STATE = 0, CLOSED_STATE = 1;

   /** 默认构造为关闭状态。 */
   public ReclosableLatch() {
      setState(CLOSED_STATE);
   }

   /** 指定初始为打开或关闭。 */
   public ReclosableLatch(boolean defaultOpen) {
      setState(defaultOpen ? OPEN_STATE : CLOSED_STATE);
   }

   /** 打开状态时允许共享获取（返回 1），否则阻塞（返回 -1）。 */
   @Override
   public final int tryAcquireShared(int ignored) {
      // 返回 1 表示放行，-1 表示需要阻塞
      return getState() == OPEN_STATE ? 1 : -1;
   }

   @Override
   public final boolean tryReleaseShared(int state) {
      // used as a mechanism to set the state of the Sync.
      setState(state);
      return true;
   }

   /** 打开门闩并唤醒所有等待线程。 */
   public final void open() {
      // 须通过 releaseShared 触发 AQS 唤醒，不可直接 setState
      releaseShared(OPEN_STATE);
   }

   /** 关闭门闩，后续 acquire 将阻塞。 */
   public final void close() {
      // do not use setState() directly since this won't notify parked threads.
      releaseShared(CLOSED_STATE);
   }

   /** 当前是否处于打开状态。 */
   public boolean isOpened() {
      return getState() == OPEN_STATE;
   }

   /** 阻塞直到门闩打开（可响应中断）。 */
   public final void await() throws InterruptedException {
      acquireSharedInterruptibly(1); // 参数 1 为占位，实际未使用
   }

   public final void awaitUninterruptibly() {
        try {
            await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


   public final boolean await(long time, TimeUnit unit) throws InterruptedException {
      return tryAcquireSharedNanos(1, unit.toNanos(time)); // the 1 is a dummy value that is not used.
   }

   @Override
   public String toString() {
      int s = getState();
      String q = hasQueuedThreads() ? "non" : "";
      return "ReclosableLatch [State = " + s + ", " + q + "empty queue]";
   }
}
