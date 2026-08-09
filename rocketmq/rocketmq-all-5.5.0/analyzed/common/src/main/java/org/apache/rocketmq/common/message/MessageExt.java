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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import org.apache.rocketmq.common.TopicFilterType;
import org.apache.rocketmq.common.sysflag.MessageSysFlag;

/**
 * Broker 存储侧扩展消息：队列、Broker 名、Born/Store 主机与时间、
 * CommitLog 偏移、重试次数及事务 prepared 偏移等。
 */
public class MessageExt extends Message {
    private static final long serialVersionUID = 5720810158625748049L;

    /** 消息所在 Broker 名称。 */
    private String brokerName;

    /** 队列 ID。 */
    private int queueId;

    /** CommitLog 中序列化后的存储大小。 */
    private int storeSize;

    /** 队列逻辑偏移。 */
    private long queueOffset;
    /** 消息系统标志（IPv6、多 Tag 等）。 */
    private int sysFlag;
    /** 消息产生时间戳。 */
    private long bornTimestamp;
    /** 消息产生主机地址。 */
    private SocketAddress bornHost;

    /** Broker 存储时间戳。 */
    private long storeTimestamp;
    /** 存储 Broker 主机地址。 */
    private SocketAddress storeHost;
    /** Broker 生成的 msgId（IP + 端口 + 偏移）。 */
    private String msgId;
    /** CommitLog 物理偏移。 */
    private long commitLogOffset;
    /** 消息体 CRC 校验值。 */
    private int bodyCRC;
    /** 重试消费次数。 */
    private int reconsumeTimes;

    /** 半事务消息 prepared 偏移。 */
    private long preparedTransactionOffset;

    public MessageExt() {
    }

    public MessageExt(int queueId, long bornTimestamp, SocketAddress bornHost, long storeTimestamp,
        SocketAddress storeHost, String msgId) {
        this.queueId = queueId;
        this.bornTimestamp = bornTimestamp;
        this.bornHost = bornHost;
        this.storeTimestamp = storeTimestamp;
        this.storeHost = storeHost;
        this.msgId = msgId;
    }

    /** 根据 sysFlag 解析 Topic 过滤类型（单 Tag / 多 Tag）。 */
    public static TopicFilterType parseTopicFilterType(final int sysFlag) {
        if ((sysFlag & MessageSysFlag.MULTI_TAGS_FLAG) == MessageSysFlag.MULTI_TAGS_FLAG) {
            return TopicFilterType.MULTI_TAG;
        }

        return TopicFilterType.SINGLE_TAG;
    }

