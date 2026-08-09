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

package org.apache.rocketmq.remoting.protocol.header;

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * 查询 Lite Topic 信息的请求头：指定父 Topic 与 Lite Topic。
 */
public class GetLiteTopicInfoRequestHeader implements CommandCustomHeader {

    /** 父 Topic 名称。 */
    private String parentTopic;
    /** Lite Topic 名称。 */
    private String liteTopic;

    /** 校验请求头字段（空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }

    /** 返回父 Topic 名称。 */
    public String getParentTopic() {
        return parentTopic;
    }

    /** 设置父 Topic 名称。 */
    public void setParentTopic(String parentTopic) {
        this.parentTopic = parentTopic;
    }

    /** 返回 Lite Topic 名称。 */
    public String getLiteTopic() {
        return liteTopic;
    }

    /** 设置 Lite Topic 名称。 */
    public void setLiteTopic(String liteTopic) {
        this.liteTopic = liteTopic;
    }
}
