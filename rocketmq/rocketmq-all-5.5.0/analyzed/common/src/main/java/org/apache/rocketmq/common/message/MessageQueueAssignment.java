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
package org.apache.rocketmq.common.message;

import java.io.Serializable;
import java.util.Map;

/**
 * 队列分配结果：绑定 {@link MessageQueue}、请求模式（PULL/POP）及扩展附件。
 */
public class MessageQueueAssignment implements Serializable {

    private static final long serialVersionUID = 8092600270527861645L;

    /** 分配的队列。 */
    private MessageQueue messageQueue;

    /** 消费请求模式，默认 PULL。 */
    private MessageRequestMode mode = MessageRequestMode.PULL;

    /** 扩展附件键值对。 */
    private Map<String, String> attachments;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((messageQueue == null) ? 0 : messageQueue.hashCode());
        result = prime * result + ((mode == null) ? 0 : mode.hashCode());
        result = prime * result + ((attachments == null) ? 0 : attachments.hashCode());
        return result;
    }

    /** 相等性仅比较 messageQueue。 */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MessageQueueAssignment other = (MessageQueueAssignment) obj;
        return messageQueue.equals(other.messageQueue);
    }

    @Override
    public String toString() {
        return "MessageQueueAssignment [MessageQueue=" + messageQueue + ", Mode=" + mode + "]";
    }

    public MessageQueue getMessageQueue() {
        return messageQueue;
    }

    public void setMessageQueue(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    public MessageRequestMode getMode() {
        return mode;
    }

    public void setMode(MessageRequestMode mode) {
        this.mode = mode;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments;
    }

}
