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
package org.apache.rocketmq.common.utils;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.hash.Hashing;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;

import static org.apache.rocketmq.common.message.MessageDecoder.NAME_VALUE_SEPARATOR;
import static org.apache.rocketmq.common.message.MessageDecoder.PROPERTY_SEPARATOR;

/**
 * 消息辅助工具：分片键哈希选队列、属性串裁剪等。
 */
public class MessageUtils {

    /** 对分片键做 Murmur3 哈希，映射到 [0, indexSize) 的下标。 */
    public static int getShardingKeyIndex(String shardingKey, int indexSize) {
        return Math.abs(Hashing.murmur3_32().hashBytes(shardingKey.getBytes(StandardCharsets.UTF_8)).asInt() % indexSize);
    }

    /** 从消息属性读取分片键（缺省为空串）并计算队列下标。 */
    public static int getShardingKeyIndexByMsg(MessageExt msg, int indexSize) {
        String shardingKey = msg.getProperty(MessageConst.PROPERTY_SHARDING_KEY);
        if (shardingKey == null) {
            shardingKey = "";
        }

        return getShardingKeyIndex(shardingKey, indexSize);
    }

    /** 批量消息各自分片键对应的队列下标集合（去重）。 */
    public static Set<Integer> getShardingKeyIndexes(Collection<MessageExt> msgs, int indexSize) {
        Set<Integer> indexSet = new HashSet<>(indexSize);
        for (MessageExt msg : msgs) {
            indexSet.add(getShardingKeyIndexByMsg(msg, indexSize));
        }
        return indexSet;
    }

    /**
     * 从 RocketMQ 属性串中删除指定 name 的键值对（格式 name=value，多属性以分隔符连接）。
     *
     * @param propertiesString 原始属性串，可为 null
     * @param name             要删除的属性名
     */
    public static String deleteProperty(String propertiesString, String name) {
        if (propertiesString != null) {
            int idx0 = 0;
            int idx1;
            int idx2;
            idx1 = propertiesString.indexOf(name, idx0);
            if (idx1 != -1) {
                // 可能需要裁剪掉匹配到的属性片段
                StringBuilder stringBuilder = new StringBuilder(propertiesString.length());
                while (true) {
                    int startIdx = idx0;
                    while (true) {
                        idx1 = propertiesString.indexOf(name, startIdx);
                        if (idx1 == -1) {
                            break;
                        }
                        startIdx = idx1 + name.length();
                        if (idx1 == 0 || propertiesString.charAt(idx1 - 1) == PROPERTY_SEPARATOR) {
                            if (propertiesString.length() > idx1 + name.length()
                                && propertiesString.charAt(idx1 + name.length()) == NAME_VALUE_SEPARATOR) {
                                break;
                            }
                        }
                    }
                    if (idx1 == -1) {
                        // 无需再跳过，追加剩余全部字符
                        stringBuilder.append(propertiesString, idx0, propertiesString.length());
                        break;
                    }
                    // 保留 idx0 到匹配起点之间的字符
                    stringBuilder.append(propertiesString, idx0, idx1);
                    // 将 idx2 移到被裁剪属性值的末尾
                    idx2 = propertiesString.indexOf(PROPERTY_SEPARATOR, idx1 + name.length() + 1);
                    // 该属性及其后直到下一分隔符的内容均被裁剪
                    if (idx2 == -1) {
                        break;
                    }
                    idx0 = idx2 + 1;
                }
                return stringBuilder.toString();
            }
        }
        return propertiesString;
    }
}
