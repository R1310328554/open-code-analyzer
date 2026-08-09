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
 * 列举 ACL 规则的请求头：支持按主体（用户/角色）与资源类型过滤。
 */
@RocketMQAction(value = RequestCode.AUTH_LIST_ACL, resource = ResourceType.CLUSTER, action = Action.GET)
public class ListAclsRequestHeader implements CommandCustomHeader {

    /** 主体过滤条件（用户名或角色），可为空表示不过滤。 */
    private String subjectFilter;

    /** 资源过滤条件（Topic/Group/Cluster 等），可为空表示不过滤。 */
    private String resourceFilter;

    /** 无参构造。 */
    public ListAclsRequestHeader() {
    }

    /** 以主体与资源过滤条件构造请求头。 */
    public ListAclsRequestHeader(String subjectFilter, String resourceFilter) {
        this.subjectFilter = subjectFilter;
        this.resourceFilter = resourceFilter;
    }

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }

    /** 返回主体过滤条件。 */
    public String getSubjectFilter() {
        return subjectFilter;
    }

    /** 设置主体过滤条件。 */
    public void setSubjectFilter(String subjectFilter) {
        this.subjectFilter = subjectFilter;
    }

    /** 返回资源过滤条件。 */
    public String getResourceFilter() {
        return resourceFilter;
    }

    /** 设置资源过滤条件。 */
    public void setResourceFilter(String resourceFilter) {
        this.resourceFilter = resourceFilter;
    }
}
