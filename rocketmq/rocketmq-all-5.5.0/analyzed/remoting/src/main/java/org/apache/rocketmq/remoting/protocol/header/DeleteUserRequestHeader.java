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
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * 删除集群用户请求头：指定待移除的用户名。
 */
@RocketMQAction(value = RequestCode.AUTH_DELETE_USER, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class DeleteUserRequestHeader implements CommandCustomHeader {

    /** 待删除的用户名。 */
    private String username;

    /** 默认构造器。 */
    public DeleteUserRequestHeader() {
    }

    /** 以指定用户名构造请求头。 */
    public DeleteUserRequestHeader(String username) {
        this.username = username;
    }

    @Override
    public void checkFields() throws RemotingCommandException {

    }

    /** 返回用户名。 */
    public String getUsername() {
        return username;
    }

    /** 设置用户名。 */
    public void setUsername(String username) {
        this.username = username;
    }
}
