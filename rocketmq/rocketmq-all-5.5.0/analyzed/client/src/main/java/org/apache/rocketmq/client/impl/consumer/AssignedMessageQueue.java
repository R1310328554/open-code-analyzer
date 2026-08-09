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
package org.apache.rocketmq.client.impl.consumer;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * 已分配消息队列状态管理：维护 rebalance 后本消费者持有的队列及其
 * {@link ProcessQueue}、拉取/消费 offset、暂停与 seek 状态。
 */
public class AssignedMessageQueue {

    /** 消息队列 -> 队列状态映射。 */
    private final ConcurrentHashMap<MessageQueue, MessageQueueState> assignedMessageQueueState;

    /** 关联的 rebalance 实现，用于复用已有 ProcessQueue。 */
    private RebalanceImpl rebalanceImpl;

    /** 构造空的已分配队列表。 */
    public AssignedMessageQueue() {
        assignedMessageQueueState = new ConcurrentHashMap<>();
    }

    /** 设置 rebalance 实现引用。 */
    public void setRebalanceImpl(RebalanceImpl rebalanceImpl) {
        this.rebalanceImpl = rebalanceImpl;
    }

    /** 判断指定队列是否已暂停消费；未分配时视为已暂停。 */
    public boolean isPaused(MessageQueue messageQueue) {
        MessageQueueState messageQueueState = assignedMessageQueueState.get(messageQueue);
        if (messageQueueState != null) {
            return messageQueueState.isPaused();
        }
        return true;
    }

    /** 暂停指定队列集合的消费。 */
    public void pause(Collection<MessageQueue> messageQueues) {
        for (MessageQueue messageQueue : messageQueues) {
            MessageQueueState messageQueueState = assignedMessageQueueState.get(messageQueue);
            if (assignedMessageQueueState.get(messageQueue) != null) {
                messageQueueState.setPaused(true);
            }
        }
    }

    /** 恢复指定队列集合的消费。 */
    public void resume(Collection<MessageQueue> messageQueueCollection) {
        for (MessageQueue messageQueue : messageQueueCollection) {
            MessageQueueState messageQueueState = assignedMessageQueueState.get(messageQueue);
            if (assignedMessageQueueState.get(messageQueue) != null) {
                messageQueueState.setPaused(false);
            }
        }
    }

    /** 获取队列对应的 {@link ProcessQueue}。 */
    public ProcessQueue getProcessQueue(MessageQueue messageQueue) {
        MessageQueueState messageQueueState = assignedMessageQueueState.get(messageQueue);
        if (messageQueueState != null) {
            return messageQueueState.getProcessQueue();
        }
        return null;
    }

    /** 获取队列当前拉取 offset，未分配时返回 -1。 */
    public long getPullOffset(MessageQueue messageQueue) {
        MessageQueueState messageQueueState = assignedMessageQueueState.get(messageQueue);
        if (messageQueueState != null) {
            return messageQueueState.getPullOffset();
        }
        return -1;
    }

    /** 更新拉取 offset；ProcessQueue 不匹配时不更新。 */
    public void updatePullOffset(MessageQueue messageQueue, long offset, ProcessQueue processQueue) {
        MessageQueueState messageQueueState = assignedMessageQueueState.get(messageQueue);
        if (messageQueueState != null) {
            if (messageQueueState.getProcessQueue() != processQueue) {
                return;
            }
            messageQueueState.setPullOffset(offset);
        }
    }

    /** 获取消费 offset，未分配时返回 -1。 */
    public long getConsumerOffset(MessageQueue messageQueue) {
        MessageQueueState messageQueueState = assignedMessageQueueState.get(messageQueue);
        if (messageQueueState != null) {
            return messageQueueState.getConsumeOffset();
        }
        return -1;
    }

    /** 更新消费 offset。 */
    public void updateConsumeOffset(MessageQueue messageQueue, long offset) {
        MessageQueueState messageQueueState = assignedMessageQueueState.get(messageQueue);
        if (messageQueueState != null) {
            messageQueueState.setConsumeOffset(offset);
        }
    }

    /** 设置 seek 目标 offset。 */
    public void setSeekOffset(MessageQueue messageQueue, long offset) {
        MessageQueueState messageQueueState = assignedMessageQueueState.get(messageQueue);
        if (messageQueueState != null) {
            messageQueueState.setSeekOffset(offset);
        }
    }

