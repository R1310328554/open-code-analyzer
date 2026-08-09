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

package org.apache.rocketmq.tools.command.topic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * allocateMQ 命令的 JSON 输出载体：IP 到已分配 {@link MessageQueue} 列表的映射。
 */
public class RebalanceResult {
    /** 各消费者 IP 对应的队列分配结果。 */
    private Map<String/*ip*/, List<MessageQueue>> result = new HashMap<>();

    /** 返回 IP 到队列列表的映射。 */
    public Map<String, List<MessageQueue>> getResult() {
        return result;
    }

    /** 设置 IP 到队列列表的映射。 */
    public void setResult(final Map<String, List<MessageQueue>> result) {
        this.result = result;
    }
}
