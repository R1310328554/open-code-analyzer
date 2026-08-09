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
 * $Id: DeleteTopicRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.remoting.protocol.header;

import com.google.common.base.MoreObjects;
import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.rpc.RpcRequestHeader;

/**
 * 克隆消费组位点请求头：将源组在某 Topic 上的消费进度复制到目标组。
 */
@RocketMQAction(value = RequestCode.CLONE_GROUP_OFFSET, action = Action.UPDATE)
public class CloneGroupOffsetRequestHeader extends RpcRequestHeader {
    /** 源消费组名称。 */
    @CFNotNull
    private String srcGroup;
    /** 目标消费组名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.GROUP)
    private String destGroup;
    /** 待克隆位点的 Topic。 */
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    /** 是否离线克隆（不通知在线消费者）。 */
    private boolean offline;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

    /** 返回目标消费组。 */
    public String getDestGroup() {
        return destGroup;
    }

    public void setDestGroup(String destGroup) {
        this.destGroup = destGroup;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回源消费组。 */
    public String getSrcGroup() {

        return srcGroup;
    }

    public void setSrcGroup(String srcGroup) {
        this.srcGroup = srcGroup;
    }

    /** 返回是否离线克隆。 */
    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    /** 返回便于诊断的可读字符串。 */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("srcGroup", srcGroup)
            .add("destGroup", destGroup)
            .add("topic", topic)
            .add("offline", offline)
            .toString();
    }
}
