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
package org.apache.rocketmq.remoting.protocol.body;

import java.util.HashSet;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 消费者 Group 名称列表，用于 Remoting 批量查询或管理接口。
 */
public class GroupList extends RemotingSerializable {
    /** Group 名集合。 */
    private HashSet<String> groupList = new HashSet<>();

    /** 返回 Group 集合。 */
    public HashSet<String> getGroupList() {
        return groupList;
    }

    /** 设置 Group 集合。 */
    public void setGroupList(HashSet<String> groupList) {
        this.groupList = groupList;
    }
}
