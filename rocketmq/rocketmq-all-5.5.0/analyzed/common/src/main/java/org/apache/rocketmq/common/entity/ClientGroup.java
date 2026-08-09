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

package org.apache.rocketmq.common.entity;

import java.util.Objects;

/**
 * 客户端 ID 与消费组的复合键，用于 Map/Set 去重与哈希索引。
 */
public class ClientGroup {

    /** 客户端实例 ID。 */
    public final String clientId;
    /** 消费组名。 */
    public final String group;
    /** 缓存的 hashCode（懒计算，0 表示未计算）。 */
    private int hash; // 默认为 0

    /**
     * @param clientId 客户端 ID
     * @param group 消费组名
     */
    public ClientGroup(String clientId, String group) {
        this.clientId = clientId;
        this.group = group;
    }

    /** 按 clientId 与 group 判等。 */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientGroup that = (ClientGroup) o;
        return Objects.equals(clientId, that.clientId)
            && Objects.equals(group, that.group);
    }

    /** 懒计算并缓存 hashCode。 */
    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Objects.hash(clientId, group);
        }
        return hash;
    }

    /** 返回调试字符串。 */
    @Override
    public String toString() {
        return "ClientGroup{" +
            "clientId='" + clientId + '\'' +
            ", group='" + group + '\'' +
            '}';
    }
}
