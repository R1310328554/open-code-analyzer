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

import java.util.concurrent.atomic.AtomicLong;

/**
 * 元数据版本号：由状态版本、时间戳与递增计数器组成，用于检测配置变更。
 */
public class DataVersion extends RemotingSerializable {
    /** 业务状态版本，通常对应 Controller 选举轮次等。 */
    private long stateVersion = 0L;
    /** 版本更新时间戳（毫秒）。 */
    private long timestamp = System.currentTimeMillis();
    /** 同 stateVersion 下的单调递增计数器。 */
    private AtomicLong counter = new AtomicLong(0);

    /** 从另一个 {@link DataVersion} 拷贝全部字段。 */
    public void assignNewOne(final DataVersion dataVersion) {
        this.timestamp = dataVersion.timestamp;
        this.stateVersion = dataVersion.stateVersion;
        this.counter.set(dataVersion.counter.get());
    }

    /** 递增版本，stateVersion 保持不变。 */
    public void nextVersion() {
        this.nextVersion(0L);
    }

    /** 指定新的 stateVersion 并递增计数器、刷新时间戳。 */
    public void nextVersion(long stateVersion) {
        this.timestamp = System.currentTimeMillis();
        this.stateVersion = stateVersion;
        this.counter.incrementAndGet();
    }

    /** 返回状态版本。 */
    public long getStateVersion() {
        return stateVersion;
    }

    /** 设置状态版本。 */
    public void setStateVersion(long stateVersion) {
        this.stateVersion = stateVersion;
    }

    /** 返回时间戳。 */
    public long getTimestamp() {
        return timestamp;
    }

    /** 设置时间戳。 */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /** 返回计数器引用。 */
    public AtomicLong getCounter() {
        return counter;
    }

    /** 替换计数器实例。 */
    public void setCounter(AtomicLong counter) {
        this.counter = counter;
    }

    /** 按 stateVersion、timestamp 与 counter 值比较相等性。 */
    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DataVersion version = (DataVersion) o;

        if (getStateVersion() != version.getStateVersion())
            return false;
        if (getTimestamp() != version.getTimestamp())
            return false;

        if (counter != null && version.counter != null) {
            return counter.longValue() == version.counter.longValue();
        }

        return null == counter && null == version.counter;

    }

    /** 基于三字段计算哈希码。 */
    @Override
    public int hashCode() {
        int result = (int) (getStateVersion() ^ (getStateVersion() >>> 32));
        result = 31 * result + (int) (getTimestamp() ^ (getTimestamp() >>> 32));
        if (null != counter) {
            long l = counter.get();
            result = 31 * result + (int) (l ^ (l >>> 32));
        }
        return result;
    }

    /** 返回 timestamp 与 counter 的摘要字符串。 */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataVersion[");
        sb.append("timestamp=").append(timestamp);
        sb.append(", counter=").append(counter);
        sb.append(']');
        return sb.toString();
    }

    /** 先比 stateVersion，再比 counter，最后比 timestamp；大者返回 1。 */
    public int compare(DataVersion dataVersion) {
        if (this.getStateVersion() > dataVersion.getStateVersion()) {
            return 1;
        } else if (this.getStateVersion() < dataVersion.getStateVersion()) {
            return -1;
        } else if (this.getCounter().get() > dataVersion.getCounter().get()) {
            return 1;
        } else if (this.getCounter().get() < dataVersion.getCounter().get()) {
            return -1;
        } else if (this.getTimestamp() > dataVersion.getTimestamp()) {
            return 1;
        } else if (this.getTimestamp() < dataVersion.getTimestamp()) {
            return -1;
        }
        return 0;
    }
}
