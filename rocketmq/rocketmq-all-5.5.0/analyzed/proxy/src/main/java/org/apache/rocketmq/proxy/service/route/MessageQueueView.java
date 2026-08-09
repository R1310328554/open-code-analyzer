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
package org.apache.rocketmq.proxy.service.route;

import com.google.common.base.MoreObjects;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;

/**
 * 主题消息队列视图：封装读/写 {@link MessageQueueSelector} 与 {@link TopicRouteWrapper}。
 */
public class MessageQueueView {
    /** 空路由占位视图，表示 NameServer 中不存在该主题。 */
    public static final MessageQueueView WRAPPED_EMPTY_QUEUE = new MessageQueueView("", new TopicRouteData(), null);

    private final MessageQueueSelector readSelector;
    private final MessageQueueSelector writeSelector;
    private final TopicRouteWrapper topicRouteWrapper;


    /** 以主题名、路由数据与惩罚器列表构造视图。 */
    public MessageQueueView(String topic, TopicRouteData topicRouteData, List<MessageQueuePenalizer<AddressableMessageQueue>> penalizer) {
        this(topic, topicRouteData, penalizer, null);
    }

    /**
     * 构造视图并指定队列优先级提供者。
     *
     * @param topic 主题名
     * @param topicRouteData NameServer 返回的路由数据
     * @param penalizer 惩罚器列表
     * @param priorityProvider 优先级提供者
     */
    public MessageQueueView(String topic, TopicRouteData topicRouteData, List<MessageQueuePenalizer<AddressableMessageQueue>> penalizer,
        MessageQueuePriorityProvider<AddressableMessageQueue> priorityProvider) {
        this.topicRouteWrapper = new TopicRouteWrapper(topicRouteData, topic);

        this.readSelector = new MessageQueueSelector(topicRouteWrapper, true, priorityProvider);
        this.writeSelector = new MessageQueueSelector(topicRouteWrapper, false, priorityProvider);

        if (CollectionUtils.isNotEmpty(penalizer)) {
            for (MessageQueuePenalizer<AddressableMessageQueue> p : penalizer) {
                this.readSelector.addPenalizer(p);
                this.writeSelector.addPenalizer(p);
            }
        }
    }

    public TopicRouteData getTopicRouteData() {
        return topicRouteWrapper.getTopicRouteData();
    }

    public TopicRouteWrapper getTopicRouteWrapper() {
        return topicRouteWrapper;
    }

    public String getTopicName() {
        return topicRouteWrapper.getTopicName();
    }

    /** 判断是否为无路由的空占位视图。 */
    public boolean isEmptyCachedQueue() {
        return this == WRAPPED_EMPTY_QUEUE;
    }

    /** 返回读路径队列选择器。 */
    public MessageQueueSelector getReadSelector() {
        return readSelector;
    }

    /** 返回写路径队列选择器。 */
    public MessageQueueSelector getWriteSelector() {
        return writeSelector;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("readSelector", readSelector)
            .add("writeSelector", writeSelector)
            .add("topicRouteWrapper", topicRouteWrapper)
            .toString();
    }
}
