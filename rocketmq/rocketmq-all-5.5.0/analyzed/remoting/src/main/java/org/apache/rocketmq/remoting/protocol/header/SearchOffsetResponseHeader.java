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

/**
 * $Id: SearchOffsetResponseHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.remoting.protocol.header;

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * 按时间戳搜索 offset 的响应头：返回匹配到的队列消费位点。
 */
public class SearchOffsetResponseHeader implements CommandCustomHeader {
    /** 搜索到的逻辑 offset。 */
    @CFNotNull
    private Long offset;

    /** 校验响应头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }

    /** 返回搜索到的 offset。 */
    public Long getOffset() {
        return offset;
    }

    /** 设置搜索到的 offset。 */
    public void setOffset(Long offset) {
        this.offset = offset;
    }
}
