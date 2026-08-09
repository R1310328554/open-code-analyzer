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

import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.rpc.RpcRequestHeader;

/**
 * 删除订阅组请求头：移除消费组并可选择清理其消费位点。
 */
@RocketMQAction(value = RequestCode.DELETE_SUBSCRIPTIONGROUP, action = Action.DELETE)
public class DeleteSubscriptionGroupRequestHeader extends RpcRequestHeader {
    /** 待删除的消费组名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.GROUP)
    private String groupName;

    /** 是否同步清理该组的消费位点。 */
    private boolean cleanOffset = false;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

    /** 返回消费组名称。 */
    public String getGroupName() {
        return groupName;
    }

    /** 设置消费组名称。 */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /** 返回是否清理位点。 */
    public boolean isCleanOffset() {
        return cleanOffset;
    }

    /** 设置是否清理位点。 */
    public void setCleanOffset(boolean cleanOffset) {
        this.cleanOffset = cleanOffset;
    }
}
