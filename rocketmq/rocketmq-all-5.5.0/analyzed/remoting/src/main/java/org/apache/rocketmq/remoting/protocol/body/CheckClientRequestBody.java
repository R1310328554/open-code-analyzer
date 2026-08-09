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

import org.apache.rocketmq.remoting.protocol.RemotingSerializable;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;

/**
 * 客户端订阅一致性校验请求体：携带 clientId、消费组与订阅信息供 Broker 比对。
 */
public class CheckClientRequestBody extends RemotingSerializable {

    /** 客户端唯一标识。 */
    private String clientId;
    /** 消费组名称。 */
    private String group;
    /** 待校验的订阅数据。 */
    private SubscriptionData subscriptionData;
    /** 命名空间（多租户隔离）。 */
    private String namespace;

    /** 返回客户端 ID。 */
    public String getClientId() {
        return clientId;
    }

    /** 设置客户端 ID。 */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** 返回消费组。 */
    public String getGroup() {
        return group;
    }

    /** 设置消费组。 */
    public void setGroup(String group) {
        this.group = group;
    }

    /** 返回订阅数据。 */
    public SubscriptionData getSubscriptionData() {
        return subscriptionData;
    }

    /** 设置订阅数据。 */
    public void setSubscriptionData(SubscriptionData subscriptionData) {
        this.subscriptionData = subscriptionData;
    }

    /** 返回命名空间。 */
    public String getNamespace() {
        return namespace;
    }

    /** 设置命名空间。 */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
