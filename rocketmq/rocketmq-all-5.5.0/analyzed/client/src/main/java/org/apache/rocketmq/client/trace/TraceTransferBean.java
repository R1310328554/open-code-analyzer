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
package org.apache.rocketmq.client.trace;

import java.util.HashSet;
import java.util.Set;

/**
 * 轨迹传输实体：携带编码后的轨迹字符串与用于索引的 key 集合（msgId、业务 keys）。
 */
public class TraceTransferBean {
    /** 分隔符编码的轨迹数据正文。 */
    private String transData;
    /** 轨迹索引 key 集合，便于按 msgId 或业务 key 检索。 */
    private Set<String> transKey = new HashSet<>();

    public String getTransData() {
        return transData;
    }

    public void setTransData(String transData) {
        this.transData = transData;
    }

    public Set<String> getTransKey() {
        return transKey;
    }

    public void setTransKey(Set<String> transKey) {
        this.transKey = transKey;
    }
}
