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

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * 查询 Broker 数据版本的响应头：changed 表示 NameServer 侧 Topic 配置是否已变更。
 */
public class QueryDataVersionResponseHeader implements CommandCustomHeader {
    /** 数据版本是否已变更。 */
    @CFNotNull
    private Boolean changed;

    /** 校验响应头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }

    /** 返回数据版本是否已变更。 */
    public Boolean getChanged() {
        return changed;
    }

    /** 设置数据版本是否已变更。 */
    public void setChanged(Boolean changed) {
        this.changed = changed;
    }

    /** 返回含 changed 字段的调试字符串。 */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QueryDataVersionResponseHeader{");
        sb.append("changed=").append(changed);
        sb.append('}');
        return sb.toString();
    }
}
