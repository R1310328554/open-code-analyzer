/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.testsuite.rest.representation;

import java.text.NumberFormat;

/**
 * JGroups 集群通信统计信息，用于测试套件监控消息与字节流量。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class JGroupsStats {

    /** 带千位分隔符的数字格式化器。 */
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    static {
        NUMBER_FORMAT.setGroupingUsed(true);
    }

    /** 已发送字节数。 */
    private long sentBytes;
    /** 已发送消息数。 */
    private long sentMessages;
    /** 已接收字节数。 */
    private long receivedBytes;
    /** 已接收消息数。 */
    private long receivedMessages;

    /** 默认构造函数。 */
    public JGroupsStats() {
    }

    /**
     * 构造包含完整统计值的实例。
     *
     * @param sentBytes 已发送字节数
     * @param sentMessages 已发送消息数
     * @param receivedBytes 已接收字节数
     * @param receivedMessages 已接收消息数
     */
    public JGroupsStats(long sentBytes, long sentMessages, long receivedBytes, long receivedMessages) {
        this.sentBytes = sentBytes;
        this.sentMessages = sentMessages;
        this.receivedBytes = receivedBytes;
        this.receivedMessages = receivedMessages;
    }

    /** 返回已发送字节数。 */
    public long getSentBytes() {
        return sentBytes;
    }

    /** 设置已发送字节数。 */
    public void setSentBytes(long sentBytes) {
        this.sentBytes = sentBytes;
    }

    /** 返回已发送消息数。 */
    public long getSentMessages() {
        return sentMessages;
    }

    /** 设置已发送消息数。 */
    public void setSentMessages(long sentMessages) {
        this.sentMessages = sentMessages;
    }

    /** 返回已接收字节数。 */
    public long getReceivedBytes() {
        return receivedBytes;
    }

    /** 设置已接收字节数。 */
    public void setReceivedBytes(long receivedBytes) {
        this.receivedBytes = receivedBytes;
    }

    /** 返回已接收消息数。 */
    public long getReceivedMessages() {
        return receivedMessages;
    }

    /** 设置已接收消息数。 */
    public void setReceivedMessages(long receivedMessages) {
        this.receivedMessages = receivedMessages;
    }

    /** 将统计信息格式化为可读字符串。 */
    public String statsAsString() {
        return String.format("sentBytes: %s, sentMessages: %d, receivedBytes: %s, receivedMessages: %d",
                NUMBER_FORMAT.format(sentBytes), sentMessages, NUMBER_FORMAT.format(receivedBytes), receivedMessages);
    }
}
