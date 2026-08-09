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
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNullable;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * 向集群添加 Broker 的请求头：可选指定 Broker 配置文件路径。
 */
@RocketMQAction(value = RequestCode.ADD_BROKER, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class AddBrokerRequestHeader implements CommandCustomHeader {
    /** Broker 配置文件路径，可为空。 */
    @CFNullable
    private String configPath;


    @Override
    public void checkFields() throws RemotingCommandException {

    }

    /** 返回配置文件路径。 */
    public String getConfigPath() {
        return configPath;
    }

    /** 设置配置文件路径。 */
    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }
}
