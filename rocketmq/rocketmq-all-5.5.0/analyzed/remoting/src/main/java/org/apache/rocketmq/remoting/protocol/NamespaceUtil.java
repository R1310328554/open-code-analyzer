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
package org.apache.rocketmq.remoting.protocol;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.topic.TopicValidator;

/**
 * 多租户命名空间工具：在 Topic/Group 资源名与 {@code NS%Resource} 形式间转换。
 */
public class NamespaceUtil {
    /** 命名空间与资源名分隔符。 */
    public static final char NAMESPACE_SEPARATOR = '%';
    /** 空字符串常量。 */
    public static final String STRING_BLANK = "";
    /** %RETRY% 前缀长度，用于剥离重试 Topic 前缀。 */
    public static final int RETRY_PREFIX_LENGTH = MixAll.RETRY_GROUP_TOPIC_PREFIX.length();
    /** %DLQ% 前缀长度，用于剥离死信 Topic 前缀。 */
    public static final int DLQ_PREFIX_LENGTH = MixAll.DLQ_GROUP_TOPIC_PREFIX.length();

    /**
     * 从资源名剥离命名空间，例如：
     * (1) MQ_INST_XX%Topic_XXX --> Topic_XXX
     * (2) %RETRY%MQ_INST_XX%GID_XXX --> %RETRY%GID_XXX
     *
     * @param resourceWithNamespace 带命名空间的 topic/groupId
     * @return 不含命名空间的 topic/groupId
     */
    public static String withoutNamespace(String resourceWithNamespace) {
        if (StringUtils.isEmpty(resourceWithNamespace) || isSystemResource(resourceWithNamespace)) {
            return resourceWithNamespace;
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (isRetryTopic(resourceWithNamespace)) {
            stringBuilder.append(MixAll.RETRY_GROUP_TOPIC_PREFIX);
        }
        if (isDLQTopic(resourceWithNamespace)) {
            stringBuilder.append(MixAll.DLQ_GROUP_TOPIC_PREFIX);
        }

        String resourceWithoutRetryAndDLQ = withOutRetryAndDLQ(resourceWithNamespace);
        int index = resourceWithoutRetryAndDLQ.indexOf(NAMESPACE_SEPARATOR);
        if (index > 0) {
            String resourceWithoutNamespace = resourceWithoutRetryAndDLQ.substring(index + 1);
            return stringBuilder.append(resourceWithoutNamespace).toString();
        }

        return resourceWithNamespace;
    }

    /**
     * 仅当资源属于指定 namespace 时才剥离，例如：
     * (1) (MQ_INST_XX1%Topic_XXX1, MQ_INST_XX1) --> Topic_XXX1
     * (2) (MQ_INST_XX2%Topic_XXX2, NULL) --> MQ_INST_XX2%Topic_XXX2
     * (3) (%RETRY%MQ_INST_XX1%GID_XXX1, MQ_INST_XX1) --> %RETRY%GID_XXX1
     * (4) (%RETRY%MQ_INST_XX2%GID_XXX2, MQ_INST_XX3) --> %RETRY%MQ_INST_XX2%GID_XXX2
     *
     * @param resourceWithNamespace 带命名空间的 topic/groupId
     * @param namespace 待剥离的命名空间
     * @return 不含命名空间的 topic/groupId
     */
    public static String withoutNamespace(String resourceWithNamespace, String namespace) {
        if (StringUtils.isEmpty(resourceWithNamespace) || StringUtils.isEmpty(namespace)) {
            return resourceWithNamespace;
        }

        String resourceWithoutRetryAndDLQ = withOutRetryAndDLQ(resourceWithNamespace);
        if (resourceWithoutRetryAndDLQ.startsWith(namespace + NAMESPACE_SEPARATOR)) {
            return withoutNamespace(resourceWithNamespace);
        }

        return resourceWithNamespace;
    }

    /** 为普通资源名添加 namespace 前缀，系统 Topic 或已包装则原样返回。 */
    public static String wrapNamespace(String namespace, String resourceWithOutNamespace) {
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(resourceWithOutNamespace)) {
            return resourceWithOutNamespace;
        }

        if (isSystemResource(resourceWithOutNamespace) || isAlreadyWithNamespace(resourceWithOutNamespace, namespace)) {
            return resourceWithOutNamespace;
        }

        String resourceWithoutRetryAndDLQ = withOutRetryAndDLQ(resourceWithOutNamespace);
        StringBuilder stringBuilder = new StringBuilder();

        if (isRetryTopic(resourceWithOutNamespace)) {
            stringBuilder.append(MixAll.RETRY_GROUP_TOPIC_PREFIX);
        }

        if (isDLQTopic(resourceWithOutNamespace)) {
            stringBuilder.append(MixAll.DLQ_GROUP_TOPIC_PREFIX);
        }

        return stringBuilder.append(namespace).append(NAMESPACE_SEPARATOR).append(resourceWithoutRetryAndDLQ).toString();

    }

