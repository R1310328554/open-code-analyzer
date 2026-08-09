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

package org.apache.rocketmq.broker.lite;

import io.netty.channel.Channel;

import java.util.List;
import java.util.Set;
import org.apache.rocketmq.common.lite.LiteSubscription;
import org.apache.rocketmq.common.lite.OffsetOption;

/**
 * Lite 订阅注册表：维护 clientId 到 LMQ 订阅关系，支持部分/全量订阅及 wildcard 查询。
 */
public interface LiteSubscriptionRegistry {

    /** 更新 clientId 对应的 Netty 通道。 */
    void updateClientChannel(String clientId, Channel channel);

    /** 获取 clientId 的 lite 订阅快照。 */
    LiteSubscription getLiteSubscription(String clientId);

    /** 当前活跃 lite 订阅总数。 */
    int getActiveSubscriptionNum();

    /** 增量添加 client 对指定 LMQ 集合的部分订阅。 */
    void addPartialSubscription(String clientId, String group, String topic, Set<String> lmqNameSet, OffsetOption offsetOption);

    /** 移除 client 对部分 LMQ 的订阅。 */
    void removePartialSubscription(String clientId, String group, String topic, Set<String> lmqNameSet);

    /** 全量替换 client 在某 topic 下的 LMQ 订阅集合并携带版本号。 */
    void addCompleteSubscription(String clientId, String group, String topic, Set<String> newLmqNameSet, long version);

    /** 移除 client 的全部 lite 订阅。 */
    void removeCompleteSubscription(String clientId);

    /** 注册 lite 控制面事件监听器。 */
    void addListener(LiteCtlListener listener);

    /** 查询订阅指定 LMQ 的全部 client（按 group 聚合）。 */
    SubscriberWrapper getAllSubscriber(String group, String lmqName);

    /** 查询 wildcard group 对父 topic 的通配订阅 client 列表。 */
    SubscriberWrapper.ListWrapper getWildcardSubscriber(String group, String parentTopic);

    /** 返回指定 group 下所有已注册 lite clientId。 */
    List<String> getAllClientIdByGroup(String group);

    /** LMQ 删除时清理相关订阅，可选是否通知客户端。 */
    void cleanSubscription(String lmqName, boolean notifyClient);

    /** 启动订阅注册表后台任务。 */
    void start();

    /** 关闭并释放订阅注册表资源。 */
    void shutdown();
}
