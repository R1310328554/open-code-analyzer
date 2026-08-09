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
package org.apache.rocketmq.client.consumer.store;

/** 读取消费偏移量的数据来源策略。 */
public enum ReadOffsetType {
    /** 仅从内存读取。 */
    READ_FROM_MEMORY,
    /** 仅从存储（本地文件或 Broker）读取。 */
    READ_FROM_STORE,
    /** 先读内存，未命中再读存储。 */
    MEMORY_FIRST_THEN_STORE;
}
