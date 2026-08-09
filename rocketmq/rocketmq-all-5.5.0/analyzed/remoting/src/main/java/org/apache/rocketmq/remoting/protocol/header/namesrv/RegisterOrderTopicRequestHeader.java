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
 * $Id: RegisterOrderTopicRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.remoting.protocol.header.namesrv;

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * 向 NameServer 注册顺序 Topic 的请求头：携带 Topic 名与顺序 Topic 配置串。
 */
public class RegisterOrderTopicRequestHeader implements CommandCustomHeader {
    /** 顺序 Topic 名称。 */
    @CFNotNull
    private String topic;
    /** 顺序 Topic 配置串（队列与 Broker 映射描述）。 */
    @CFNotNull
    private String orderTopicString;

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }

    /** 返回 Topic 名称。 */
    public String getTopic() {
        return topic;
    }

    /** 设置 Topic 名称。 */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回顺序 Topic 配置串。 */
    public String getOrderTopicString() {
        return orderTopicString;
    }

    /** 设置顺序 Topic 配置串。 */
    public void setOrderTopicString(String orderTopicString) {
        this.orderTopicString = orderTopicString;
    }
}
