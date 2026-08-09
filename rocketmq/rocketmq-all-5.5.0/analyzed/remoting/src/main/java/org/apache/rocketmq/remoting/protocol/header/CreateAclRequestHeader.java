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
 * 创建 ACL 权限规则请求头：指定授权主体（Subject）。
 */
@RocketMQAction(value = RequestCode.AUTH_CREATE_ACL, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class CreateAclRequestHeader implements CommandCustomHeader {

    /** ACL 授权主体（用户或角色）。 */
    private String subject;

    /** 默认构造器。 */
    public CreateAclRequestHeader() {
    }

    /** 以指定主体构造请求头。 */
    public CreateAclRequestHeader(String subject) {
        this.subject = subject;
    }

    @Override
    public void checkFields() throws RemotingCommandException {

    }

    /** 返回授权主体。 */
    public String getSubject() {
        return subject;
    }

    /** 设置授权主体。 */
    public void setSubject(String subject) {
        this.subject = subject;
    }
}
