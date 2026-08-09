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
 * 删除 ACL 权限规则请求头：按主体、策略类型与资源定位待删规则。
 */
@RocketMQAction(value = RequestCode.AUTH_DELETE_ACL, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class DeleteAclRequestHeader implements CommandCustomHeader {

    /** ACL 授权主体。 */
    private String subject;

    /** 策略类型（如 Custom / Default）。 */
    private String policyType;

    /** 受控资源标识（Topic/Group/Cluster 等）。 */
    private String resource;

    /** 默认构造器。 */
    public DeleteAclRequestHeader() {
    }

    /** 以主体与资源构造请求头。 */
    public DeleteAclRequestHeader(String subject, String resource) {
        this.subject = subject;
        this.resource = resource;
    }

    @Override
    public void checkFields() throws RemotingCommandException {

    }

    /** 返回授权主体。 */
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    /** 返回策略类型。 */
    public String getPolicyType() {
        return policyType;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    /** 返回受控资源。 */
    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
