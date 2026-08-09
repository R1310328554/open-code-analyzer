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
package org.apache.rocketmq.controller.impl.task;

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/** 非活跃 Broker 扫描响应头；具体身份列表在 Remoting 消息体 JSON 中返回。 */
public class CheckNotActiveBrokerResponse implements CommandCustomHeader {
    /** 默认无参构造。 */
    public CheckNotActiveBrokerResponse() {
    }

    /** 本响应无额外字段需校验。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }

    @Override
    public String toString() {
        return "CheckNotActiveBrokerResponse{}";
    }
}
