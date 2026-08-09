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

/*
  $Id: QueueData.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.remoting.protocol.route;

/**
 * Topic 在某 Broker 上的队列元数据：读写队列数、权限位与系统标志。
 * 按 brokerName 字典序实现 {@link Comparable}。
 */
public class QueueData implements Comparable<QueueData> {
    /** 承载该 Topic 队列的 Broker 名称。 */
    private String brokerName;
    /** 可读队列数量。 */
    private int readQueueNums;
    /** 可写队列数量。 */
    private int writeQueueNums;
    /** Topic 在该 Broker 上的权限位（读/写/继承等）。 */
    private int perm;
    /** Topic 系统标志位。 */
    private int topicSysFlag;

    public QueueData() {

    }

    /** 深拷贝构造：复制源 QueueData 的全部字段。 */
    public QueueData(QueueData queueData) {
        this.brokerName = queueData.brokerName;
        this.readQueueNums = queueData.readQueueNums;
        this.writeQueueNums = queueData.writeQueueNums;
        this.perm = queueData.perm;
        this.topicSysFlag = queueData.topicSysFlag;
    }

    /** 返回可读队列数。 */
    public int getReadQueueNums() {
        return readQueueNums;
    }

    /** 设置可读队列数。 */
    public void setReadQueueNums(int readQueueNums) {
        this.readQueueNums = readQueueNums;
    }

    /** 返回可写队列数。 */
    public int getWriteQueueNums() {
        return writeQueueNums;
    }

    /** 设置可写队列数。 */
    public void setWriteQueueNums(int writeQueueNums) {
        this.writeQueueNums = writeQueueNums;
    }

    /** 返回权限位。 */
    public int getPerm() {
        return perm;
    }

    /** 设置权限位。 */
    public void setPerm(int perm) {
        this.perm = perm;
    }

    /** 返回 Topic 系统标志。 */
    public int getTopicSysFlag() {
        return topicSysFlag;
    }

    /** 设置 Topic 系统标志。 */
    public void setTopicSysFlag(int topicSysFlag) {
        this.topicSysFlag = topicSysFlag;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((brokerName == null) ? 0 : brokerName.hashCode());
        result = prime * result + perm;
        result = prime * result + readQueueNums;
        result = prime * result + writeQueueNums;
        result = prime * result + topicSysFlag;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        QueueData other = (QueueData) obj;
        if (brokerName == null) {
            if (other.brokerName != null)
                return false;
        } else if (!brokerName.equals(other.brokerName))
            return false;
        if (perm != other.perm)
            return false;
        if (readQueueNums != other.readQueueNums)
            return false;
        if (writeQueueNums != other.writeQueueNums)
            return false;
        return topicSysFlag == other.topicSysFlag;
    }

    @Override
    public String toString() {
        return "QueueData [brokerName=" + brokerName + ", readQueueNums=" + readQueueNums
            + ", writeQueueNums=" + writeQueueNums + ", perm=" + perm + ", topicSysFlag=" + topicSysFlag
            + "]";
    }

    @Override
    /** 按 brokerName 字典序比较。 */
    public int compareTo(QueueData o) {
        return this.brokerName.compareTo(o.getBrokerName());
    }

    /** 返回 Broker 名称。 */
    public String getBrokerName() {
        return brokerName;
    }

    /** 设置 Broker 名称。 */
    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }
}