    /** 将 SocketAddress 写入已有 ByteBuffer（IPv4 4 字节 / IPv6 16 字节 + 端口）。 */
    public static ByteBuffer socketAddress2ByteBuffer(final SocketAddress socketAddress, final ByteBuffer byteBuffer) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        InetAddress address = inetSocketAddress.getAddress();
        if (address instanceof Inet4Address) {
            byteBuffer.put(inetSocketAddress.getAddress().getAddress(), 0, 4);
        } else {
            byteBuffer.put(inetSocketAddress.getAddress().getAddress(), 0, 16);
        }
        byteBuffer.putInt(inetSocketAddress.getPort());
        byteBuffer.flip();
        return byteBuffer;
    }

    /** 分配合适容量并将 SocketAddress 编码为 ByteBuffer。 */
    public static ByteBuffer socketAddress2ByteBuffer(SocketAddress socketAddress) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        InetAddress address = inetSocketAddress.getAddress();
        ByteBuffer byteBuffer;
        if (address instanceof Inet4Address) {
            byteBuffer = ByteBuffer.allocate(4 + 4);
        } else {
            byteBuffer = ByteBuffer.allocate(16 + 4);
        }
        return socketAddress2ByteBuffer(socketAddress, byteBuffer);
    }

    public ByteBuffer getBornHostBytes() {
        return socketAddress2ByteBuffer(this.bornHost);
    }

    public ByteBuffer getBornHostBytes(ByteBuffer byteBuffer) {
        return socketAddress2ByteBuffer(this.bornHost, byteBuffer);
    }

    public ByteBuffer getStoreHostBytes() {
        return socketAddress2ByteBuffer(this.storeHost);
    }

    public ByteBuffer getStoreHostBytes(ByteBuffer byteBuffer) {
        return socketAddress2ByteBuffer(this.storeHost, byteBuffer);
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public long getBornTimestamp() {
        return bornTimestamp;
    }

    public void setBornTimestamp(long bornTimestamp) {
        this.bornTimestamp = bornTimestamp;
    }

    public SocketAddress getBornHost() {
        return bornHost;
    }

    public void setBornHost(SocketAddress bornHost) {
        this.bornHost = bornHost;
    }

    /** 返回 bornHost 的 IP 字符串。 */
    public String getBornHostString() {
        if (null != this.bornHost) {
            InetAddress inetAddress = ((InetSocketAddress) this.bornHost).getAddress();

            return null != inetAddress ? inetAddress.getHostAddress() : null;
        }

        return null;
    }

    public String getBornHostNameString() {
        if (null != this.bornHost) {
            if (bornHost instanceof InetSocketAddress) {
                // 使用 getHostString 避免反向 DNS 查询
                return ((InetSocketAddress) bornHost).getHostString();
            }
            InetAddress inetAddress = ((InetSocketAddress) this.bornHost).getAddress();

            return null != inetAddress ? inetAddress.getHostName() : null;
        }

        return null;
    }

    public long getStoreTimestamp() {
        return storeTimestamp;
    }

    public void setStoreTimestamp(long storeTimestamp) {
        this.storeTimestamp = storeTimestamp;
    }

    public SocketAddress getStoreHost() {
        return storeHost;
    }

    public void setStoreHost(SocketAddress storeHost) {
        this.storeHost = storeHost;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public int getSysFlag() {
        return sysFlag;
    }

    public void setSysFlag(int sysFlag) {
        this.sysFlag = sysFlag;
    }

    /** 标记 storeHost 为 IPv6 地址。 */
    public void setStoreHostAddressV6Flag() { this.sysFlag = this.sysFlag | MessageSysFlag.STOREHOSTADDRESS_V6_FLAG; }

    /** 标记 bornHost 为 IPv6 地址。 */
    public void setBornHostV6Flag() { this.sysFlag = this.sysFlag | MessageSysFlag.BORNHOST_V6_FLAG; }

    public int getBodyCRC() {
        return bodyCRC;
    }

    public void setBodyCRC(int bodyCRC) {
        this.bodyCRC = bodyCRC;
    }

    public long getQueueOffset() {
        return queueOffset;
    }

    public void setQueueOffset(long queueOffset) {
        this.queueOffset = queueOffset;
    }

    public long getCommitLogOffset() {
        return commitLogOffset;
    }

    public void setCommitLogOffset(long physicOffset) {
        this.commitLogOffset = physicOffset;
    }

    public int getStoreSize() {
        return storeSize;
    }

    public void setStoreSize(int storeSize) {
        this.storeSize = storeSize;
    }

    public int getReconsumeTimes() {
        return reconsumeTimes;
    }

    public void setReconsumeTimes(int reconsumeTimes) {
        this.reconsumeTimes = reconsumeTimes;
    }

    public long getPreparedTransactionOffset() {
        return preparedTransactionOffset;
    }

    public void setPreparedTransactionOffset(long preparedTransactionOffset) {
        this.preparedTransactionOffset = preparedTransactionOffset;
    }

    /** 从瞬态属性读取 topicSysFlag。 */
    /** 获取 Topic 系统标志，未设置时返回 null。 */
    public Integer getTopicSysFlag() {
        String topicSysFlagString = getProperty(MessageConst.PROPERTY_TRANSIENT_TOPIC_CONFIG);
        if (topicSysFlagString != null && topicSysFlagString.length() > 0) {
            return Integer.valueOf(topicSysFlagString);
        }
        return null;
    }

    /** 写入或清除 topicSysFlag 瞬态属性。 */
    public void setTopicSysFlag(Integer topicSysFlag) {
        if (topicSysFlag == null) {
            clearProperty(MessageConst.PROPERTY_TRANSIENT_TOPIC_CONFIG);
        } else {
            putProperty(MessageConst.PROPERTY_TRANSIENT_TOPIC_CONFIG, String.valueOf(topicSysFlag));
        }
    }

    /** 从瞬态属性读取 groupSysFlag。 */
    /** 获取消费组系统标志，未设置时返回 null。 */
    public Integer getGroupSysFlag() {
        String groupSysFlagString = getProperty(MessageConst.PROPERTY_TRANSIENT_GROUP_CONFIG);
        if (groupSysFlagString != null && groupSysFlagString.length() > 0) {
            return Integer.valueOf(groupSysFlagString);
        }
        return null;
    }

    /** 写入或清除 groupSysFlag 瞬态属性。 */
    public void setGroupSysFlag(Integer groupSysFlag) {
        if (groupSysFlag == null) {
            clearProperty(MessageConst.PROPERTY_TRANSIENT_GROUP_CONFIG);
        } else {
            putProperty(MessageConst.PROPERTY_TRANSIENT_GROUP_CONFIG, String.valueOf(groupSysFlag));
        }
    }

    @Override
    public String toString() {
        return "MessageExt [brokerName=" + brokerName + ", queueId=" + queueId + ", storeSize=" + storeSize + ", queueOffset=" + queueOffset
            + ", sysFlag=" + sysFlag + ", bornTimestamp=" + bornTimestamp + ", bornHost=" + bornHost
            + ", storeTimestamp=" + storeTimestamp + ", storeHost=" + storeHost + ", msgId=" + msgId
            + ", commitLogOffset=" + commitLogOffset + ", bodyCRC=" + bodyCRC + ", reconsumeTimes="
            + reconsumeTimes + ", preparedTransactionOffset=" + preparedTransactionOffset
            + ", toString()=" + super.toString() + "]";
    }
}
