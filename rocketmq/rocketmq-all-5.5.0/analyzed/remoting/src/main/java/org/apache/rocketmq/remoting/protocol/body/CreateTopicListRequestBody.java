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
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 批量创建 Topic 请求体：携带 {@link TopicConfig} 列表。
 */
public class CreateTopicListRequestBody extends RemotingSerializable {
    /** 待创建的 Topic 配置列表（非空）。 */
    @CFNotNull
    private List<TopicConfig> topicConfigList;

    /** 默认构造，供反序列化使用。 */
    public CreateTopicListRequestBody() {}

    /** 以配置列表初始化。 */
    public CreateTopicListRequestBody(List<TopicConfig> topicConfigList) {
        this.topicConfigList = topicConfigList;
    }

    /** 返回 Topic 配置列表。 */
    public List<TopicConfig> getTopicConfigList() {
        return topicConfigList;
    }

    /** 设置 Topic 配置列表。 */
    public void setTopicConfigList(List<TopicConfig> topicConfigList) {
        this.topicConfigList = topicConfigList;
    }

}
