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

package org.apache.rocketmq.common.lite;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.Pair;

/**
 * Lite Topic 与 LMQ 队列名之间的编解码工具。
 * 命名模式：{@code %LMQ%$parentTopic$liteTopic}，{@code $} 为分隔符。
 */
public class LiteUtil {

    /** 父 Topic 与 Lite Topic 之间的分隔符。 */
    public static final char SEPARATOR = '$';
    /** Lite Topic 对应 LMQ 名前缀：{@code %LMQ%$}。 */
    public static final String LITE_TOPIC_PREFIX = MixAll.LMQ_PREFIX + SEPARATOR;

    /**
     * 将父 Topic 与 Lite Topic 编码为 LMQ 队列名。
     * Lite Topic 基于 LMQ 实现且无重试 Topic；并非所有 LMQ 都是 Lite Topic。
     * 格式示例：{@code %LMQ%$parentTopic$liteTopic}
     *
     * @param parentTopic 父 Topic，作命名空间
     * @param liteTopic 子 Topic 字符串
     * @return LMQ 队列名，参数为空时返回 null
     */
    public static String toLmqName(String parentTopic, String liteTopic) {
        if (StringUtils.isEmpty(parentTopic) || StringUtils.isEmpty(liteTopic)) {
            return null;
        }
        return LITE_TOPIC_PREFIX + parentTopic + SEPARATOR + liteTopic;
    }

    /**
     * 判断 lmqName 是否为 Lite Topic 队列（仅检查前缀）。
     *
     * @param lmqName LMQ 队列名
     * @return 是 Lite Topic 队列返回 true
     */
    public static boolean isLiteTopicQueue(String lmqName) {
        return lmqName != null && lmqName.startsWith(LITE_TOPIC_PREFIX);
    }

    /** 从 LMQ 名解析父 Topic；格式不合法时返回 null。 */
    public static String getParentTopic(String lmqName) {
        if (!isLiteTopicQueue(lmqName)) {
            return null;
        }
        int index = lmqName.indexOf(SEPARATOR, LITE_TOPIC_PREFIX.length());
        if (index == -1 || index == lmqName.length() - 1 || index == LITE_TOPIC_PREFIX.length()) {
            return null;
        }
        if (lmqName.indexOf(SEPARATOR, index + 1) != -1) {
            return null;
        }
        return lmqName.substring(LITE_TOPIC_PREFIX.length(), index);
    }

    /** 从 LMQ 名解析 Lite Topic 子名；格式不合法时返回 null。 */
    public static String getLiteTopic(String lmqName) {
        if (!isLiteTopicQueue(lmqName)) {
            return null;
        }
        int index = lmqName.indexOf(SEPARATOR, LITE_TOPIC_PREFIX.length());
        if (index == -1 || index == lmqName.length() - 1 || index == LITE_TOPIC_PREFIX.length()) {
            return null;
        }
        if (lmqName.indexOf(SEPARATOR, index + 1) != -1) {
            return null;
        }
        return lmqName.substring(index + 1);
    }

    /**
     * 从 {@code %LMQ%$parentTopic$liteTopic} 解析父 Topic 与 Lite Topic。
     *
     * @param lmqName LMQ 队列名
     * @return Pair(父 Topic, Lite Topic)，解析失败返回 null
     */
    public static Pair<String, String> getParentAndLiteTopic(String lmqName) {
        if (null == lmqName || !lmqName.startsWith(LITE_TOPIC_PREFIX)) {
            return null;
        }
        String[] array = StringUtils.split(lmqName, SEPARATOR);
        if (array.length != 3) {
            return null;
        }
        return new Pair<>(array[1], array[2]);
    }

    /**
     * 判断 lmqName 是否为指定父 Topic 下的 Lite Topic 队列（前缀匹配）。
     *
     * @param lmqName LMQ 队列名
     * @param parentTopic 父 Topic
     * @return 属于该父 Topic 返回 true
     */
    public static boolean belongsTo(String lmqName, String parentTopic) {
        return lmqName != null && lmqName.startsWith(LITE_TOPIC_PREFIX + parentTopic + SEPARATOR);
    }
}
