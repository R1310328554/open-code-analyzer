/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.common.running;

/**
 * Broker 运行时监控指标键：CommitLog/ConsumeQueue 磁盘占用与偏移等。
 */
public enum RunningStats {
    /** CommitLog 最大物理偏移。 */
    commitLogMaxOffset,
    /** CommitLog 最小物理偏移。 */
    commitLogMinOffset,
    /** CommitLog 磁盘使用率。 */
    commitLogDiskRatio,
    /** ConsumeQueue 磁盘使用率。 */
    consumeQueueDiskRatio,
    /** 定时消息队列偏移。 */
    scheduleMessageOffset,
}
