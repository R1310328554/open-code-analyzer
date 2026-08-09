/*
 * Copyright (C) 2017 Brett Wooldridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zaxxer.hikari.metrics;

/**
 * 连接池指标追踪器接口，由各类指标后端（Dropwizard、Micrometer、Prometheus 等）实现。
 *
 * @author Brett Wooldridge
 */
public interface IMetricsTracker extends AutoCloseable
{
   /** 记录新建物理连接所耗毫秒数。 */
   default void recordConnectionCreatedMillis(long connectionCreatedMillis) {}

   /** 记录从池中获取连接所耗纳秒数。 */
   default void recordConnectionAcquiredNanos(final long elapsedAcquiredNanos) {}

   /** 记录连接借出使用所耗毫秒数。 */
   default void recordConnectionUsageMillis(final long elapsedBorrowedMillis) {}

   /** 记录一次连接获取超时事件。 */
   default void recordConnectionTimeout() {}

   /** 关闭并注销已注册的指标。 */
   @Override
   default void close() {}
}
