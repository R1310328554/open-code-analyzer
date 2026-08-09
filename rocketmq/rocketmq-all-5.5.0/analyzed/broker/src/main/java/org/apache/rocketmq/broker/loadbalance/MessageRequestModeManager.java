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
package org.apache.rocketmq.broker.loadbalance;

import java.util.concurrent.ConcurrentHashMap;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.broker.BrokerPathConfigHelper;
import org.apache.rocketmq.common.ConfigManager;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;
import org.apache.rocketmq.remoting.protocol.body.SetMessageRequestModeRequestBody;

/**
 * 消息拉取模式配置管理器：持久化 topic × consumerGroup 的 {@link SetMessageRequestModeRequestBody} 映射。
 */
public class MessageRequestModeManager extends ConfigManager {

    private transient BrokerController brokerController;

    private ConcurrentHashMap<String/*topic*/, ConcurrentHashMap<String/*consumerGroup*/, SetMessageRequestModeRequestBody>>
        messageRequestModeMap = new ConcurrentHashMap<>();

    /** 空构造，供 JSON 反序列化使用。 */
    public MessageRequestModeManager() {
        // empty construct for decode
    }

    /** 绑定 Broker 以解析配置文件路径。 */
    public MessageRequestModeManager(BrokerController brokerController) {
        this.brokerController = brokerController;
    }

    /** 设置指定 topic 与 consumerGroup 的消息请求模式（POP/PULL 等）。 */
    public void setMessageRequestMode(String topic, String consumerGroup, SetMessageRequestModeRequestBody requestBody) {
        ConcurrentHashMap<String, SetMessageRequestModeRequestBody> consumerGroup2ModeMap = messageRequestModeMap.get(topic);
        if (consumerGroup2ModeMap == null) {
            consumerGroup2ModeMap = new ConcurrentHashMap<>();
            ConcurrentHashMap<String, SetMessageRequestModeRequestBody> pre =
                messageRequestModeMap.putIfAbsent(topic, consumerGroup2ModeMap);
            if (pre != null) {
                consumerGroup2ModeMap = pre;
            }
        }
        consumerGroup2ModeMap.put(consumerGroup, requestBody);
    }

    /** 查询 topic+group 的消息请求模式；未配置则 null。 */
    public SetMessageRequestModeRequestBody getMessageRequestMode(String topic, String consumerGroup) {
        ConcurrentHashMap<String, SetMessageRequestModeRequestBody> consumerGroup2ModeMap = messageRequestModeMap.get(topic);
        if (consumerGroup2ModeMap != null) {
            return consumerGroup2ModeMap.get(consumerGroup);
        }

        return null;
    }

    /** 返回完整的 topic → group → 模式 映射表。 */
    public ConcurrentHashMap<String, ConcurrentHashMap<String, SetMessageRequestModeRequestBody>> getMessageRequestModeMap() {
        return this.messageRequestModeMap;
    }

    public void setMessageRequestModeMap(ConcurrentHashMap<String, ConcurrentHashMap<String, SetMessageRequestModeRequestBody>> messageRequestModeMap) {
        this.messageRequestModeMap = messageRequestModeMap;
    }

    @Override
    public String encode() {
        return this.encode(false);
    }

    @Override
    /** 返回 messageRequestMode 持久化 JSON 文件路径。 */
    public String configFilePath() {
        return BrokerPathConfigHelper.getMessageRequestModePath(this.brokerController.getMessageStoreConfig().getStorePathRootDir());
    }

    @Override
    /** 从 JSON 恢复 messageRequestModeMap。 */
    public void decode(String jsonString) {
        if (jsonString != null) {
            MessageRequestModeManager obj = RemotingSerializable.fromJson(jsonString, MessageRequestModeManager.class);
            if (obj != null) {
                this.messageRequestModeMap = obj.messageRequestModeMap;
            }
        }
    }

    @Override
    public String encode(boolean prettyFormat) {
        return RemotingSerializable.toJson(this, prettyFormat);
    }
}
