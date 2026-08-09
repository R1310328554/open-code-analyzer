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
 * Pop 长轮询信息查询响应头：返回当前轮询中的 Consumer 数量。
 */
public class PollingInfoResponseHeader implements CommandCustomHeader {


    /** 当前处于长轮询等待中的 Consumer 数量。 */
    @CFNotNull
    private int pollingNum;

    /** 返回轮询 Consumer 数量。 */
    public int getPollingNum() {
        return pollingNum;
    }

    /** 设置轮询 Consumer 数量。 */
    public void setPollingNum(int pollingNum) {
        this.pollingNum = pollingNum;
    }

    /** 校验响应头字段（空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }
}