    /** 判断资源是否已包含指定 namespace（忽略 %RETRY%/%DLQ% 前缀）。 */
    public static boolean isAlreadyWithNamespace(String resource, String namespace) {
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(resource) || isSystemResource(resource)) {
            return false;
        }

        String resourceWithoutRetryAndDLQ = withOutRetryAndDLQ(resource);

        return resourceWithoutRetryAndDLQ.startsWith(namespace + NAMESPACE_SEPARATOR);
    }

    /** 生成 %RETRY% + namespace%consumerGroup 形式的重试 Topic 名。 */
    public static String wrapNamespaceAndRetry(String namespace, String consumerGroup) {
        if (StringUtils.isEmpty(consumerGroup)) {
            return null;
        }

        return new StringBuilder()
            .append(MixAll.RETRY_GROUP_TOPIC_PREFIX)
            .append(wrapNamespace(namespace, consumerGroup))
            .toString();
    }

    /** 从资源名解析 namespace 段；无分隔符或系统资源返回空串。 */
    public static String getNamespaceFromResource(String resource) {
        if (StringUtils.isEmpty(resource) || isSystemResource(resource)) {
            return STRING_BLANK;
        }
        String resourceWithoutRetryAndDLQ = withOutRetryAndDLQ(resource);
        int index = resourceWithoutRetryAndDLQ.indexOf(NAMESPACE_SEPARATOR);

        return index > 0 ? resourceWithoutRetryAndDLQ.substring(0, index) : STRING_BLANK;
    }

    /** 去掉 %RETRY% 或 %DLQ% 前缀，保留 namespace 与主体名。 */
    public static String withOutRetryAndDLQ(String originalResource) {
        if (StringUtils.isEmpty(originalResource)) {
            return STRING_BLANK;
        }
        if (isRetryTopic(originalResource)) {
            return originalResource.substring(RETRY_PREFIX_LENGTH);
        }

        if (isDLQTopic(originalResource)) {
            return originalResource.substring(DLQ_PREFIX_LENGTH);
        }

        return originalResource;
    }

    /** 系统 Topic 或系统消费组视为不可加 namespace 的资源。 */
    private static boolean isSystemResource(String resource) {
        if (StringUtils.isEmpty(resource)) {
            return false;
        }

        if (TopicValidator.isSystemTopic(resource) || MixAll.isSysConsumerGroup(resource)) {
            return true;
        }

        return false;
    }

    /** 是否以 %RETRY% 开头的重试 Topic。 */
    public static boolean isRetryTopic(String resource) {
        return StringUtils.isNotBlank(resource) && resource.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX);
    }

    /** 是否以 %DLQ% 开头的死信 Topic。 */
    public static boolean isDLQTopic(String resource) {
        return StringUtils.isNotBlank(resource) && resource.startsWith(MixAll.DLQ_GROUP_TOPIC_PREFIX);
    }
}
