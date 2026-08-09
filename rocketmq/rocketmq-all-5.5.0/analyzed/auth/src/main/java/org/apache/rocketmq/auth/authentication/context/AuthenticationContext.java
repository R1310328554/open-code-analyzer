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
package org.apache.rocketmq.auth.authentication.context;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * 认证上下文抽象基类：承载通道 ID、RPC 标识及扩展信息。
 */
public abstract class AuthenticationContext {

    private String channelId;

    private String rpcCode;

    private Map<String, Object> extInfo;

    /** 返回 Netty 通道 ID。 */
    public String getChannelId() {
        return channelId;
    }

    /** 设置 Netty 通道 ID。 */
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    /** 返回 RPC 方法标识（gRPC 全名或 Remoting code）。 */
    public String getRpcCode() {
        return rpcCode;
    }

    /** 设置 RPC 方法标识。 */
    public void setRpcCode(String rpcCode) {
        this.rpcCode = rpcCode;
    }

    /** 按 key 读取扩展信息；key 为空或不存在时返回 null。 */
    @SuppressWarnings("unchecked")
    public <T> T getExtInfo(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        if (this.extInfo == null) {
            return null;
        }
        Object value = this.extInfo.get(key);
        if (value == null) {
            return null;
        }
        return (T) value;
    }

    /** 写入单条扩展信息；key 或 value 无效时忽略。 */
    public void setExtInfo(String key, Object value) {
        if (StringUtils.isBlank(key) || value == null) {
            return;
        }
        if (this.extInfo == null) {
            this.extInfo = new HashMap<>();
        }
        this.extInfo.put(key, value);
    }

    /** 判断指定 key 的扩展信息是否存在且非 null。 */
    public boolean hasExtInfo(String key) {
        Object value = getExtInfo(key);
        return value != null;
    }

    /** 返回扩展信息映射（可能为 null）。 */
    public Map<String, Object> getExtInfo() {
        return extInfo;
    }

    /** 批量设置扩展信息映射。 */
    public void setExtInfo(Map<String, Object> extInfo) {
        this.extInfo = extInfo;
    }
}
