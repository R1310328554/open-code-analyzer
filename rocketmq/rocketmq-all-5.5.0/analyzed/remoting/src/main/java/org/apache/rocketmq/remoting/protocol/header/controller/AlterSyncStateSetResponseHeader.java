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
package org.apache.rocketmq.remoting.protocol.header.controller;

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * Controller 修改 SyncStateSet 的响应头：返回更新后的 syncStateSet epoch。
 */
public class AlterSyncStateSetResponseHeader implements CommandCustomHeader {
    /** 变更后的 SyncStateSet epoch 版本号。 */
    private int newSyncStateSetEpoch;

    /** 默认构造。 */
    public AlterSyncStateSetResponseHeader() {
    }

    /** 返回新的 SyncStateSet epoch。 */
    public int getNewSyncStateSetEpoch() {
        return newSyncStateSetEpoch;
    }

    /** 设置新的 SyncStateSet epoch。 */
    public void setNewSyncStateSetEpoch(int newSyncStateSetEpoch) {
        this.newSyncStateSetEpoch = newSyncStateSetEpoch;
    }

    /** 返回含新 epoch 的调试字符串。 */
    @Override
    public String toString() {
        return "AlterSyncStateSetResponseHeader{" +
            "newSyncStateSetEpoch=" + newSyncStateSetEpoch +
            '}';
    }

    /** 校验响应头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }
}
