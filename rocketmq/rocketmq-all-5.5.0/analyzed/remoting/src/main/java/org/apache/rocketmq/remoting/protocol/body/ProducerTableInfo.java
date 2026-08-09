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

import java.util.List;
import java.util.Map;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 生产者组 → 生产者实例列表映射表，用于 Admin 查询在线生产者。
 */
public class ProducerTableInfo extends RemotingSerializable {
    /** 以映射表初始化。 */
    public ProducerTableInfo(Map<String, List<ProducerInfo>> data) {
        this.data = data;
    }

    /** groupName → {@link ProducerInfo} 列表。 */
    private Map<String, List<ProducerInfo>> data;

    /** 返回生产者映射表。 */
    public Map<String, List<ProducerInfo>> getData() {
        return data;
    }

    /** 设置生产者映射表。 */
    public void setData(Map<String, List<ProducerInfo>> data) {
        this.data = data;
    }
}
