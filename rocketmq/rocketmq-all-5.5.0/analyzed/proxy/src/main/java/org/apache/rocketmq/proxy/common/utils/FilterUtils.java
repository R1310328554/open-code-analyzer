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
package org.apache.rocketmq.proxy.common.utils;

import java.util.Set;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;

/**
 * 订阅过滤工具：判断消息 Tag 是否匹配消费者订阅表达式。
 */
public class FilterUtils {
    /**
     * 判断消息 Tag 是否匹配消费者组的 {@link SubscriptionData}。
     *
     * @param tagsSet {@link SubscriptionData} 中的 tag 集合；空集合表示订阅全部（*）
     * @param tags 消息 Tag；为 null 表示消息未携带 Tag
     */
    public static boolean isTagMatched(Set<String> tagsSet, String tags) {
        // 空 tag 集合表示 SUB_ALL，直接匹配
        if (tagsSet.isEmpty()) {
            return true;
        }

        // 消息无 Tag 时无法匹配具体 tag 过滤
        if (tags == null) {
            return false;
        }

        return tagsSet.contains(tags);
    }
}
