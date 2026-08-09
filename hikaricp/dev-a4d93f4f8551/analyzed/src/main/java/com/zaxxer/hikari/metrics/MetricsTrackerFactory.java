/*
 * Copyright (C) 2013,2014 Brett Wooldridge
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

/** 指标追踪器工厂，由各指标后端实现。 */
public interface MetricsTrackerFactory
{
   /**
    * 创建 {@link IMetricsTracker} 实例。
    *
    * @param poolName 连接池名称
    * @param poolStats 用于读取池状态的 {@link PoolStats} 实例
    * @return {@link IMetricsTracker} 实现实例
    */
   IMetricsTracker create(String poolName, PoolStats poolStats);
}
