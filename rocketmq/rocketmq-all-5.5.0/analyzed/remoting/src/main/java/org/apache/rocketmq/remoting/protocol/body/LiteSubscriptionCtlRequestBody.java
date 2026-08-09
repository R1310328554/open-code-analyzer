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

import java.util.Set;
import org.apache.rocketmq.common.lite.LiteSubscriptionDTO;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * Lite 订阅关系批量变更请求体，携带待增删的 {@link LiteSubscriptionDTO} 集合。
 */
public class LiteSubscriptionCtlRequestBody extends RemotingSerializable {

    /** 待处理的 Lite 订阅项集合。 */
    private Set<LiteSubscriptionDTO> subscriptionSet;

    /** 设置订阅项集合。 */
    public void setSubscriptionSet(Set<LiteSubscriptionDTO> subscriptionSet) {
        this.subscriptionSet = subscriptionSet;
    }

    /** 返回订阅项集合。 */
    public Set<LiteSubscriptionDTO> getSubscriptionSet() {
        return subscriptionSet;
    }
}
