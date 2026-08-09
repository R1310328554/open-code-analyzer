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

import java.util.HashSet;
import java.util.Set;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 同步状态集（SyncStateSet）：Controller 模式下参与同步复制的 BrokerId 集合及纪元。
 */
public class SyncStateSet extends RemotingSerializable {
    /** 同步副本 BrokerId 集合。 */
    private Set<Long> syncStateSet;
    /** 同步状态集纪元，变更时递增。 */
    private int syncStateSetEpoch;

    /** 以副本集合与纪元构造。 */
    public SyncStateSet(Set<Long> syncStateSet, int syncStateSetEpoch) {
        this.syncStateSet = new HashSet<>(syncStateSet);
        this.syncStateSetEpoch = syncStateSetEpoch;
    }

    /** 返回同步副本集合的防御性拷贝。 */
    public Set<Long> getSyncStateSet() {
        return new HashSet<>(syncStateSet);
    }

    public void setSyncStateSet(Set<Long> syncStateSet) {
        this.syncStateSet = new HashSet<>(syncStateSet);
    }

    /** 返回同步状态集纪元。 */
    public int getSyncStateSetEpoch() {
        return syncStateSetEpoch;
    }

    public void setSyncStateSetEpoch(int syncStateSetEpoch) {
        this.syncStateSetEpoch = syncStateSetEpoch;
    }

    @Override
    public String toString() {
        return "SyncStateSet{" +
            "syncStateSet=" + syncStateSet +
            ", syncStateSetEpoch=" + syncStateSetEpoch +
            '}';
    }
}
