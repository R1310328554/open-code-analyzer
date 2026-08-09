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

import java.util.HashMap;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 通用键值表 Remoting 体，用于 Admin 接口传递字符串配置或统计项。
 */
public class KVTable extends RemotingSerializable {
    /** 键值映射表。 */
    private HashMap<String, String> table = new HashMap<>();

    /** 返回键值表。 */
    public HashMap<String, String> getTable() {
        return table;
    }

    /** 设置键值表。 */
    public void setTable(HashMap<String, String> table) {
        this.table = table;
    }
}
