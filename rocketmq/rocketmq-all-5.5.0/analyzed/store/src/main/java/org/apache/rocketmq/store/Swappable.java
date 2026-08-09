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
package org.apache.rocketmq.store;

/**
 * 超大磁盘场景下可交换映射：定期 swap/clean MappedFile 以降低页表占用。
 */
public interface Swappable {
    /** 按保留数量与间隔将冷 MappedFile 换出内存。 */
    void swapMap(int reserveNum, long forceSwapIntervalMs, long normalSwapIntervalMs);
    /** 清理已换出且超过强制间隔的 MappedFile 映射。 */
    void cleanSwappedMap(long forceCleanSwapIntervalMs);
}
