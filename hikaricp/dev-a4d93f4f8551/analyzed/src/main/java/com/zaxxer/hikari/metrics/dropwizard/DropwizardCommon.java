/*
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

package com.zaxxer.hikari.metrics.dropwizard;

/** Dropwizard/Codahale 指标命名常量（包内共享）。 */
final class DropwizardCommon {
   private DropwizardCommon()
   {
   }

   /** 指标分类名。 */
   static final String METRIC_CATEGORY = "pool";
   /** 获取连接等待时间指标名。 */
   static final String METRIC_NAME_WAIT = "Wait";
   /** 连接使用时长指标名。 */
   static final String METRIC_NAME_USAGE = "Usage";
   /** 连接创建时长指标名。 */
   static final String METRIC_NAME_CONNECT = "ConnectionCreation";
   /** 连接获取超时速率指标名。 */
   static final String METRIC_NAME_TIMEOUT_RATE = "ConnectionTimeoutRate";
   /** 连接总数指标名。 */
   static final String METRIC_NAME_TOTAL_CONNECTIONS = "TotalConnections";
   /** 空闲连接数指标名。 */
   static final String METRIC_NAME_IDLE_CONNECTIONS = "IdleConnections";
   /** 活跃连接数指标名。 */
   static final String METRIC_NAME_ACTIVE_CONNECTIONS = "ActiveConnections";
   /** 等待连接线程数指标名。 */
   static final String METRIC_NAME_PENDING_CONNECTIONS = "PendingConnections";
   /** 最大连接数指标名。 */
   static final String METRIC_NAME_MAX_CONNECTIONS = "MaxConnections";
   /** 最小连接数指标名。 */
   static final String METRIC_NAME_MIN_CONNECTIONS = "MinConnections";
}
