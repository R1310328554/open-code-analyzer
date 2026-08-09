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
package org.apache.rocketmq.namesrv.kvconfig;

import java.util.HashMap;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * KV 配置序列化包装类：将 NameServer 的命名空间键值表持久化为 JSON 并可反序列化加载。
 */
public class KVConfigSerializeWrapper extends RemotingSerializable {
    /** 配置表：外层键为命名空间，内层为键值对。 */
    private HashMap<String/* Namespace */, HashMap<String/* Key */, String/* Value */>> configTable;

    /** 获取完整 KV 配置表。 */
    public HashMap<String, HashMap<String, String>> getConfigTable() {
        return configTable;
    }

    /** 设置 KV 配置表（通常由 JSON 反序列化或导出时填充）。 */
    public void setConfigTable(HashMap<String, HashMap<String, String>> configTable) {
        this.configTable = configTable;
    }
}
