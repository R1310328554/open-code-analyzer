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
package org.apache.rocketmq.remoting.protocol.header;

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * 修改不可见时间响应头：返回 Pop 时间、新不可见时长及 Revive 队列 ID。
 */
public class ChangeInvisibleTimeResponseHeader implements CommandCustomHeader {


    /** 消息 Pop 时间戳。 */
    @CFNotNull
    private long popTime;
    /** 生效后的不可见时长（毫秒）。 */
    @CFNotNull
    private long invisibleTime;

    /** Revive 主题队列 ID，用于超时重投。 */
    @CFNotNull
    private int reviveQid;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

    /** 返回 Pop 时间戳。 */
    public long getPopTime() {
        return popTime;
    }

    public void setPopTime(long popTime) {
        this.popTime = popTime;
    }

    /** 返回不可见时长。 */
    public long getInvisibleTime() {
        return invisibleTime;
    }

    public void setInvisibleTime(long invisibleTime) {
        this.invisibleTime = invisibleTime;
    }

    /** 返回 Revive 队列 ID。 */
    public int getReviveQid() {
        return reviveQid;
    }

    public void setReviveQid(int reviveQid) {
        this.reviveQid = reviveQid;
    }
}
