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

package org.apache.rocketmq.remoting.protocol.body;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.rocketmq.remoting.protocol.DataVersion;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 消费位点持久化包装：topic@group → queueId → offset 及 {@link DataVersion}。
 */
public class ConsumerOffsetSerializeWrapper extends RemotingSerializable {
    /** topic@group → (queueId → 消费位点)。 */
    private ConcurrentMap<String/* topic@group */, ConcurrentMap<Integer, Long>> offsetTable =
        new ConcurrentHashMap<>(512);
    /** 位点表版本号，用于增量同步。 */
    private DataVersion dataVersion;

    /** 返回位点表。 */
    public ConcurrentMap<String, ConcurrentMap<Integer, Long>> getOffsetTable() {
        return offsetTable;
    }

    /** 设置位点表。 */
    public void setOffsetTable(ConcurrentMap<String, ConcurrentMap<Integer, Long>> offsetTable) {
        this.offsetTable = offsetTable;
    }

    /** 返回数据版本。 */
    public DataVersion getDataVersion() {
        return dataVersion;
    }

    /** 设置数据版本。 */
    public void setDataVersion(DataVersion dataVersion) {
        this.dataVersion = dataVersion;
    }
}
