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

package org.apache.rocketmq.remoting.protocol.header.namesrv;

import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * 删除 NameServer KV 配置的请求头：按 namespace 与 key 移除键值对。
 */
@RocketMQAction(value = RequestCode.DELETE_KV_CONFIG, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class DeleteKVConfigRequestHeader implements CommandCustomHeader {
    /** KV 配置命名空间。 */
    @CFNotNull
    private String namespace;
    /** 待删除的配置键。 */
    @CFNotNull
    private String key;

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }

    /** 返回命名空间。 */
    public String getNamespace() {
        return namespace;
    }

    /** 设置命名空间。 */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /** 返回配置键。 */
    public String getKey() {
        return key;
    }

    /** 设置配置键。 */
    public void setKey(String key) {
        this.key = key;
    }
}
