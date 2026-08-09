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

package org.apache.rocketmq.tieredstore.core;

/**
 * 消息存储 Topic 过滤器：判定 Topic 是否应被分层存储排除。
 */
public interface MessageStoreFilter {

    /** 若 Topic 应被过滤（不写入分层存储）则返回 true。 */
    boolean filterTopic(String topicName);

    /** 将 Topic 加入黑名单，后续 {@link #filterTopic} 将排除该 Topic。 */
    void addTopicToBlackList(String topicName);
}
