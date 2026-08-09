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

import java.util.List;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;

/**
 * 订阅组配置列表 Remoting 体：批量传输 {@link SubscriptionGroupConfig} 条目。
 */
public class SubscriptionGroupList extends RemotingSerializable {
    /** 订阅组配置列表，不可为空。 */
    @CFNotNull
    private List<SubscriptionGroupConfig> groupConfigList;

    public SubscriptionGroupList() {}

    /** 以给定配置列表构造。 */
    public SubscriptionGroupList(List<SubscriptionGroupConfig> groupConfigList) {
        this.groupConfigList = groupConfigList;
    }

    /** 返回订阅组配置列表。 */
    public List<SubscriptionGroupConfig> getGroupConfigList() {
        return groupConfigList;
    }

    /** 设置订阅组配置列表。 */
    public void setGroupConfigList(List<SubscriptionGroupConfig> groupConfigList) {
        this.groupConfigList = groupConfigList;
    }

}