    /** 获取 seek 目标 offset，未分配时返回 -1。 */
    public long getSeekOffset(MessageQueue messageQueue) {
        MessageQueueState messageQueueState = assignedMessageQueueState.get(messageQueue);
        if (messageQueueState != null) {
            return messageQueueState.getSeekOffset();
        }
        return -1;
    }

    /** 按 topic 更新已分配队列：移除不在 assigned 中的队列并标记 dropped。 */
    public void updateAssignedMessageQueue(String topic, Collection<MessageQueue> assigned) {
        synchronized (this.assignedMessageQueueState) {
            Iterator<Map.Entry<MessageQueue, MessageQueueState>> it = this.assignedMessageQueueState.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<MessageQueue, MessageQueueState> next = it.next();
                if (next.getKey().getTopic().equals(topic)) {
                    if (!assigned.contains(next.getKey())) {
                        next.getValue().getProcessQueue().setDropped(true);
                        it.remove();
                    }
                }
            }
            addAssignedMessageQueue(assigned);
        }
    }

    /** 全量更新已分配队列列表。 */
    public void updateAssignedMessageQueue(Collection<MessageQueue> assigned) {
        synchronized (this.assignedMessageQueueState) {
            Iterator<Map.Entry<MessageQueue, MessageQueueState>> it = this.assignedMessageQueueState.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<MessageQueue, MessageQueueState> next = it.next();
                if (!assigned.contains(next.getKey())) {
                    next.getValue().getProcessQueue().setDropped(true);
                    it.remove();
                }
            }
            addAssignedMessageQueue(assigned);
        }
    }

    private void addAssignedMessageQueue(Collection<MessageQueue> assigned) {
        for (MessageQueue messageQueue : assigned) {
            if (!this.assignedMessageQueueState.containsKey(messageQueue)) {
                MessageQueueState messageQueueState;
                if (rebalanceImpl != null && rebalanceImpl.getProcessQueueTable().get(messageQueue) != null) {
                    messageQueueState = new MessageQueueState(messageQueue, rebalanceImpl.getProcessQueueTable().get(messageQueue));
                } else {
                    ProcessQueue processQueue = new ProcessQueue();
                    messageQueueState = new MessageQueueState(messageQueue, processQueue);
                }
                this.assignedMessageQueueState.put(messageQueue, messageQueueState);
            }
        }
    }

    /** 移除指定 topic 下所有已分配队列。 */
    public void removeAssignedMessageQueue(String topic) {
        synchronized (this.assignedMessageQueueState) {
            Iterator<Map.Entry<MessageQueue, MessageQueueState>> it = this.assignedMessageQueueState.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<MessageQueue, MessageQueueState> next = it.next();
                if (next.getKey().getTopic().equals(topic)) {
                    it.remove();
                }
            }
        }
    }

    /** 返回当前已分配的消息队列集合。 */
    public Set<MessageQueue> getAssignedMessageQueues() {
        return this.assignedMessageQueueState.keySet();
    }

    /** 单队列运行时状态：ProcessQueue、offset 与暂停标志。 */
    private class MessageQueueState {
        private MessageQueue messageQueue;
        private ProcessQueue processQueue;
        /** 是否暂停消费。 */
        private volatile boolean paused = false;
        /** 当前拉取 offset。 */
        private volatile long pullOffset = -1;
        /** 当前消费 offset。 */
        private volatile long consumeOffset = -1;
        /** seek 目标 offset。 */
        private volatile long seekOffset = -1;

        private MessageQueueState(MessageQueue messageQueue, ProcessQueue processQueue) {
            this.messageQueue = messageQueue;
            this.processQueue = processQueue;
        }

        public MessageQueue getMessageQueue() {
            return messageQueue;
        }

        public void setMessageQueue(MessageQueue messageQueue) {
            this.messageQueue = messageQueue;
        }

        public boolean isPaused() {
            return paused;
        }

        public void setPaused(boolean paused) {
            this.paused = paused;
        }

        public long getPullOffset() {
            return pullOffset;
        }

        public void setPullOffset(long pullOffset) {
            this.pullOffset = pullOffset;
        }

        public ProcessQueue getProcessQueue() {
            return processQueue;
        }

        public void setProcessQueue(ProcessQueue processQueue) {
            this.processQueue = processQueue;
        }

        public long getConsumeOffset() {
            return consumeOffset;
        }

        public void setConsumeOffset(long consumeOffset) {
            this.consumeOffset = consumeOffset;
        }

        public long getSeekOffset() {
            return seekOffset;
        }

        public void setSeekOffset(long seekOffset) {
            this.seekOffset = seekOffset;
        }
    }
}
