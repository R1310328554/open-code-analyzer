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
 * 更新 ACL 配置的请求头：指定待更新的 ACL 主体（subject）。
 * 具体 ACL 规则内容由请求 body 携带。
 */
@RocketMQAction(value = RequestCode.AUTH_UPDATE_ACL, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class UpdateAclRequestHeader implements CommandCustomHeader {

    /** ACL 主体标识（如用户或资源名）。 */
    private String subject;

    /** 无参构造。 */
    public UpdateAclRequestHeader() {
    }

    /** 按 ACL 主体构造。 */
    public UpdateAclRequestHeader(String subject) {
        this.subject = subject;
    }

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }

    /** 返回 ACL 主体。 */
    public String getSubject() {
        return subject;
    }

    /** 设置 ACL 主体。 */
    public void setSubject(String subject) {
        this.subject = subject;
    }
}
