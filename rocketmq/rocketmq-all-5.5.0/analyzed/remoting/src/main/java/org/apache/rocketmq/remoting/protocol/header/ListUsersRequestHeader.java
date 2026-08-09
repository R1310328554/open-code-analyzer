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
 * 列举 ACL 用户的请求头：支持按用户名前缀或模式过滤。
 */
@RocketMQAction(value = RequestCode.AUTH_LIST_USER, resource = ResourceType.CLUSTER, action = Action.GET)
public class ListUsersRequestHeader implements CommandCustomHeader {

    /** 用户名过滤条件，可为空表示列举全部用户。 */
    private String filter;

    /** 无参构造。 */
    public ListUsersRequestHeader() {
    }

    /** 以过滤条件构造请求头。 */
    public ListUsersRequestHeader(String filter) {
        this.filter = filter;
    }

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }

    /** 返回用户名过滤条件。 */
    public String getFilter() {
        return filter;
    }

    /** 设置用户名过滤条件。 */
    public void setFilter(String filter) {
        this.filter = filter;
    }
}
